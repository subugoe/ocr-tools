package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.FileManager;

public class FileTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		FileManager manager = new FileManager();
		Properties props = manager.getPropertiesFromFile("gbv-fraktur.properties");
		
		assertEquals("input", props.getProperty("input"));
	}

}
