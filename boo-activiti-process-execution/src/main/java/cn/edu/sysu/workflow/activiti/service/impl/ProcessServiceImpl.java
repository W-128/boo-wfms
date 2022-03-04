package cn.edu.sysu.workflow.activiti.service.impl;

import cn.edu.sysu.workflow.activiti.admission.limiter.SLALimit;
import cn.edu.sysu.workflow.activiti.admission.timewheel.ActivitiTask;
import cn.edu.sysu.workflow.activiti.admission.timewheel.Timer;
import cn.edu.sysu.workflow.activiti.admission.timewheel.TimerTask;
import cn.edu.sysu.workflow.activiti.bufferFIFO.Buffer;
import cn.edu.sysu.workflow.activiti.util.CommonUtil;
import org.activiti.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import cn.edu.sysu.workflow.activiti.service.ProcessService;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.activiti.engine.task.Task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

@Service public class ProcessServiceImpl implements ProcessService, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(ProcessService.class);

    @Autowired RestTemplate restTemplate;

    private final String URL_PREFIX = "http://activiti-engine/activiti-engine";

    private final String QUERY_START_URL_PREFIX = "http://activiti-engine-query-start/activiti-engine-query-start";

    @Override public void afterPropertiesSet() throws Exception {
        Timer.getInstance();
    }

    @Override
    public ResponseEntity<?> startProcessInstanceByKey(String processModelKey, Map<String, Object> variables) {
        String url = QUERY_START_URL_PREFIX + "/startProcessInstanceByKey/" + processModelKey;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    @Override
    public ResponseEntity<?> startProcessInstanceById(String processInstanceId, Map<String, Object> variables) {
        String url = URL_PREFIX + "/startProcessInstanceById/" + processInstanceId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    @Override public ResponseEntity<?> getCurrentTasks(String processInstanceId) {
        String url = URL_PREFIX + "/getCurrentTasks/" + processInstanceId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        return result;
    }

    @Override public ResponseEntity<?> getCurrentTasks(String processInstancedId, Map<String, Object> variables) {
        String url = URL_PREFIX + "/getCurrentTasksOfAssignee/" + processInstancedId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class, valueMap);
        return result;
    }

    @Override public ResponseEntity<?> getCurrentSingleTask(String processInstanceId) {
        String url = QUERY_START_URL_PREFIX + "/getCurrentSingleTask/" + processInstanceId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        return result;
    }

    @Override public ResponseEntity<?> claimTask(String processInstanceId, String taskId, String assignee) {
        String url = URL_PREFIX + "/claimTask/" + processInstanceId + "/" + taskId;
        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee", assignee);
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.postForEntity(url, valueMap, String.class);
        return result;
    }

    @Override public ResponseEntity<?> isEnded(String processInstanceId) {
        String url = URL_PREFIX + "/isEnded/" + processInstanceId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        return result;
    }

    // 不延迟
    @Override public ResponseEntity<?> completeTask(String taskId, Map<String, String> variables) {
        long start = System.currentTimeMillis();
        int rtl = Integer.parseInt(variables.get("rtl"));
        String url = URL_PREFIX + "/completeTask/" + taskId;
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        String[] strings = getProcessInstanceIdAndTaskName(result);
        long end = System.currentTimeMillis();
        logger.info("rtllevel:" + rtl + " completeTask request response time: " + (end - start) + "ms");
        logger.info(
            "processInstanceId: " + strings[0] + " taskName: " + strings[1] + " start: " + start + " end: " + end);
        return result;
    }

    @Override public ResponseEntity<?> completeTaskWithFIFOBuffer(String taskId, Map<String, String> variables) {
        long start = System.currentTimeMillis();
        int rtl = Integer.parseInt(variables.get("rtl"));
        String url = URL_PREFIX + "/completeTask/" + taskId;
        ActivitiTask activitiTask = new ActivitiTask(url, restTemplate, start, rtl);
        FutureTask<ResponseEntity<String>> futureTask = new FutureTask<>(activitiTask);
        Buffer.getInstance().produce(futureTask);
        ResponseEntity<String> result = null;
        try {
            result = futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    @Override public ResponseEntity<?> completeTaskWithDelay(String taskId, Map<String, String> variables) {
        //获取租户SLA级别定义
        int rtl = Integer.parseInt(variables.get("rtl"));
        String tenantId = variables.get("tenantId");
        long start = System.currentTimeMillis();
        String url = URL_PREFIX + "/completeTask/" + taskId;
        ActivitiTask activitiTask = new ActivitiTask(url, restTemplate, start, rtl);
        activitiTask.setStartTime(start);
        FutureTask<ResponseEntity<String>> futureTask = new FutureTask<>(activitiTask);
        TimerTask timerTask = new TimerTask(rtl * SLALimit.RESPONSE_TIME_PER_LEVEL, futureTask, rtl, tenantId);
        Timer.getInstance().addTask(timerTask);
        ResponseEntity<String> result = null;
        try {
            result = futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
        return result;
    }

    public String[] getProcessInstanceIdAndTaskName(ResponseEntity<String> result) {
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
        return new String[] {bodys.get("processInstanceId"), bodys.get("taskName")};
    }
}