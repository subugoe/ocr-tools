package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;

public class ConfigParserTest {

	final static Logger logger = LoggerFactory.getLogger(ConfigParserTest.class);
	
	protected static ConfigParser cp = null;

	@BeforeClass
	public static void init () throws ConfigurationException {
		
		try {
			cp = new ConfigParser().loadConfig();
		} catch (RuntimeException e) {
			logger.info("If run from Maven an Exception is expected", e);
		}
		assertNull(cp);
	}
	
	@Test
	public void testUrl () throws ConfigurationException {
		URL url = getClass().getResource("/" + ConfigParser.DEFAULT_CONFIG);
		cp = new ConfigParser(url).loadConfig();
		assertFalse(cp.getDebugAuth());
	}

	@Test
	public void testAuth () throws ConfigurationException {
		System.setProperty(ConfigParser.DEBUG_PROPERTY, "true");
		URL url = getClass().getResource("/" + ConfigParser.DEFAULT_CONFIG);
		cp = new ConfigParser(url).loadConfig();
		assertTrue(cp.getDebugAuth());

	}

}
