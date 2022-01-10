package cn.edu.sysu.workflow.activiti.bufferFIFO;

import org.springframework.http.ResponseEntity;

/**
 * @author wuyunzhi
 * @create 2022-01-10 20:20
 */
public class Consumer implements Runnable {
    @Override public void run() {
        ActivitiTaskInBuffer activitiTaskInBuffer = Buffer.getInstance().consume();
//        if (activitiTaskInBuffer != null) {
//            try {
//                long waitEndTime = System.currentTimeMillis();
//                ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
//                long end = System.currentTimeMillis();
//                int rtl = (Integer)variables.get("rtl").get(0);
//                logger.info("activiti engine response time: " + (end - waitEndTime) + "ms");
//                logger.info("rtllevel:" + rtl + " request response time: " + (end - this.startTime) + "ms");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
}

