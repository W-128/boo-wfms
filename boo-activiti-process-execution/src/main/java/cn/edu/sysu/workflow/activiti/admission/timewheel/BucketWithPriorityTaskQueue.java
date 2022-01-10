package cn.edu.sysu.workflow.activiti.admission.timewheel;

import jdk.nashorn.internal.ir.IdentNode;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wuyunzhi
 * @create 2021-12-29 21:46
 */
public class BucketWithPriorityTaskQueue {
    // 当前槽的过期时间
    private AtomicLong expiration = new AtomicLong(-1L);

    private int taskNumSize;

    private PriorityBlockingQueue<TimerTask> taskQueue;

    public BucketWithPriorityTaskQueue(int taskNumSize) {
        this.taskNumSize = taskNumSize;
        //rtl大的优先级高
        this.taskQueue = new PriorityBlockingQueue<>(taskNumSize, new Comparator<TimerTask>() {
            @Override public int compare(TimerTask o1, TimerTask o2) {
                return (int)(o2.getRtl() - o1.getRtl());
            }
        });
    }

    public BucketWithPriorityTaskQueue() {
    }

    /**
     * 设置槽的过期时间
     */
    public boolean setExpire(long expire) {
        return expiration.getAndSet(expire) != expire;
    }

    /**
     * 获取槽的过期时间
     */
    public long getExpire() {
        return expiration.get();
    }

    /**
     * 新增任务到bucket
     *
     * @param timerTask
     */
    public synchronized void addTask(TimerTask timerTask) {
        taskQueue.add(timerTask);
    }

    /**
     * 查询时间槽任务数
     *
     * @return
     */
    public int getTaskNum() {
        return taskQueue.size();
    }

    /**
     * 删除任务
     *
     * @param count
     * @return
     */
    public synchronized List<TimerTask> removeTaskAndGet(int count) {
        List<TimerTask> rtnList = new LinkedList<>();
        //全部返回
        if (count == -1) {
            count = taskQueue.size();
        }
        Iterator<TimerTask> iterator = taskQueue.iterator();
        int n = 0;
        while (iterator.hasNext()) {
            if (n >= count) {
                break;
            }
            n++;
            rtnList.add(iterator.next());
            iterator.remove();
        }
        return rtnList;
    }

    public int getTaskMaxRtl() {
        return taskQueue.peek().getRtl();
    }

    public synchronized TimerTask getTaskQueueTop() {
        return taskQueue.poll();
    }
}
