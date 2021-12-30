package cn.edu.sysu.workflow.activiti.admission.timewheel;

import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 定时器
 *
 * @author: Gordan Lin
 * @create: 2019/12/12
 **/
public class Timer {

    private static Logger logger = LoggerFactory.getLogger(Timer.class);

    // 时间槽时间长度，单位是毫秒
    private static final int TICK_MS = 1000;
    // 时间槽个数
    private static final int WHEEL_SIZE = 60;
    // 滑动时间窗口大小
    private static final int TIME_WINDOW_SIZE = 20;

    private static final int REQUST_THRESHOLD = 60;

    // 时间轮
    private TimingWheel timingWheel;

    //限流器
    private RateLimiter rateLimiter;

    // 对于一个Timer以及附属的时间轮，都只有一个priorityQueue priorityQueue中存放的是Bucket
    private PriorityBlockingQueue<Bucket> priorityQueue =
        new PriorityBlockingQueue<>(WHEEL_SIZE + 1, new Comparator<Bucket>() {
            @Override public int compare(Bucket bucket1, Bucket bucket2) {
                return (int)(bucket1.getExpire() - bucket2.getExpire());
            }
        });
    public static PriorityBlockingQueue<TimerTask> bufferPriorityQueue = new PriorityBlockingQueue<>();
    // 优先队列中各个bucket任务数，通过TIME_WINDOW_SIZE控制长度实现滑动时间窗口（空间换时间）
    private LinkedList<Integer> timeWindow = new LinkedList<>();

    private ExecutorService workerThreadPool;

    private ScheduledExecutorService bossThreadPool;

    private static Timer TIMER_INSTANCE;

    public static Timer getInstance() {
        if (TIMER_INSTANCE == null) {
            synchronized (Timer.class) {
                if (TIMER_INSTANCE == null) {
                    TIMER_INSTANCE = new Timer();
                }
            }
        }
        return TIMER_INSTANCE;
    }

    private Timer() {
        rateLimiter = RateLimiter.create(REQUST_THRESHOLD);
        workerThreadPool = Executors.newFixedThreadPool(100,
            new ThreadFactoryBuilder().setPriority(10).setNameFormat("TimerWheelWorker").build());
        bossThreadPool = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setPriority(10).setNameFormat("TimerWheelBoss").build());

