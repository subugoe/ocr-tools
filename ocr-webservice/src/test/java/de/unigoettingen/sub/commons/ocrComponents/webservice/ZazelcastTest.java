package de.unigoettingen.sub.commons.ocrComponents.webservice;



import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;


import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;


import static java.lang.Thread.sleep;

public class ZazelcastTest {
	final static Logger logger = LoggerFactory.getLogger(HazelcastTest.class);
	static final Comparator<Item> ORDER = new ItemComparator();


	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHazelcast() throws InterruptedException{
		System.out.println("------------test2");

		HazelcastInstance h = Hazelcast.newHazelcastInstance(null);
	
        PriorityQueue<Item> q = new PriorityQueue<Item>(100, ORDER);
        BlockingQueue<Item> queue = h.getQueue("default");
        Queue<Item> listqueue = new LinkedList<Item>(); 

        for(int i=1 ; i < 5; i++){
        	 sleep(2000);
        	 Item a = new Item(1, new Date().getTime(), "I" +i );
             listqueue.offer(a);
             queue.add(a);
             sleep(10);
             a = new Item(2, new Date().getTime(), "J"+i );
             listqueue.offer(a);
             queue.add(a);
             sleep(10);
             a = new Item(1, new Date().getTime(), "K"+i);
             listqueue.offer(a);
             queue.add(a);
             sleep(10);
             a = new Item(4, new Date().getTime(), "L"+i );
             listqueue.offer(a);
             queue.add(a);
             sleep(10);
             a = new Item(5, new Date().getTime(), "M" +i);
             listqueue.offer(a);
             queue.add(a);       	
        }
            		
        JOptionPane.showMessageDialog(null, "Start Hazelcast");
		q.addAll(queue);
		//sleep(500);
		
		System.out.println("++++++ B Start PriorityQueue SIZE +++++ "+ q.size());

		while (listqueue.size() > 0) {
			Item i = q.poll();
			if(i != null){
				if(i.getValue().equals("I1") || i.getValue().equals("K1")|| i.getValue().equals("L1")|| i.getValue().equals("M1")|| i.getValue().equals("J1") || 
						i.getValue().equals("I2") || i.getValue().equals("K2")|| i.getValue().equals("L2")|| i.getValue().equals("M2")|| i.getValue().equals("J2")||
						i.getValue().equals("I3") || i.getValue().equals("K3")|| i.getValue().equals("L3")|| i.getValue().equals("M3")|| i.getValue().equals("J3")||
						i.getValue().equals("I4") || i.getValue().equals("K4")|| i.getValue().equals("L4")|| i.getValue().equals("M4")|| i.getValue().equals("J4")){
					sleep(300);
					System.out.println("#### B SORTED #### " + i.getPrio() + " "
							+ i.getTime() + " " + i.getValue());
					listqueue.poll();
					//queue.remove(i);				
				}
			}
		}
		System.out.println("++++++ B Q Prioritie +++++ "+ q.size());
		System.out.println("++++++ B QUEUE Hazelcast+++++ "+ queue.size());
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
}
