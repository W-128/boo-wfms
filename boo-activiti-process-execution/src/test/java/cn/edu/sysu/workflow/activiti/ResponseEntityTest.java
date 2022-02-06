package cn.edu.sysu.workflow.activiti;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.task.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author wuyunzhi
 * @create 2022-02-05 17:23
 */
public class ResponseEntityTest {
    public static void main(String[] args) {
        HashMap<String, String> response = new HashMap<>();

        response.put("status", "success");
        response.put("taskId", "111");
        response.put("taskName", "pay");
        response.put("processInstanceId","0101");
        ResponseEntity<String> result= ResponseEntity.status(HttpStatus.OK).body(JSON.toJSONString(response));
        String body=result.getBody();
        List<String> list= Arrays.asList(body.substring(1,body.length()-1).split(","));
        HashMap<String,String> response1=new HashMap<>();
        for (String s : list) {
            String key=Arrays.asList(s.split(":")).get(0);
            key=key.substring(1,key.length()-1);
            String value=Arrays.asList(s.split(":")).get(1);
            value=value.substring(1,value.length()-1);
            response1.put(key,value);
        }
        System.out.println(response1.get("processInstanceId"));
    }
}
