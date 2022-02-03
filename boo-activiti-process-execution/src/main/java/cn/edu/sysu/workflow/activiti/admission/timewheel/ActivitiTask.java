package cn.edu.sysu.workflow.activiti.admission.timewheel;

import com.netflix.loadbalancer.RandomRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 引擎执行的任务
 *
 * @author: Gordan Lin
 * @create: 2019/12/13
 **/
public class ActivitiTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ActivitiTask.class);

    private String url;

    private MultiValueMap<String, Object> variables;

    private RestTemplate restTemplate;

    private String processInstanceId;

    private String taskName;

    private long startTime;


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public ActivitiTask(String url) {
        this.url = url;
    }

    public ActivitiTask(String url, MultiValueMap<String, Object> variables, RestTemplate restTemplate,
        String processInstanceId, String taskName, long startTime) {
        this.url = url;
        this.variables = variables;
        this.restTemplate = restTemplate;
        this.processInstanceId = processInstanceId;
        this.taskName = taskName;
        this.startTime = startTime;
    }


    public ActivitiTask(String url, MultiValueMap<String, Object> variables, RestTemplate restTemplate,
        long startTime) {
        this.url = url;
        this.variables = variables;
        this.restTemplate = restTemplate;
        this.startTime = startTime;
    }

    @Override public void run() {
        try {
            long waitEndTime = System.currentTimeMillis();
            logger.debug("activitiTask里restTemplate实例" + restTemplate.toString());
            ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
            long end = System.currentTimeMillis();
            int rtl = (Integer)variables.get("rtl").get(0);
            //logger.info("activiti engine response time: " + (end - waitEndTime) + "ms");
            logger.info("rtllevel:" + rtl + " request response time: " + (end - this.startTime) + "ms");
            logger.info(
                "processInstanceId: " + processInstanceId + " taskName: " + taskName + " start: " + startTime + " end: " + end);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
