package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;

public class ConfigParserTest {
	
	final static Logger logger = LoggerFactory.getLogger(ConfigParserTest.class);
	
	@BeforeClass
	public static void init () {
		ConfigParser cp = new ConfigParser();
		assertFalse(ConfigParser.getDebugAuth());
	}

	@Test
	public void testAuth () {
		System.setProperty("ocr.finereader.server.debug.auth", "true");
		ConfigParser cp = new ConfigParser();
		assertTrue(ConfigParser.getDebugAuth());
		
	}
	
}
