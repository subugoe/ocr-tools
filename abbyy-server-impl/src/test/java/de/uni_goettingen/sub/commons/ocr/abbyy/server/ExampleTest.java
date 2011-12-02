package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp;

public class ExampleTest {

	private String resourcesDir = System.getProperty("user.dir") + "/src/test/resources/";
		
	@BeforeClass
	public static void setUp() throws Exception {
		new File(ServerStarter.davFolder + "/error").mkdirs();
		new File(ServerStarter.davFolder + "/input").mkdirs();
		new File(ServerStarter.davFolder + "/output").mkdirs();

		ServerStarter.startDavServer(9001);
	}

	@Ignore
	@Test
	public void test() throws Exception {
		
		AbbyyServerOCREngine engine = AbbyyServerOCREngine.newOCREngine();
		
		OCRProcess process = constructProcess(engine, "procname");
		
		engine.addOcrProcess(process);
		
		
		Thread t = new DirAbbyySimulator(ServerStarter.davFolder);
		t.start();
		
		engine.recognize();
		
	}
	
	private OCRProcess constructProcess(OCREngine engine, String processName) {
		OCRProcess process = engine.newOcrProcess();
		
		process.setName(processName);

		List<OCRImage> images = new ArrayList<OCRImage>();
		File imagePath = new File(resourcesDir + "hotfolder/PPN129323640_0010/00000001.tif");
		OCRImage image = engine.newOcrImage(imagePath.toURI());
		image.setSize(imagePath.length());
		images.add(image);
		process.setOcrImages(images);
		
		OCROutput output = engine.newOcrOutput();
		File resultDir = new File(System.getProperty("user.dir") + "/target/test-results");
		File outputPath = new File(resultDir + "/" + processName + ".txt");
		output.setUri(outputPath.toURI());
		process.addOutput(OCRFormat.TXT, output);
		
		Set<Locale> langs = new HashSet<Locale>();
		langs.add(new Locale("en"));
		process.setLanguages(langs);
		
		process.setPriority(OCRPriority.NORMAL);
		
		process.setTextTyp(OCRTextTyp.NORMAL);
		
		return process;
	}
	
	@After
	public void tearDown() throws Exception {
	}


}
