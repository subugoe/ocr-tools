package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp;
import de.unigoettingen.sub.commons.util.stream.StreamUtils;

public class AbbyyServerOCR implements Runnable  {
	final static Logger logger = LoggerFactory.getLogger(AbbyyServerOCR.class);
//	AbbyyServerOCR abbyyServerOCR = new AbbyyServerOCR();
    
	@Override
	public void run(){
		try {
			logger.debug("Starting from test copyTestFilesToServer");
			copyTestFilesToServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
	
    public void copyTestFilesToServer () throws Exception {
		AbbyyServerOCREngine ase = AbbyyServerOCREngine.getInstance();
		//ase.setHotfolder(hotfolder);
		

		for (String book : AbbyyOCRProcessTest.TEST_FOLDERS) {
			File testDir = new File(AbbyyOCRProcessTest.BASEFOLDER_FILE.getAbsoluteFile() + "/hotfolder/" + HotfolderTest.INPUT + "/" + book);
			logger.debug("Creating AbbyyOCRProcess for " + testDir.getAbsolutePath());
		//	List<File> imageDirs = OCRUtil.getTargetDirectories(testDir,extension);
			AbbyyOCRProcess aop = AbbyyServerOCREngine.createProcessFromDir(testDir, AbbyyTicketTest.EXTENSION);
			aop.addLanguage(Locale.GERMAN);
			aop.setTextTyp(OCRTextTyp.NORMAL);
			assertNotNull(aop);
			for (OCRFormat f: AbbyyTicketTest.OUTPUT_DEFINITIONS.keySet()) {
				//Call the copy contructor to get rid of mock objects
				AbbyyOCROutput aoo = new AbbyyOCROutput((AbbyyOCROutput) AbbyyTicketTest.OUTPUT_DEFINITIONS.get(f));
					//(AbbyyOCROutput) AbbyyTicketTest.OUTPUT_DEFINITIONS.get(f);
				URI uri = new URI(AbbyyTicketTest.BASEFOLDER_FILE.toURI() + "LOCAL" + "/" + book + "." + f.toString().toLowerCase());
				aoo.setUri(uri);
				aop.addOutput(f, aoo);
				
			}
			//aop.setOcrOutputs();
			//TODO: set the inout folder to new File(apacheVFSHotfolderImpl.getAbsolutePath() + "/" + INPUT_NAME);
			File testTicket = new File(AbbyyOCRProcessTest.BASEFOLDER_FILE.getAbsoluteFile() + "/"
					+ HotfolderTest.INPUT
					+ "/"
					+ book
					+ ".xml");
			aop.write(new FileOutputStream(testTicket), testDir.getName());
			logger.debug("Wrote AbbyyTicket:\n" + StreamUtils.dumpInputStream(new FileInputStream(testTicket)));
			logger.debug("Starting Engine");
			ase.addOcrProcess(aop);
		}
		ase.recognize();
		
	}

    
}

