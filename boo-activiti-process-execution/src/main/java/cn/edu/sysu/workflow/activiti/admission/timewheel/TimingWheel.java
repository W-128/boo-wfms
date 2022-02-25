package cn.edu.sysu.workflow.activiti.admission.timewheel;

import cn.edu.sysu.workflow.activiti.admission.timewheel.test.AdmissionTest;
import cn.edu.sysu.workflow.activiti.admission.timewheel.test.TestTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 时间轮
 *
 * @author: Gordan Lin
 * @create: 2019/12/11
 **/
public class TimingWheel {

    // 一个时间槽的时间长度
    private long tickMs;

    // 时间轮大小，即时间槽个数
    private int wheelSize;

    // 槽
    private BucketWithTenantQueue[] buckets;

    private int requestThreshold;

    // 时间轮指针 最开始时=时间轮创建的时间
    private long currentTimestamp;

    private AtomicBoolean isInitial = new AtomicBoolean(true);

    // 对于一个Timer以及附属的时间轮，都只有一个priorityQueue
    private PriorityBlockingQueue<BucketWithTenantQueue> priorityQueue;

    private static Logger logger = LoggerFactory.getLogger(TimingWheel.class);

    public TimingWheel(long tickMs, int wheelSize, long currentTimestamp,
        PriorityBlockingQueue<BucketWithTenantQueue> priorityQueue, int requestThreshold) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        //保证指针是tickMs的整数倍
        this.currentTimestamp = currentTimestamp - (currentTimestamp % tickMs);
        this.buckets = new BucketWithTenantQueue[wheelSize];
        this.priorityQueue = priorityQueue;
        this.requestThreshold = requestThreshold;
        for (int i = 0; i < wheelSize; i++) {
            buckets[i] = new BucketWithTenantQueue();
        }
    }

    /**
     * 添加任务到时间轮
     *
     * @param timerTask
     * @return
     */
    public synchronized boolean addTask(TimerTask timerTask) {
        long delayMs = timerTask.getDelayMs();
        if (delayMs < tickMs) { // 到期了,直接执行
            return false;
        } else {
            int bucketIndex = (int)(((delayMs - tickMs + currentTimestamp) / tickMs) % wheelSize);
            BucketWithTenantQueue bucket = buckets[bucketIndex];
            bucket.addTask(timerTask);
            // 添加到优先队列中
            // 原过期时间不等于现过期时间 是不是意味着转了一轮了
            // 如果没有多转一轮，bucket的过期时间是不会变的，所以每次提交到priorityQueue的时候，都是bucket复用的时候
            if (bucket.setExpire(delayMs + currentTimestamp - (delayMs + currentTimestamp) % tickMs)) {
                logger.debug("bucket[{}] 重设过期时间为:{}", bucketIndex,
                    delayMs + currentTimestamp - (delayMs + currentTimestamp) % tickMs);
                //重设bucket过期时间时，之前被覆盖的bucket中的任务肯定是提交完了，现在只有最新的一个任务
                if (bucket.getTaskNum() != 1) {
                    logger.warn("重设bucket过期时间,bucket中任务数不是一个。目前任务数为" + bucket.getTaskNum());
                }
                priorityQueue.offer(bucket);
        }
        return true;
    }

}

    public void advanceClock(long timestamp) {
        if (timestamp >= currentTimestamp + tickMs) {
            currentTimestamp = timestamp - (timestamp % tickMs);
        }
    }

}
