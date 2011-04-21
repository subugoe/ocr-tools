package de.unigoettingen.sub.commons.ocrComponents.webservice;


import static java.lang.Thread.sleep;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;


public class HazelcastTest {

	static final Comparator<Item> ORDER = new ItemComparator();


	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHazelcast() throws InterruptedException{
		System.out.println("------------test1");
		 HazelcastInstance h = Hazelcast.newHazelcastInstance(null);
         IList<Item> temp = h.getList("default");
         
         temp.add(new Item(2, new Date().getTime()));
         temp.add(new Item(1, new Date().getTime()));
         System.out.println("+++++++++++"+ temp.size());
         while (temp.size() < 3) {
        	 temp = h.getList("default");
         }
         
         PriorityQueue<Item> q = new PriorityQueue<Item>(100, ORDER);
         q.addAll(temp);
         
         
         Item i = q.poll();
         System.out.println(i.getPrio() + " " + i.getTime());
         i = q.poll();
         System.out.println(i.getPrio() + " " + i.getTime());
         i = q.poll();
         System.out.println(i.getPrio() + " " + i.getTime());
        
 	}
	
	@After
	public void tearDown() throws Exception {
	}

}
