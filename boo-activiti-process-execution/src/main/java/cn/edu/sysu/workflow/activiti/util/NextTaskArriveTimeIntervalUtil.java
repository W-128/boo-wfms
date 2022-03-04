package cn.edu.sysu.workflow.activiti.util;

import cn.edu.sysu.workflow.activiti.admission.timewheel.Timer;

import java.util.HashMap;

/**
 * @author wuyunzhi
 * @create 2022-02-10 18:02
 */
public class NextTaskArriveTimeIntervalUtil {

    private static HashMap<String, HashMap<String, Integer>> nextTaskArriveTimeInterval = new HashMap<>();

    public int getNextTaskArriveTimeInterval(String processDefinitionId, String taskName) {
        return nextTaskArriveTimeInterval.get(processDefinitionId).get(taskName);
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

    private NextTaskArriveTimeIntervalUtil() {
        HashMap<String, Integer> online_shopping_two_task_hashMap = new HashMap<>();
        online_shopping_two_task_hashMap.put("choose goods", 10);
        online_shopping_two_task_hashMap.put("pay", 0);
        nextTaskArriveTimeInterval.put("online-shopping-two-task", online_shopping_two_task_hashMap);

        HashMap<String, Integer> online_shopping_five_task_hashMap = new HashMap<>();
        online_shopping_five_task_hashMap.put("choose goods", 10);
        online_shopping_five_task_hashMap.put("pay", 10);
        online_shopping_five_task_hashMap.put("send goods", 10);
        online_shopping_five_task_hashMap.put("receive goods", 10);
        online_shopping_five_task_hashMap.put("commit comments", 0);
        nextTaskArriveTimeInterval.put("online-shopping-five-task", online_shopping_five_task_hashMap);

    }
}
