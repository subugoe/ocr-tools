package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.assertNotNull;



import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import org.apache.commons.configuration.ConfigurationException;

import org.apache.commons.vfs.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerEngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class AbbyyServerEngineTest {
	public static OCREngine abbyy;
	public Hotfolder hotfolder;

	final static Logger logger = LoggerFactory.getLogger(AbbyyServerEngineTest.class);
	
	@Before
	public void init () throws FileSystemException, ConfigurationException, URISyntaxException {
		logger.debug("Starting Test");
		ConfigParser config = new ConfigParser();
		
		logger.debug(config.getWebdavURL());
		URI uri = new URI(config.getWebdavURL());
		assertNotNull(uri);
		
		abbyy = AbbyyServerEngine.getInstance();

		assertNotNull(abbyy);
		
		//abbyy = mock(AbbyyServerEngine.class);



	}

	@Test

	public void testCli () throws IOException, ConfigurationException {	
		OCRProcess p = null; //((OCRProcess) new Process(new File(""));
		abbyy.setOCRProcess(p);
		

		
		abbyy.recognize();
		assertNotNull(abbyy);

	}

}
