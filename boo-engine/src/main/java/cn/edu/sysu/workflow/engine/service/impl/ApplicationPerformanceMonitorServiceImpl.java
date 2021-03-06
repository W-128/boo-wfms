package cn.edu.sysu.workflow.engine.service.impl;

import cn.edu.sysu.workflow.common.entity.ServiceInfo;
import cn.edu.sysu.workflow.engine.BooEngineApplication;
import cn.edu.sysu.workflow.engine.dao.ServiceInfoDAO;
import cn.edu.sysu.workflow.engine.service.ApplicationPerformanceMonitorService;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.util.Objects;

/**
 * {@link ApplicationPerformanceMonitorService}
 *
 * @author Skye
 * Created on 2019/12/26
 */

@Service
public class ApplicationPerformanceMonitorServiceImpl implements ApplicationPerformanceMonitorService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationPerformanceMonitorServiceImpl.class);

    @Autowired
    private ServiceInfoDAO serviceInfoDAO;

    @Autowired
    private Environment environment;

    @Override
    @PostConstruct
    public void postConstruct() {
        try {
            String url = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + environment.getProperty("server.port");
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceInfoId(BooEngineApplication.ENGINE_ID);
            serviceInfo.setUrl(url);
            serviceInfoDAO.save(serviceInfo);
        } catch (Exception e) {
            log.error("When creating service info, exception occurred, " + e.toString());
        }
    }

    @Override
    @PreDestroy
    public void preDestroy() {
        try {
            serviceInfoDAO.deleteByServiceInfoId(BooEngineApplication.ENGINE_ID);
        } catch (Exception e) {
            log.error("When deleting service info, exception occurred, " + e.toString());
        }
    }

    @Override
    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    public void monitorRunner() {
        try {
            StringBuilder sb = new StringBuilder();

            // CPU Load
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpuValue = operatingSystemMXBean.getProcessCpuLoad();
            sb.append("??????CPU????????????").append(cpuValue * 100).append("% | ");

            // Memory Usage
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
            double memoryResult = heapMemory.getUsed() * 1.0 / heapMemory.getCommitted();
            sb.append("??????Memory????????????").append(memoryResult * 100).append("% | ");

            // Tomcat Threads
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("Tomcat:name=\"http-nio-" + environment.getProperty("server.port") + "\",type=ThreadPool");
            AttributeList list = mBeanServer.getAttributes(name, new String[]{"currentThreadsBusy"});
            Attribute att = (Attribute)list.get(0);
            Integer tomcatValue  = (Integer)att.getValue();
            double maxThreads = Double.parseDouble(Objects.requireNonNull(environment.getProperty("server.tomcat.max-threads")));
            double tomcatResult = tomcatValue / maxThreads;
            sb.append("??????Tomcat????????????").append(tomcatValue);

            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceInfoId(BooEngineApplication.ENGINE_ID);
            serviceInfo.setCpuOccupancyRate(cpuValue);
            serviceInfo.setMemoryOccupancyRate(memoryResult);
            serviceInfo.setTomcatConcurrency(tomcatResult);
            serviceInfo.updateBusiness();
            serviceInfoDAO.update(serviceInfo);
            sb.insert(0, "???????????????????????????" + serviceInfo.getBusiness() + "% | ");

            log.debug(sb.toString());
        } catch (MalformedObjectNameException | InstanceNotFoundException | ReflectionException | NullPointerException e) {
            log.error("When updating service info, exception occurred, " + e.toString());
        }
    }
}
