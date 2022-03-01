package cn.edu.sysu.workflow.activiti.admission.timewheel;

import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.Arrays;

/**
 * 定时器
 *
 * @author: Gordan Lin
 * @create: 2019/12/12
 **/
public class Timer {

    private static Logger logger = LoggerFactory.getLogger(Timer.class);

    // 预测长度
    private static final int PREDICT_LENGTH = 15;
    // 时间槽时间长度，单位是毫秒
    private static final int TICK_MS = 1000;
    // 时间槽个数
    private static final int WHEEL_SIZE = 60;

    private static final int REQUEST_THRESHOLD = 50;

    private static final int TENANT_SUM = 2;
    // 时间轮
    private TimingWheel timingWheel;

    //限流器
    private RateLimiter rateLimiter;

    //USE_PREDICT==false 不使用预测
    //USE_PREDICT==true+alpha==0&&beta==0 全提前
//    private final boolean USE_PREDICT = true;
//
//    private final double alpha = 0.5;
//    private final double beta = 0.3;

    private final boolean USE_PREDICT = true;

    private final double alpha = 0;
    private final double beta = 0;
//
//    private final boolean USE_PREDICT = false;

    // 对于一个Timer以及附属的时间轮，都只有一个priorityQueue priorityQueue中存放的是Bucket
    private PriorityBlockingQueue<BucketWithTenantQueue> priorityQueue =
        new PriorityBlockingQueue<>(WHEEL_SIZE + 1, new Comparator<BucketWithTenantQueue>() {
            @Override public int compare(BucketWithTenantQueue bucket1, BucketWithTenantQueue bucket2) {
                return (int)(bucket1.getExpire() - bucket2.getExpire());
            }
        });

    //存放时间槽中放不下的任务，按ddl排序
    public static PriorityBlockingQueue<TimerTask> bufferPriorityQueue = new PriorityBlockingQueue<>();