        timingWheel = new TimingWheel(TICK_MS, WHEEL_SIZE, System.currentTimeMillis(), priorityQueue, REQUST_THRESHOLD);
        bossThreadPool.scheduleAtFixedRate(() -> {
            TIMER_INSTANCE.advanceClock();
        }, 0, TICK_MS, TimeUnit.MILLISECONDS);
    }

    public LinkedList<Integer> getTimeWindow() {
        return timeWindow;
    }

    /**
     * 将任务添加到时间轮
     */
    public void addTask(TimerTask timerTask) {
        if (!timingWheel.addTask(timerTask)) {
            //在限流范围内
            if (rateLimiter.tryAcquire() == true) {
                workerThreadPool.submit(timerTask.getTask());
            }
            //超出阈值，延时再提交
            else {
                logger.debug("delayMs=0的请求由于限流被延迟");
                timerTask.setDelayMs(timerTask.getDelayMs() + TICK_MS);
                addTask(timerTask);
            }
        }
    }

    /**
     * 指针推进
     */
    public void advanceClock() {
        logger.debug("指针向前一格");
        long currentTimestamp = System.currentTimeMillis();
        timingWheel.advanceClock(currentTimestamp);

        Bucket bucket = priorityQueue.peek();
        if (bucket == null || bucket.getExpire() > currentTimestamp) {
            logger.debug("时间槽为空或者未到期");
            if (bufferPriorityQueue.size() != 0) {
                TimerTask timerTask = null;
                for (int i = 0; i < REQUST_THRESHOLD; i++) {
                    timerTask = bufferPriorityQueue.poll();
                    if (timerTask == null) {
                        break;
                    } else {
                        workerThreadPool.submit(timerTask.getTask());
                        logger.debug("从bufferPriorityQueue中提交任务");
                    }
                }
                return;
            } else {
                //logger.info("当前负载：0");
                return;
            }
        }

        //pop出来的就是到期的
        try {
            // 执行请求
            List<TimerTask> taskList = admittingWithThreshold();
            //测试
            //直接pop 优先级队列的顶部
            //            List<TimerTask> taskList = PollPriorityQueue();
            //            logger.info("当前负载：{}", taskList.size());
            if (!timeWindow.isEmpty()) {
//                logger.debug("当前时间窗第一格：{}", timeWindow.get(0));
                timeWindow.removeFirst();
            }

            for (TimerTask timerTask : taskList) {
                //                if (rateLimiter.tryAcquire() == true) {
                //                    workerThreadPool.submit(timerTask.getTask());
                //                } else {
                //                    logger.debug("请求由于限流被延迟");
                //                    timerTask.setDelayMs(timerTask.getDelayMs() + TICK_MS);
                //                    addTask(timerTask);
                //                }
                workerThreadPool.submit(timerTask.getTask());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<TimerTask> PollPriorityQueue() {
        Bucket bucket = priorityQueue.poll();
        List<TimerTask> taskList = bucket.removeTaskAndGet(-1);
        //        if (bucket.getTaskNum()>REQUST_THRESHOLD){
        //            logger.debug("bucket中的请求数量超出阈值");
        ////            //后面的一个bucket
        ////            Bucket tempBucket = priorityQueue.peek();
        //            taskList=bucket.removeTaskAndGet(REQUST_THRESHOLD);
        //            //remainTask都是已经到期了的 将它们延后一个t0
        //            List<TimerTask> remainTask=bucket.removeTaskAndGet(-1);
        //            for (TimerTask timerTask : remainTask) {
        //                timerTask.setDelayMs(TICK_MS);
        //                addTask(timerTask);
        //            }
        //        }
        //        else{
        //            taskList=bucket.removeTaskAndGet(-1);
        //        }
        return taskList;

    }

    /**
     * admitting算法
     *
     * @return
     */
    private List<TimerTask> admitting() {
        List<TimerTask> taskList = null;

        int requestSum = 0; // 滑动窗口请求总数
        int requestAvg = 0; // 滑动窗口请求平均数
        int i;
        for (i = 1; i <= timeWindow.size(); i++) {
            if (i > TIME_WINDOW_SIZE) {
                break;
            }
            requestSum += timeWindow.get(i - 1);
        }
        requestAvg = requestSum / i;

        if (timeWindow.isEmpty() || timeWindow.get(0) > requestAvg) {
            Bucket bucket = priorityQueue.poll();
            taskList = bucket.removeTaskAndGet(-1);
        } else {
            Bucket bucket = priorityQueue.poll();
            taskList = bucket.removeTaskAndGet(-1);

            int moveCount = requestAvg - timeWindow.get(0);
            // 移动时间槽请求
            if (moveCount > 0) {
                //从队列中有的第一个bucket移动 不一定是后一个bucket
                Bucket tempBucket = priorityQueue.peek();
                //可能会不足moveCount
                List<TimerTask> tempTaskList = tempBucket.removeTaskAndGet(moveCount);
                taskList.addAll(tempTaskList);
                int remain = timeWindow.get(1) - tempTaskList.size();
                timeWindow.set(1, remain);
            }
        }
        return taskList;
    }

    private List<TimerTask> admittingWithThreshold() {
        List<TimerTask> taskList = null;
        Bucket bucket = priorityQueue.poll();
        int remainTaskNum = REQUST_THRESHOLD - bucket.getTaskNum();
        taskList = bucket.removeTaskAndGet(-1);
        logger.debug("remainTaskNum{}",remainTaskNum);
        if (bufferPriorityQueue.size() != 0) {
            TimerTask timerTask = null;
            for (int i = 0; i < remainTaskNum; i++) {
                timerTask = bufferPriorityQueue.poll();
                if (timerTask == null) {
                    break;
                } else {
                    taskList.add(timerTask);
                }
            }
        }
        return taskList;
    }

}
