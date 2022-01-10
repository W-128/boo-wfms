package cn.edu.sysu.workflow.activiti.bufferFIFO;

import cn.edu.sysu.workflow.activiti.admission.timewheel.ActivitiTask;
import cn.edu.sysu.workflow.activiti.admission.timewheel.Timer;
import cn.edu.sysu.workflow.activiti.admission.timewheel.TimerTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author wuyunzhi
 * @create 2022-01-09 18:44
 */
public class Buffer {
    ConcurrentLinkedQueue<ActivitiTaskInBuffer> bufferPool;
    private static Buffer BUFFER_INSTANCE;
    private ScheduledExecutorService customerThreadPool;
    private static final int REQUST_THRESHOLD = 60;

    public static Buffer getInstance() {
        if (BUFFER_INSTANCE == null) {
            synchronized (Timer.class) {
                if (BUFFER_INSTANCE == null) {
                    BUFFER_INSTANCE = new Buffer();
                }
            }
        }
        return BUFFER_INSTANCE;
    }

    private Buffer() {
        bufferPool = new ConcurrentLinkedQueue<>();
        customerThreadPool = new ScheduledThreadPoolExecutor(REQUST_THRESHOLD,
            new ThreadFactoryBuilder().setNameFormat("customerThread").build());
        customerThreadPool.scheduleAtFixedRate(new Consumer(), 0,1, TimeUnit.SECONDS);
    }

    public ActivitiTaskInBuffer consume() {
        return bufferPool.remove();
    }

    public void submitTask() {

    }
}
