package cn.edu.sysu.workflow.activiti.admission.timewheel;

import com.netflix.loadbalancer.RandomRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 引擎执行的任务
 *
 * @author: Gordan Lin
 * @create: 2019/12/13
 **/
public class ActivitiTask implements Callable<ResponseEntity<String>> {

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

    @Override public ResponseEntity<String> call() {
        long waitEndTime = System.currentTimeMillis();
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        String body = result.getBody();
        List<String> list = Arrays.asList(body.substring(1, body.length() - 1).split(","));
        HashMap<String, String> bodys = new HashMap<>();
        for (String s : list) {
            String key = Arrays.asList(s.split(":")).get(0);
            key = key.substring(1, key.length() - 1);
            String value = Arrays.asList(s.split(":")).get(1);
            value = value.substring(1, value.length() - 1);
            bodys.put(key, value);
        }
        long end = System.currentTimeMillis();
        int rtl = (Integer)variables.get("rtl").get(0);
        //logger.info("activiti engine response time: " + (end - waitEndTime) + "ms");
        logger.info("rtllevel:" + rtl + " request response time: " + (end - this.startTime) + "ms");
        logger.info(
            "processInstanceId: " + bodys.get("processInstanceId") + " taskName: " + bodys.get("taskName") + " start: " + this.startTime + " end: " + end);
        return result;
    }

}
