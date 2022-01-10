package cn.edu.sysu.workflow.activiti.admission.timewheel.test;

import cn.edu.sysu.workflow.activiti.admission.timewheel.TimerTask;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author wuyunzhi
 * @create 2021-12-29 21:55
 */
public class PriorityBlockingQueueTest {
    public static void main(String[] args) {
        Person person1 = new Person(10, 5);
        Person person2 = new Person(50, 1);
        PriorityBlockingQueue<Person> priorityBlockingQueue = new PriorityBlockingQueue();
        priorityBlockingQueue.add(person2);
        priorityBlockingQueue.add(person1);
        for (int i = 0; i < 2; i++) {
            System.out.println(priorityBlockingQueue.poll().age);
        }
        PriorityBlockingQueue<Person> priorityBlockingQueueWithMoney =
            new PriorityBlockingQueue(2, new Comparator<Person>() {
                @Override public int compare(Person person1, Person person2) {
                    return (int)(person2.money - person1.money);
                }

            });
        priorityBlockingQueueWithMoney.add(person1);
        priorityBlockingQueueWithMoney.add(person2);
        for (int i = 0; i < 2; i++) {
            System.out.println(priorityBlockingQueueWithMoney.poll().age);
        }
    }

    public static class Person implements Comparable<Person> {
        public Person(int age, int money) {
            this.age = age;
            this.money = money;
        }

        public int age;
        public int money;

        //类内实现的Compare根据age 从小到大排列
        @Override public int compareTo(Person person) {
            return this.age > person.age ? 1 : (this.age < person.age ? -1 : 0);
        }
    }
}
