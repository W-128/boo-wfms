package cn.edu.sysu.workflow.activiti;

import org.hibernate.query.criteria.internal.compile.CriteriaQueryTypeQueryAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@RunWith(SpringJUnit4ClassRunner.class) @SpringBootTest public class BooProcessExecutionApplicationTests {

    @Test public void contextLoads() {

    }

    @Test public void testPriorityQueue() {
        int a = 1;
        int b = 2;
        int c = 3;
        PriorityQueue<Integer> queue = new PriorityQueue<>();
        queue.offer(a);
        queue.offer(b);
        queue.offer(c);
        c = 4;
        queue.offer(c);
        System.out.println(queue.size());

    }

    @Test public void testCopyOnWriteArrayList() {
        CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        if (copyOnWriteArrayList.get(1) == null) {
            System.out.println(1);
        }
    }
}
