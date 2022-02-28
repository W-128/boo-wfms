package cn.edu.sysu.workflow.activiti.service.impl;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.hystrix.HystrixCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author wuyunzhi
 * @create 2022-01-14 17:04
 */

@RunWith(SpringRunner.class)
public class ProcessServiceImplTest {



    @TestConfiguration
    static class prepareCustomServices {
        @Bean
        public ProcessServiceImpl getProcessService() {
            return new ProcessServiceImpl();
        }

        @Bean
        public RestTemplate restTemplate(){
            return new RestTemplate();
        }
    }
    @Autowired
    @Qualifier("getProcessService")
    private ProcessServiceImpl processService;



    @Test public void getProcessInstanceIdAndTaskName() {
        HashMap<String, String> response = new HashMap<>();

        response.put("status", "success");
        response.put("taskId", "111");
        response.put("taskName", "pay");
        response.put("processInstanceId","0101");
        ResponseEntity<String> result= ResponseEntity.status(HttpStatus.OK).body(JSON.toJSONString(response));
        String[] strings=processService.getProcessInstanceIdAndTaskName(result);
        System.out.println(strings[0]);
        System.out.println(strings[1]);
    }
}