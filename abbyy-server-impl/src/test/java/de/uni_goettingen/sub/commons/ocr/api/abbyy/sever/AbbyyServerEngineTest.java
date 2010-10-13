package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.assertNotNull;




import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
		
	}

	@Test
	public void testCli () throws IOException, ConfigurationException {	
		List <String> inputFile = new ArrayList<String>();
		
		String inputfile = "file://./src/test/resources/local/PPN129323640_0010";
		String inputfile1 = "file://./src/test/resources/local/PPN31311157X_0102";
		String inputfile2 = "file://./src/test/resources/local/PPN514401303_1890";
		inputFile.add(inputfile);
		inputFile.add(inputfile1);
		inputFile.add(inputfile2);
		for (String str : inputFile){
			str = parseString(str);
			System.out.println("waw " + str);
			OCRProcess p = abbyy.newProcess(new File(str));
		    abbyy.addOcrProcess(p);
		}
		/*inputfile = parseString(inputfile);
		OCRProcess p = abbyy.newProcess(new File(inputfile));
	    abbyy.addOcrProcess(p);*/
		
		abbyy.recognize();
		assertNotNull(abbyy);

	}

	public static String parseString(String str){
		String remoteFile = null;
		if (str.contains("/./")) {
			int i = 0;
			for (String lang : Arrays.asList(str.split("/./"))) {
				if (i == 0){
					i++;
				}else{
					remoteFile = lang;
				}
			}
		}
		return remoteFile;	
	}
}
