package cn.edu.sysu.workflow.activiti.admission.timewheel;

import com.sun.media.jfxmedia.logging.Logger;
import javafx.scene.input.InputMethodTextRun;
import org.slf4j.LoggerFactory;

import javax.xml.ws.EndpointReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 时间槽
 *
 * @author: Gordan Lin
 * @create: 2019/12/12
 **/
public class BucketWithTenantQueue {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(BucketWithTenantQueue.class);

    // 当前槽的过期时间
    private AtomicLong expiration = new AtomicLong(-1L);

    //key=getTenantId value=任务列表
    private HashMap<String, CopyOnWriteArrayList<TimerTask>> hashMap = new HashMap<>();

    public BucketWithTenantQueue() {

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
        String tenantId = timerTask.getTenantId();
        if (hashMap.containsKey(tenantId)) {
            hashMap.get(tenantId).add(timerTask);

        } else {
            CopyOnWriteArrayList<TimerTask> taskList = new CopyOnWriteArrayList<>();
            taskList.add(timerTask);
            hashMap.put(tenantId, taskList);
        }
    }

    /**
     * 查询时间槽任务数
     *
     * @return
     */
    public int getTaskNum() {
        int taskNum = 0;
        for (CopyOnWriteArrayList<TimerTask> timerTasks : hashMap.values()) {
            taskNum = taskNum + timerTasks.size();
        }
        return taskNum;
    }

    /**
     * 删除任务
     *
     * @param count
     * @return
     */
    public synchronized List<TimerTask> removeTaskAndGet(int count) {
        List<TimerTask> rtnList = new CopyOnWriteArrayList<>();
        if (getTaskNum() == 0) {
            return rtnList;
        }
        // 全部返回
        if (count == -1) {
            for (CopyOnWriteArrayList<TimerTask> copyOnWriteArrayList : hashMap.values()) {
                rtnList.addAll(copyOnWriteArrayList);
                copyOnWriteArrayList.clear();
            }
        } else {
            int tenantTaskCount = count / hashMap.size();
            int remainCount = 0;
            for (CopyOnWriteArrayList<TimerTask> copyOnWriteArrayList : hashMap.values()) {
                int removeCount = 0;
                Iterator<TimerTask> iterator = copyOnWriteArrayList.iterator();
                while (iterator.hasNext()) {
                    if (removeCount >= tenantTaskCount) {
                        break;
                    }
                    removeCount++;
                    TimerTask timerTask = iterator.next();
                    rtnList.add(timerTask);
                    copyOnWriteArrayList.remove(timerTask);
                }
                remainCount = remainCount + tenantTaskCount - removeCount;
            }
            //每个队列取count均值后加总不够count
            if (remainCount != 0) {
                for (CopyOnWriteArrayList<TimerTask> copyOnWriteArrayList : hashMap.values()) {
                    if (remainCount <= 0) {
                        break;
                    }
                    Iterator<TimerTask> iterator = copyOnWriteArrayList.iterator();
                    while (iterator.hasNext()) {
                        if (remainCount <= 0) {
                            break;
                        }
                        remainCount--;
                        TimerTask timerTask = iterator.next();
                        rtnList.add(timerTask);
                        copyOnWriteArrayList.remove(timerTask);
                    }
                }
            }
        }
        return rtnList;
    }
}
