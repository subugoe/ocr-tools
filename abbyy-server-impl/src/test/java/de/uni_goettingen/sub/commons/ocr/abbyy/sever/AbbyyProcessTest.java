package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyProcess;

@SuppressWarnings("serial")
public class AbbyyProcessTest {
	final static Logger logger = LoggerFactory.getLogger(AbbyyProcessTest.class);
	public static File BASEFOLDER_FILE = TicketTest.BASEFOLDER_FILE;
	public static List<String> TEST_FOLDERS;

	
	static {
		TEST_FOLDERS = new ArrayList<String>(){{
			add("PPN129323640_0010");
			add("PPN31311157X_0102");
			add("PPN514401303_1890");
			add("PPN514854804_0001");
		}};
				
	}
	
	
	@BeforeClass
	public static void init () {
		logger.debug("Starting AbbyyProcessTest");
		//Nothing to do here
	}
	
	@Test
	public void createAbbyyProcess () throws IOException {
		for (String book: TEST_FOLDERS) {
			File testDir = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book);
			logger.debug("Creating AbbyyProcess for "+ testDir.getAbsolutePath());
			AbbyyProcess aop = AbbyyProcess.createProcessFromDir(testDir, TicketTest.EXTENSION);
			assertNotNull(aop);
			aop.setOcrOutput(TicketTest.OUTPUT_DEFINITIONS);
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			aop.write(testTicket, testDir.getName());
			logger.debug("Wrote Ticket:\n" + TicketTest.dumpTicket(new FileInputStream(testTicket)));
		}
		
	}
	
	@Test
	public void checkTicketCount () throws IOException, XmlException {
		for (String book: TEST_FOLDERS) {
			File testDir = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book);
			logger.debug("Creating AbbyyProcess for "+ testDir.getAbsolutePath());
			AbbyyProcess aop = AbbyyProcess.createProcessFromDir(testDir, TicketTest.EXTENSION);
			assertNotNull(aop);
			aop.setOcrOutput(TicketTest.OUTPUT_DEFINITIONS);
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			aop.write(testTicket, testDir.getName());
			logger.debug("Wrote Ticket:\n" + TicketTest.dumpTicket(new FileInputStream(testTicket)));
			assertTrue("This fails if the number of files between ticket and file system differs.", TicketTest.parseFilesFromTicket(testTicket).size() == aop.getOcrImages().size());
		}
	}
	
	@Test
	public void createProcessViaAPI () {
		
	}
	
	@AfterClass
	public static void cleanup () {
		logger.debug("Cleaning up");
		for (String book: TEST_FOLDERS) {
			File testTicket = new File(BASEFOLDER_FILE.getAbsoluteFile() + File.separator + HotfolderTest.INPUT + File.separator + book + ".xml");
			logger.equals("Deleting file " + testTicket.getAbsolutePath());
			testTicket.delete();
			assertTrue(!testTicket.exists());
		}
	}
	
}
