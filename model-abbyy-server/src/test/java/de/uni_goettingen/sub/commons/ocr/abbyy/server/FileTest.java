package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class FileTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		FileAccess manager = new FileAccess();
		Properties props = manager.getPropertiesFromFile("gbv-fraktur.properties");
		
		assertEquals("input", props.getProperty("inputFolder"));
	}

}
