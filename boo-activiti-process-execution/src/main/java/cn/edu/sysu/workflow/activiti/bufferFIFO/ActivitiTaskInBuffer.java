package cn.edu.sysu.workflow.activiti.bufferFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author wuyunzhi
 * @create 2022-01-10 20:23
 */
public class ActivitiTaskInBuffer {
    private static Logger logger = LoggerFactory.getLogger(ActivitiTaskInBuffer.class);

    private String url;

    private MultiValueMap<String, Object> variables;

    private RestTemplate restTemplate;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    private long startTime;

    public ActivitiTaskInBuffer(String url, MultiValueMap<String, Object> variables, RestTemplate restTemplate) {
        this.url = url;
        this.variables = variables;
        this.restTemplate = restTemplate;
    }
}
