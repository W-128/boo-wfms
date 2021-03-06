package cn.edu.sysu.workflow.activiti;


import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BooActivitiEngineApplicationTests {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void initDeploy() {
        String r1 = "processes/online-shopping.bpmn20.xml";
        for (int i = 0; i < 1; i++) {
            repositoryService.createDeployment().addClasspathResource(r1).deploy();
        }
    }

    @Test
    public void getProcessDefinitionId() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionKey("online-shopping").list();
        for (ProcessDefinition p : list) {
            System.out.println(p.getId());
        }
    }

    @Test
    public void contextDeploy() {
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println(count);

        String r1 = "processes/leave.bpmn20.xml";
        String r2 = "processes/travel-booking.bpmn20.xml";

        long startTime1 = System.currentTimeMillis();
        repositoryService.createDeployment().addClasspathResource(r2).deploy();
        long endTime1 = System.currentTimeMillis();
        System.out.println("first: "  + (endTime1 - startTime1));

        long startTime2 = System.currentTimeMillis();
        repositoryService.createDeployment().addClasspathResource(r1).deploy();
        long endTime2 = System.currentTimeMillis();
        System.out.println("second: " + (endTime2 - startTime2));

        long startTime3 = System.currentTimeMillis();
        repositoryService.createDeployment().addClasspathResource(r2).deploy();
        long endTime3 = System.currentTimeMillis();
        System.out.println("third:" + (endTime3 - startTime3));

        long startTime4 = System.currentTimeMillis();
        repositoryService.createDeployment().addClasspathResource(r1).deploy();
        long endTime4 = System.currentTimeMillis();
        System.out.println("fourth: " + (endTime4 - startTime4));
    }


    @Test
    public void contextStart() {
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println(count);

        //????????????leave
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("apply", "zhangsan");
        variables.put("approve", "lisi");
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", variables);
        endTime = System.currentTimeMillis();
        System.out.println("start: " + (endTime - startTime));

        //?????????????????????
        String processId = processInstance.getId();
        Task task1 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        startTime = System.currentTimeMillis();
        taskService.complete(task1.getId(), variables);
        endTime = System.currentTimeMillis();
        System.out.println("complete: " + (endTime - startTime));

        //?????????????????????
        Task task2 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        variables.put("pass", true);
        startTime = System.currentTimeMillis();
        taskService.complete(task2.getId(), variables);
        endTime = System.currentTimeMillis();
        System.out.print("complete: " + (endTime - startTime));

        System.out.println("????????????" + historyService.createHistoricProcessInstanceQuery().finished().count());
    }

    /**
     * ?????????????????????????????????????????????
     */
    @Test
    public void testSingleTaskCost() {
        //????????????leave
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("apply", "zhangsan");
        variables.put("approve", "lisi");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", variables);
        ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
        System.out.println(executionEntity.getActivity());

        //?????????????????????
        String processId = processInstance.getId();
        System.out.println("processId: " + processId);
        Task task1 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("task1_Id: " + task1.getId());
        System.out.println("task1_Name: " + task1.getName());
        taskService.complete(task1.getId(), variables);

        //?????????????????????
        Task task2 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        variables.put("pass", 1);
        taskService.complete(task2.getId(), variables);
        System.out.println("????????????" + historyService.createHistoricProcessInstanceQuery().finished().count());
    }

    @Test
    public void testLeave() {
        //????????????????????????processes?????????????????????
        System.out.println("ok>>");
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println(count);

        //????????????leave
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("apply", "zhangsan");
        variables.put("approve", "lisi");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", variables);
        ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("leave", variables);
        ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
        ExecutionEntity executionEntity1 = (ExecutionEntity) processInstance1;
        System.out.println(executionEntity.getActivity());
        System.out.println(executionEntity1.getActivity());

        if(executionEntity.getActivity() == executionEntity1.getActivity()) {
            System.out.println("equal");
        } else {
            System.out.println("no equal");
        }

        //?????????????????????
        String processId = processInstance.getId();
        System.out.println("processId: " + processId);
        Task task1 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("task1_Id: " + task1.getId());
        System.out.println("task1_Name: " + task1.getName());
        taskService.complete(task1.getId(), variables);

        //?????????????????????
        Task task2 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        variables.put("pass", 1);
        taskService.complete(task2.getId(), variables);


        System.out.println("????????????" + historyService.createHistoricProcessInstanceQuery().finished().count());
    }


    //?????????proceddDefinitionEntity??????????????????????????????????????????????????????
    @Test
    public void testRecoverProcessDefinitionEntityTime() {
        //?????????????????????????????????
//        String modelTwo = "processes/2_model.bpmn20.xml";
//        DeploymentBuilder builder1 = repositoryService.createDeployment();
//        builder1.addClasspathResource(modelTwo);
//        builder1.deploy();
//        ProcessInstance pi = runtimeService.startProcessInstanceByKey("load-application");

        System.out.println("before recover");
        long startTime = System.currentTimeMillis();
        initDeploy();
//        ProcessInstance pi1 = runtimeService.startProcessInstanceById("online-shopping:1:4");
        ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("online-shopping");
        long endTime = System.currentTimeMillis();
        System.out.println("recover: " + (endTime - startTime));
        System.out.println("processDefinitionId:" + pi1.getProcessDefinitionId());

        System.out.println("after recover");
        //???????????????????????????????????????procssDefinition
        long startTime1 = System.currentTimeMillis();
        //ProcessInstance pi2 = runtimeService.startProcessInstanceById("online-shopping:1:4");
        ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("online-shopping");
        long endTime1 = System.currentTimeMillis();
        System.out.println("no recover: " + (endTime1 - startTime1));
        System.out.println("processDefinitionId:" + pi2.getProcessDefinitionId());

        long startTime2 = System.currentTimeMillis();
        ProcessInstance pi3 = runtimeService.startProcessInstanceByKey("online-shopping");
        long endTime2 = System.currentTimeMillis();
        System.out.println("no recover: " + (endTime2 - startTime2));//?????????????????????296ms?????????35?????????????????????????????????????????????????????????
    }

    @Test
    public void testTravelBooking() {
        //?????????????????????
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println(count);
        count = 0;

        //????????????
        String traveler = "Mike";
        String hotel = "1";
        String flight = "0";
        String car = "1";

        //????????????:
        Map<String, Object> variables = new HashMap<String, Object>();
        Map<String, Object> subVariables = new HashMap<String, Object>();

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("travel-booking", variables);
        System.out.println(pi);

        //??????????????????register
        Task registerTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println(registerTask.getName());
        // -- traveler????????????
        taskService.claim(registerTask.getId(), traveler);
        System.out.println("taskId:" + registerTask.getId());
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler????????????
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        //???????????????
        // -- ????????????????????????register
         Task registerItineraryTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        // -- -- traveler ????????????
        taskService.claim(registerItineraryTask.getId(), traveler);
        // -- -- ????????????
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        subVariables.put("hotel", hotel);
        subVariables.put("car", car);
        subVariables.put("flight", flight);
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId(), subVariables);
        }
        // -- ?????????????????????book
        tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        System.out.println("??????????????????????????????" + tasks.size());
        // -- -- ?????????????????????????????????
        for(Task task : tasks) {
            System.out.println("Task for " + traveler + ": " + task.getName());
            taskService.claim(task.getId(), traveler);
        }
        // -- -- ?????????????????????
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler????????????
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        // -- ???????????????????????????prepare pay
        Task preparePayTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        // -- -- ??????prepare pay
        taskService.claim(preparePayTask.getId(), traveler);
        // -- -- ??????prepare pay
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler????????????
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        //???????????????pay??????
        Task payTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        // -- -- ??????prepare pay
        taskService.claim(payTask.getId(), traveler);
        // -- -- ??????prepare pay
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler????????????
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        //??????????????????
        System.out.println(historyService.createHistoricProcessInstanceQuery().finished().count());
    }

    @Test public void testAllActiveTask(){
        String r1 = "processes/online-shopping-five-task.bpmn20.xml";

        repositoryService.createDeployment().addClasspathResource(r1).deploy();

        runtimeService.startProcessInstanceByKey("online-shopping-five-task");
        runtimeService.startProcessInstanceByKey("online-shopping-five-task");

        List<Task> activeTask =taskService.createTaskQuery().active().list();
        for (Task task : activeTask) {
            System.out.println(task.getName());
        }


    }

    @Test public void executeTask(){
        List<Task> activeTask =taskService.createTaskQuery().active().list();
        for (Task task : activeTask) {
            System.out.println(task.getName());
        }
        for (Task task : activeTask) {
            taskService.complete(task.getId());
        }
        activeTask =taskService.createTaskQuery().active().list();
        for (Task task : activeTask) {
            System.out.println(task.getName());
        }

    }

}
