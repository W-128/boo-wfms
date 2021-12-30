package cn.edu.sysu.workflow.activiti.admission.timewheel.test;

import cn.edu.sysu.workflow.activiti.admission.timewheel.Timer;
import cn.edu.sysu.workflow.activiti.admission.timewheel.TimerTask;
import org.springframework.aop.scope.ScopedProxyUtils;

/**
 * @author wuyunzhi
 * @create 2021-12-28 22:16
 */

public class TimerTaskPriorityTest {
    public static void main(String[] args) {
//        admittingWithThresholdTest();


    }
//    public static void AdvanceClockTest(){
//        //小于阈值 全部提交执行
//        Timer timer=Timer.getInstance();
//        TimerTask timerTask1=new TimerTask(1,new TestTask());
//        timerTask1.setExpirationMs(System.currentTimeMillis()+100);
//
//        TimerTask timerTask2=new TimerTask(1,new TestTask());
//        timerTask2.setExpirationMs(System.currentTimeMillis()+300);
//
//        TimerTask timerTask3=new TimerTask(1,new TestTask());
//        timerTask3.setExpirationMs(System.currentTimeMillis()+200);
//
//        Timer.bufferPriorityQueue.offer(timerTask1);
//        Timer.bufferPriorityQueue.offer(timerTask2);
//        Timer.bufferPriorityQueue.offer(timerTask3);
//    }
//    public static void admittingWithThresholdTest(){
//        TimerTask timerTaskA=new TimerTask(1000,new TestTask("timerTaskA"));
//        TimerTask timerTaskB=new TimerTask(1000,new TestTask("timerTaskB"));
//        TimerTask timerTaskC=new TimerTask(1000,new TestTask("timerTaskC"));
//        TimerTask timerTaskD=new TimerTask(1000,new TestTask("timerTaskD"));
//        TimerTask timerTaskE=new TimerTask(1000,new TestTask("timerTaskE"));
//        TimerTask timerTaskF=new TimerTask(1000,new TestTask("timerTaskF"));
//        TimerTask timerTaskG=new TimerTask(3000,new TestTask("timerTaskG"));
//        TimerTask timerTaskH=new TimerTask(3000,new TestTask("timerTaskH"));
//        TimerTask timerTaskI=new TimerTask(3000,new TestTask("timerTaskI"));
//        Timer timer=Timer.getInstance();
//        timer.addTask(timerTaskA);
//        timer.addTask(timerTaskB);
//        timer.addTask(timerTaskC);
//        timer.addTask(timerTaskD);
//        timer.addTask(timerTaskE);
//        timer.addTask(timerTaskF);
//        timer.addTask(timerTaskG);
//        timer.addTask(timerTaskH);
//        timer.addTask(timerTaskI);
//
//
//
//
//
//
//    }
}
