package de.unigoettingen.sub.commons.ocrComponents.webservice;


import static java.lang.Thread.sleep;


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






public class HazelcastTest {
	final static Logger logger = LoggerFactory.getLogger(HazelcastTest.class);
	
	static final Comparator<Item> ORDER = new ItemComparator();


	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHazelcast() throws InterruptedException{
		System.out.println("------------test1");
		
		HazelcastInstance h = Hazelcast.newHazelcastInstance(null);

         PriorityQueue<Item> q = new PriorityQueue<Item>(100, ORDER);
         BlockingQueue<Item> queue = h.getQueue("default");
         Queue<Item> listqueue = new LinkedList<Item>(); 
         for(int i=1 ; i < 5; i++){
        	sleep(2000); 
        	Item a = new Item(1, new Date().getTime(), "A" +i );
            listqueue.add(a);
            queue.add(a);
            sleep(10);
            a = new Item(2, new Date().getTime(), "B" +i);
            listqueue.add(a);
            queue.add(a);
            sleep(10);
            a = new Item(1, new Date().getTime(), "C"+i);
            listqueue.add(a);
            queue.add(a);
            sleep(10);
            a = new Item(4, new Date().getTime(), "D"+i );
            listqueue.add(a);
            queue.add(a);
            sleep(10);
            a = new Item(5, new Date().getTime(), "E" +i);
            listqueue.add(a);
            queue.add(a);     
         } 
         
         JOptionPane.showMessageDialog(null, "Start Hazelcast");
         q.addAll(queue);
         
        // sleep(1500);
         System.out.println("++++++ A Start PriorityQueue SIZE +++++ "+ q.size());
         while(listqueue.size() > 0){
        	 Item i = q.poll();
        	 if(i != null){
        		 if(i.getValue().equals("A1") || i.getValue().equals("B1")|| i.getValue().equals("C1")|| i.getValue().equals("D1")|| i.getValue().equals("E1")|| 
        				 i.getValue().equals("A2") || i.getValue().equals("B2")|| i.getValue().equals("C2")|| i.getValue().equals("D2")|| i.getValue().equals("E2") ||
        				 i.getValue().equals("A3") || i.getValue().equals("B3")|| i.getValue().equals("C3")|| i.getValue().equals("D3")|| i.getValue().equals("E3")|| 
        				 i.getValue().equals("A4") || i.getValue().equals("B4")|| i.getValue().equals("C4")|| i.getValue().equals("D4")|| i.getValue().equals("E4")){
      				sleep(300);
      				System.out.println("#### A SORTED #### " + i.getPrio() + " "
      						+ i.getTime() + " " + i.getValue());
      				listqueue.poll();
      				//queue.remove(i);
             	}
        	 }	
         }
        System.out.println("++++++ A Q Prioritie +++++ "+ q.size());
 		System.out.println("++++++ A QUEUE Hazelcast+++++ "+ queue.size());
 	}
	
	@After
	public void tearDown() throws Exception {
		//Hazelcast.shutdownAll();
	}
}
