package cn.edu.sysu.workflow.activiti;

import javax.sound.midi.Soundbank;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author wuyunzhi
 * @create 2022-02-03 20:14
 */
public class FutureTaskTest {
    public static void main(String[] args) {
        ActivitiTask activitiTask=new ActivitiTask();
        FutureTask<String> futureTask=new FutureTask<>(activitiTask);
        Thread thread=new Thread(futureTask);
        thread.start();
        try {
            // 5. 调用get()方法获取任务结果,如果任务没有执行完成则阻塞等待
            String str = futureTask.get();
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("main Thread end");


    }
    public static class ActivitiTask implements Callable<String>{
        @Override
        public String call(){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ("activitiTask complete");
        }
    }
}
