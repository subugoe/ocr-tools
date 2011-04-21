package de.unigoettingen.sub.commons.ocrComponents.webservice;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import static java.lang.Thread.sleep;

public class ZazelcastTest {

	static final Comparator<Item> ORDER = new ItemComparator();


	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHazelcast() throws InterruptedException{
		System.out.println("------------test2");

		 HazelcastInstance h = Hazelcast.newHazelcastInstance(null);
         IList<Item> temp = h.getList("default");
         
         temp.add(new Item(2, new Date().getTime()));
 
         
         sleep(5000);
 	}
	
	@After
	public void tearDown() throws Exception {
	}

}
