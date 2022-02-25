package cn.edu.sysu.workflow.activiti.admission.timewheel;

import org.springframework.http.ResponseEntity;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 需要延迟执行的任务，放在Bucket里
 *
 * @author: Gordan Lin
 * @create: 2019/12/12
 **/
public class TimerTask implements Comparable<TimerTask> {

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    // 延迟时间
    private long delayMs;

    // 任务
    private FutureTask futureTask;

    //入缓存队列时才会用到expirationMs
    private long expirationMs;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    private String tenantId;

    public int getRtl() {
        return rtl;
    }

    public void setRtl(int rtl) {
        this.rtl = rtl;
    }

    private int rtl;

    public TimerTask(long delayMs, FutureTask futureTask, int rtl, String tenantId) {
        this.delayMs = delayMs;
        this.futureTask = futureTask;
        this.rtl = rtl;
        this.tenantId = tenantId;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public FutureTask getTask() {
        return futureTask;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    @Override public int compareTo(TimerTask timerTask) {
        return this.expirationMs > timerTask.getExpirationMs() ? 1 :
            (this.expirationMs < timerTask.getExpirationMs() ? -1 : 0);
    }
}
