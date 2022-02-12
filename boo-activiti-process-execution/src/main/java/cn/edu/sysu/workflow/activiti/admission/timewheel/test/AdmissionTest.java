package cn.edu.sysu.workflow.activiti.admission.timewheel.test;
import cn.edu.sysu.workflow.activiti.admission.limiter.SLALimit;
import cn.edu.sysu.workflow.activiti.admission.timewheel.ActivitiTask;
import cn.edu.sysu.workflow.activiti.admission.timewheel.Timer;
import cn.edu.sysu.workflow.activiti.admission.timewheel.TimerTask;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.xml.bind.SchemaOutputResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.IntStream;

/**
 * @author wuyunzhi
 * @create 2021-12-06 19:57
 */
public class AdmissionTest  {
    private static Logger logger = LoggerFactory.getLogger(AdmissionTest.class);
    public static void main(String[] args) {
//        logger.debug("AdmissionTest");
//        TestTask task1=new TestTask("task1");
//        TimerTask timerTask1 = new TimerTask(1000, task1);
//        TestTask task2=new TestTask("task2");
//        TimerTask timerTask2 = new TimerTask(2000, task2);
//        TestTask task3=new TestTask("task3");
//        TimerTask timerTask3 = new TimerTask(2000, task3);
//        TestTask task4=new TestTask("task4");
//        TimerTask timerTask4 = new TimerTask(3000, task4);
//        TestTask task5=new TestTask("task5");
//        TimerTask timerTask5 = new TimerTask(3000, task5);
//        TestTask task6=new TestTask("task6");
//        TimerTask timerTask6 = new TimerTask(3000, task6);
//        TestTask task7=new TestTask("task7");
//        TimerTask timerTask7 = new TimerTask(3000, task7);
//        Timer.getInstance().addTask(timerTask1);
//        Timer.getInstance().addTask(timerTask2);
//        Timer.getInstance().addTask(timerTask3);
//        Timer.getInstance().addTask(timerTask4);
//        Timer.getInstance().addTask(timerTask5);
//        Timer.getInstance().addTask(timerTask6);
//        Timer.getInstance().addTask(timerTask7);
//        Thresholdtest();
//        Delay0RateLimiterTest();
//        ArrayList<Integer> arrayList=new ArrayList<>();
//        arrayList.add(0,1);
//        CopyOnWriteArrayList<Integer> copyOnWriteArrayList=new CopyOnWriteArrayList<>();
//        copyOnWriteArrayList.add(10,1);
//        int a=copyOnWriteArrayList.get(1);
//        System.out.println(a);
        PriorityBlockingQueueTest();


    }

//    public static void Thresholdtest(){
//        //threshold限定为2
//        //每秒启动3 应该有一个被延后t0
//        TestTask task1=new TestTask("task1");
//        TimerTask timerTask1 = new TimerTask(1000, task1);
//        TestTask task2=new TestTask("task2");
//        TimerTask timerTask2 = new TimerTask(1000, task2);
//        TestTask task3=new TestTask("task3");
//        TimerTask timerTask3 = new TimerTask(1000, task3);
//        Timer.getInstance().addTask(timerTask1);
//        Timer.getInstance().addTask(timerTask2);
//        Timer.getInstance().addTask(timerTask3);
//    }
    public static void RateLimiterTest(){
        RateLimiter rateLimiter=RateLimiter.create(5);

        // 积攒1秒
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        IntStream.range(0, 10).forEach(a -> {
            boolean isAcquire=rateLimiter.tryAcquire();
            System.out.println("第" + a + "次请求是否被允许：" + isAcquire);
        });

        // 积攒1秒
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        IntStream.range(0, 10).forEach(a -> {
            boolean isAcquire=rateLimiter.tryAcquire();
            System.out.println("第" + a + "次请求是否被允许：" + isAcquire);
        });

    }
//    public static void Delay0RateLimiterTest() {
//        Timer.getInstance().addTask(new TimerTask(0, new TestTask("task1")));
//        Timer.getInstance().addTask(new TimerTask(0, new TestTask("task2")));
//        Timer.getInstance().addTask(new TimerTask(0, new TestTask("task3")));
//        Timer.getInstance().addTask(new TimerTask(0, new TestTask("task4")));
//        Timer.getInstance().addTask(new TimerTask(0, new TestTask("task5")));
//        Timer.getInstance().addTask(new TimerTask(1000, new TestTask("task6")));
//        Timer.getInstance().addTask(new TimerTask(2000, new TestTask("task7")));
//
//
//
//
//
//
//
//    }


    public static void PriorityBlockingQueueTest() {
        PriorityBlockingQueue<Integer> priorityBlockingQueue = new PriorityBlockingQueue<>();
        Integer integer = priorityBlockingQueue.poll();
        return;
    }
}
