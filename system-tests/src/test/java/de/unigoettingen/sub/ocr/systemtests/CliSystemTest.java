package de.unigoettingen.sub.ocr.systemtests;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocrComponents.cli.Main;


public class CliSystemTest {

	private String[] resultFileNames = {"test.txt", "test.pdf", "test.xml", "test.xml.result.xml",
			"akademie.txt", "akademie.pdf", "akademie.xml", "akademie.xml.result.xml"};
	
	@Before
	public void beforeEachTest() throws Exception {
		ensureResultsNotPresent();
	}

	private void ensureResultsNotPresent() {
		for (String fileName : resultFileNames) {
			File file = new File("target/" + fileName);
			if(file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void test() throws URISyntaxException, IOException {
		Main.main(validOptions());
		
		assertResultsArePresent();
		
		String textResult = FileUtils.readFileToString(new File("target/test.txt"));
		assertThat(textResult, containsString("Anna-Lena"));
	}

	private void assertResultsArePresent() {
		for (String fileName : resultFileNames) {
			File file = new File("target/" + fileName);
			assertTrue("File must be present: " + file, file.exists());
		}
	}

	private String[] validOptions() {
		return new String[]{"-indir", "src/test/resources/input", 
				"-informats", "tif,jpg",
				"-texttype", "normal",
				"-langs", "de,en,fr",
				"-outdir", "target",
				"-outformats", "txt,pdf,xml",
				"-prio", "2",
				"-engine", "abbyy-multiuser",
				"-props", "maxImagesInSubprocess=2,maxParallelProcesses=2,books.split=true"};
	}

}
