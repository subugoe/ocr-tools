package de.unigoettingen.sub.commons.ocrComponents.webservice;

import static java.lang.Thread.sleep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class Probe {

	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHazelcast() {
		Item a = new Item(1, new Date().getTime(), "A" );
		List<Item> list = new ArrayList<Item>();
		
		list.add(a);

		a = new Item(2, new Date().getTime(), "B");

		list.add(a);
		
		System.out.println("#### A REM QUEUE SIZE #### "+ list.size());			

		list.remove(a);
		//a--;
		System.out.println("####A  LIST SIZE  ##########" + list.size());

	}
	
	
}
