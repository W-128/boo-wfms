package cn.edu.sysu.workflow.activiti.admission.timewheel.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wuyunzhi
 * @create 2021-12-06 19:59
 */
public class TestTask implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(TestTask.class);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;


    public TestTask(String name){
        this.name=name;
    }
    public TestTask(){
    }
    @Override public void run(){
        logger.debug(name+"提交执行");
    }
}
