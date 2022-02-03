package cn.edu.sysu.workflow.activiti.bufferFIFO;

import cn.edu.sysu.workflow.activiti.admission.timewheel.ActivitiTask;
import cn.edu.sysu.workflow.activiti.admission.timewheel.Timer;
import cn.edu.sysu.workflow.activiti.admission.timewheel.TimerTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author wuyunzhi
 * @create 2022-01-09 18:44
 */
public class Buffer {
    private static Logger logger = LoggerFactory.getLogger(Buffer.class);

    ConcurrentLinkedQueue<ActivitiTask> bufferPool;
    private static Buffer BUFFER_INSTANCE;
    private ScheduledExecutorService customerThreadPool;
    private ExecutorService workerThreadPool;
    private static final int REQUST_THRESHOLD = 50;

    public static Buffer getInstance() {
        if (BUFFER_INSTANCE == null) {
            synchronized (Buffer.class) {
                if (BUFFER_INSTANCE == null) {
                    BUFFER_INSTANCE = new Buffer();
                }
            }
        }
        return BUFFER_INSTANCE;
    }

    private Buffer() {
        bufferPool = new ConcurrentLinkedQueue<>();
        workerThreadPool = Executors.newFixedThreadPool(35,
            new ThreadFactoryBuilder().setPriority(10).setNameFormat("submitTaskWorker").build());
//        workerThreadPool=Executors.newCachedThreadPool();
        customerThreadPool = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("submitTaskBoss").build());
        customerThreadPool.scheduleAtFixedRate(()->{
            Buffer.BUFFER_INSTANCE.consumeTaskEverySecond();
        }, 0, 1, TimeUnit.SECONDS);
    }

    void consumeTaskEverySecond(){
//        无阈值
//        ActivitiTask activitiTask=bufferPool.poll();
//        while (activitiTask!=null){
//            logger.debug("提交一个activitiTask");
//            workerThreadPool.submit(activitiTask);
//            activitiTask=bufferPool.poll();
//        }

        for (int i = 0; i < REQUST_THRESHOLD; i++) {
            ActivitiTask activitiTask=bufferPool.poll();
            if (activitiTask==null){
                break;
            }
            else{
                logger.debug("提交一个activitiTask");
                workerThreadPool.submit(activitiTask);
            }
        }
    }

    public void produce(ActivitiTask activitiTask) {
        logger.debug("生产一个activitiTask");
        bufferPool.offer(activitiTask);

    }
}
