package cn.edu.sysu.workflow.activiti.service.impl;

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

    @Test public void completeTaskWithFIFOBuffer() {
        HashMap<String, Object> variable = new HashMap<>();
        variable.put("rtl", 1);
        for (int i = 0; i < 10; i++) {
            processService.completeTaskWithFIFOBuffer(i+"", variable);

        }
        try {
            Thread.sleep( 10000 );
        } catch (Exception e){
            System.exit( 0 ); //退出程序
        }
    }
    @Test public void restTemplateTest(){
        HashMap<String, Object> variable = new HashMap<>();
        variable.put("rtl", 1);
        processService.completeTask("1",variable);
        processService.completeTaskWithFIFOBuffer("2", variable);
        try {
            Thread.sleep( 1000 );
        } catch (Exception e){
            System.exit( 0 ); //退出程序
        }
    }
}