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

import java.util.HashMap;
import java.util.Map;

@Service public class ProcessServiceImpl implements ProcessService, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(ProcessService.class);

    @Autowired RestTemplate restTemplate;

    @Autowired TaskService taskService;

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
    @Override public ResponseEntity<?> completeTask(String taskId, String processDefinitionId, String processInstanceId,
        Map<String, Object> variables) {
        long start = System.currentTimeMillis();
        int rtl = (Integer)variables.get("rtl");
        String url = URL_PREFIX + "/completeTask/" + taskId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        long end = System.currentTimeMillis();
        logger.info("rtllevel:" + rtl + " completeTask request response time: " + (end - start) + "ms");
        return result;
    }

    public ResponseEntity<?> completeTask(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        String taskName = task.getName();
        long start = System.currentTimeMillis();
        int rtl = (Integer)variables.get("rtl");
        String url = URL_PREFIX + "/completeTask/" + taskId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        logger.debug("completeTask的restTemplate实例" + restTemplate.toString());
        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        long end = System.currentTimeMillis();
        logger.info("rtllevel:" + rtl + " completeTask request response time: " + (end - start) + "ms");
        logger.info(
            "processInstanceId: " + processInstanceId + " taskName: " + taskName + " start: " + start + " end: " + end);
        return result;
    }

    public ResponseEntity<?> completeTaskWithFIFOBuffer(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        String taskName = task.getName();
        long start = System.currentTimeMillis();
        int rtl = (Integer)variables.get("rtl");
        String url = URL_PREFIX + "/completeTask/" + taskId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ActivitiTask activitiTask = new ActivitiTask(url, valueMap, restTemplate, processInstanceId, taskName, start);
        Buffer.getInstance().produce(activitiTask);
        return ResponseEntity.ok("请求已加入FIFO缓冲池");

    }

    //延迟请求
    public ResponseEntity<?> completeTaskWithDelay(String taskId, String processDefinitionId, String processInstanceId,
        Map<String, Object> variables) {
        //获取租户SLA级别定义
        //        int rar = (Integer)variables.get("rar");
        int rtl = (Integer)variables.get("rtl");

        //        RateLimiter limiter = null;
        //        try {
        //            // key要求：tenantId-rarLevel
        //            limiter = SLALimit.requestRateLimiterCaches.get("test-"+rar);
        //        } catch (ExecutionException e) {
        //            e.printStackTrace();
        //        }
        //        if (!limiter.tryAcquire()) {
        //            logger.error("rar："+rar+" rtl："+rtl);
        //            logger.error("流程定义："+processDefinitionId+" 流程实例："+processInstanceId+" 任务："+taskId+" 请求由于限流被拒绝");
        //            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请求由于限流被拒绝");
        //        }
        long start = System.currentTimeMillis();
        String url = URL_PREFIX + "/completeTask/" + taskId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ActivitiTask activitiTask = new ActivitiTask(url, valueMap, restTemplate, start);
        activitiTask.setStartTime(start);
        TimerTask timerTask = new TimerTask(rtl * SLALimit.RESPONSE_TIME_PER_LEVEL, activitiTask, rtl);
        Timer.getInstance().addTask(timerTask);

        return ResponseEntity.ok("请求正在调度中");
    }

    public ResponseEntity<?> completeTaskWithDelay(String taskId, Map<String, Object> variables) {
        //获取租户SLA级别定义
        int rtl = (Integer)variables.get("rtl");

        long start = System.currentTimeMillis();
        String url = URL_PREFIX + "/completeTask/" + taskId;
        MultiValueMap<String, Object> valueMap = CommonUtil.map2MultiValueMap(variables);
        ActivitiTask activitiTask = new ActivitiTask(url, valueMap, restTemplate, start);
        activitiTask.setStartTime(start);
        TimerTask timerTask = new TimerTask(rtl * SLALimit.RESPONSE_TIME_PER_LEVEL, activitiTask, rtl);
        Timer.getInstance().addTask(timerTask);

        return ResponseEntity.ok("请求已加入时间槽");
    }

}