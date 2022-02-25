package cn.edu.sysu.workflow.activiti.util;

import cn.edu.sysu.workflow.activiti.admission.timewheel.Timer;

import java.util.HashMap;

/**
 * @author wuyunzhi
 * @create 2022-02-10 18:02
 */
public class NextTaskArriveTimeIntervalUtil {
    private static HashMap<String, Integer> nextTaskArriveTimeInterval = new HashMap<>();

    //任务一返回10s,任务二返回0s
    public  int getNextTaskArriveTimeInterval(String taskName) {
        return nextTaskArriveTimeInterval.get(taskName);
    }
    private static NextTaskArriveTimeIntervalUtil UTIL_INSTANCE;

    public static NextTaskArriveTimeIntervalUtil getInstance() {
        if (UTIL_INSTANCE == null) {
            synchronized (NextTaskArriveTimeIntervalUtil.class) {
                if (UTIL_INSTANCE == null) {
                    UTIL_INSTANCE = new NextTaskArriveTimeIntervalUtil();
                }
            }
        }
        return UTIL_INSTANCE;
    }

    private NextTaskArriveTimeIntervalUtil(){
        nextTaskArriveTimeInterval.put("choose goods", 10);
        nextTaskArriveTimeInterval.put("pay", 0);
    }
}
