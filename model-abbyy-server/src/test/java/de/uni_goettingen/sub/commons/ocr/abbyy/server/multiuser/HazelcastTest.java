package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.LOCAL_INPUT;
import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.LOCAL_OUTPUT;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyOCRProcess;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.MyServers;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class HazelcastTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MyServers.startDavServer();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MyServers.stopDavServer();
	}

	@Before
	public void setUp() throws Exception {
		for (File file : LOCAL_OUTPUT.listFiles()) {
			file.delete();
		}
		MyServers.startAbbyySimulator();
	}

	@After
	public void tearDown() throws Exception {
		MyServers.stopAbbyySimulator();
	}

	@Test
	public void test() {
		OCREngine engine = MultiUserAbbyyOCREngine.getInstance();
		
		File inputDir = new File(LOCAL_INPUT, "oneImageBook");
		File outputFile = new File(LOCAL_OUTPUT, "oneImageBook.xml");

		recognize(engine, inputDir, outputFile);
				
		assertTrue(outputFile.exists());
	}
	
	private void recognize(OCREngine engine, File inputDir, File outputFile) {
		AbbyyOCRProcess process = (AbbyyOCRProcess)engine.newOcrProcess();
		String jobName = inputDir.getName();
		process.setName(jobName);
		
		List<OCRImage> images = new ArrayList<OCRImage>();
		for (File imageFile : inputDir.listFiles()) {
			// could be a dir like eg .svn
			if (imageFile.isFile()) {
				URI imageUri = imageFile.toURI();
				OCRImage image = engine.newOcrImage(imageUri);
				images.add(image);
			}
		}
		process.setOcrImages(images);
		
		OCRFormat format = OCRFormat.XML;
		OCROutput output = engine.newOcrOutput();
		output.setUri(outputFile.toURI());
		process.addOutput(format, output);
		
		engine.addOcrProcess(process);
		engine.recognize();

	}
	
	@Test
	public void twoInstances() throws InterruptedException, ConfigurationException {
		final File inputDir1 = new File(LOCAL_INPUT, "tenImagesBook");
		final File outputFile1 = new File(LOCAL_OUTPUT, "tenImagesBook.xml");
		final File inputDir2 = new File(LOCAL_INPUT, "oneImageBook");
		final File outputFile2 = new File(LOCAL_OUTPUT, "oneImageBook.xml");
		
		// first instance is started in a thread
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					HazelcastInstance h1 = Hazelcast.newHazelcastInstance(null);
					OCREngine engine = new MultiUserAbbyyOCREngine(h1);
					recognize(engine, inputDir1, outputFile1);
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
		
		// threshold for hazelcast, or else there is an exception
		Thread.sleep(500);
		
		// second instance
		HazelcastInstance h2 = Hazelcast.newHazelcastInstance(null);
		OCREngine engine = new MultiUserAbbyyOCREngine(h2);
		recognize(engine, inputDir2, outputFile2);

		t.join();

		assertTrue(outputFile1.exists());
		assertTrue(outputFile2.exists());
		
		// the thread instance is started first, but must finish after 
		// the second instance, which means they ran in parallel
		assertTrue(outputFile1.lastModified() > outputFile2.lastModified());
	}
	
}