    private int[] predictTimeWindow = new int[PREDICT_LENGTH];

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
        rateLimiter = RateLimiter.create(REQUEST_THRESHOLD);
        workerThreadPool = Executors.newFixedThreadPool(35,
            new ThreadFactoryBuilder().setPriority(10).setNameFormat("TimerWheelWorker").build());
        bossThreadPool = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setPriority(10).setNameFormat("TimerWheelBoss").build());

        timingWheel =
            new TimingWheel(TICK_MS, WHEEL_SIZE, System.currentTimeMillis(), priorityQueue, REQUEST_THRESHOLD);
        bossThreadPool.scheduleAtFixedRate(() -> {
            TIMER_INSTANCE.advanceClock();
        }, 0, TICK_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 将任务添加到时间轮
     */
    public void addTask(TimerTask timerTask) {
        if (!timingWheel.addTask(timerTask)) {
            //if为true 即为delay=0ms
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
        advancePredictTimeWindow();
        BucketWithTenantQueue bucket = priorityQueue.peek();
        int allSubmitTaskNum = 0;
        try {
            //时间槽为空或时间槽未到期
            if (bucket == null || bucket.getExpire() > currentTimestamp) {
                allSubmitTaskNum = submitTaskFromBufferPriorityQueue(REQUEST_THRESHOLD);
                logger.info("共提交" + allSubmitTaskNum + "个任务");
                return;
            }
            //pop出来的就是到期的
            allSubmitTaskNum = admittingWithThresholdAndSubmit();
            logger.info("共提交" + allSubmitTaskNum + "个任务");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int admittingWithThresholdAndSubmit() {
        int allSubmitTaskNum = 0;
        //从到期时间槽中提交
        List<TimerTask> taskList = null;
        List<TimerTask> remainTaskList = null;
        BucketWithTenantQueue bucket = priorityQueue.poll();
        taskList = bucket.removeTaskAndGet(REQUEST_THRESHOLD);
        remainTaskList = bucket.removeTaskAndGet(-1);
        if (bucket.getTaskNum() != 0) {
            logger.warn("到期后时间槽未清空");
        }
        for (TimerTask timerTask : taskList) {
            workerThreadPool.submit(timerTask.getTask());
            allSubmitTaskNum++;
        }
        for (TimerTask timerTask : remainTaskList) {
            bufferPriorityQueue.offer(timerTask);
        }
        int remainTaskNum = REQUEST_THRESHOLD - allSubmitTaskNum;
        allSubmitTaskNum = allSubmitTaskNum + submitTaskFromBufferPriorityQueue(remainTaskNum);
        return allSubmitTaskNum;
    }

    public synchronized void addToPredictTimeWindow(int predictNextTaskInterval) {
        if (predictNextTaskInterval != 0) {
            predictTimeWindow[predictNextTaskInterval]++;
        }
    }

    public synchronized void advancePredictTimeWindow() {
        for (int i = 0; i < PREDICT_LENGTH - 2; i++) {
            predictTimeWindow[i] = predictTimeWindow[i + 1];
        }
        predictTimeWindow[PREDICT_LENGTH - 1] = 0;
        //        logger.info("预测时间窗为: " + predictTimeWindowToString());

    }

    public String predictTimeWindowToString() {
        return Arrays.toString(predictTimeWindow);
    }

    public int getMaxElementPredictTimeWindow() {
        return Arrays.stream(predictTimeWindow).max().getAsInt();
    }

    public double getMeanEleMeantPredictTimeWindow() {
        double sum = 0;
        for (int i = 0; i < PREDICT_LENGTH; i++) {
            sum = sum + predictTimeWindow[i];
        }
        return sum / PREDICT_LENGTH;
    }

    public boolean isMove() {
        if (USE_PREDICT == false) {
            return false;
        }
        //使用预测
        if (getMaxElementPredictTimeWindow() >= REQUEST_THRESHOLD * alpha
            || getMeanEleMeantPredictTimeWindow() >= REQUEST_THRESHOLD * beta) {
            return true;
        }
        return false;
    }

    public int move(int moveCount) {
        int alreadyMoveCount = 0;
        if (moveCount == 0) {
            return alreadyMoveCount;
        }

        if (priorityQueue.size() == 0) {
            return alreadyMoveCount;
        }
        //提前最多PREDICT_LENGTH个时间槽
        int i = 0;
        while (moveCount != 0 && i < PREDICT_LENGTH) {
            BucketWithTenantQueue bucket = priorityQueue.peek();
            if (bucket == null) {
                return alreadyMoveCount;
            } else {
                //bucket不为空
                List<TimerTask> taskList = bucket.removeTaskAndGet(moveCount);
                i++;
                for (TimerTask timerTask : taskList) {
                    workerThreadPool.submit(timerTask.getTask());
                    alreadyMoveCount++;
                    moveCount--;
                    logger.info("提前提取时间槽内任务");
                }
            }
        }

        //提前一个时间槽
        //        if (moveCount != 0) {
        //            BucketWithPriorityTaskQueue bucket = priorityQueue.peek();
        //            if (bucket == null) {
        //                return alreadyMoveCount;
        //            } else {
        //                //bucket不为空
        //                List<TimerTask> taskList = bucket.removeTaskAndGet(moveCount);
        //                for (TimerTask timerTask : taskList) {
        //                    workerThreadPool.submit(timerTask.getTask());
        //                    alreadyMoveCount++;
        //                    logger.info("提前提取时间槽内任务");
        //                }
        //            }
        //        }
        return alreadyMoveCount;
    }

    //返回从暂存队列和提前时间槽中共提交的任务数量
    public int submitTaskFromBufferPriorityQueue(int taskNum) {
        int allSubmitTaskNum = 0;
        if (taskNum == 0) {
            return allSubmitTaskNum;
        }
        //暂存队列为空
        if (bufferPriorityQueue.size() == 0) {
            if (isMove() == true) {
                allSubmitTaskNum = move(taskNum);
            }
        }
        //暂存队列中有
        else {
            int AlreadyMoveTaskNum = 0;
            TimerTask timerTask = null;
            int moveCount = 0;
            for (int i = 0; i < taskNum; i++) {
                timerTask = bufferPriorityQueue.poll();
                // 暂存队列中的任务数未达taskNum
                if (timerTask == null) {
                    moveCount = taskNum - i;
                    break;
                } else {
                    workerThreadPool.submit(timerTask.getTask());
                    allSubmitTaskNum++;
                    logger.info("从bufferPriorityQueue中提交任务");
                }
            }
            //从暂存队列中提取的未达阈值
            if (moveCount != 0) {
                //判断是否要提前
                if (isMove() == true) {
                    allSubmitTaskNum = allSubmitTaskNum + move(moveCount);
                }
            }

        }
        return allSubmitTaskNum;

    }

}
