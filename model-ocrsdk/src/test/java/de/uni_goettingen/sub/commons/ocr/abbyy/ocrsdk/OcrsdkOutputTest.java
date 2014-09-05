package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class OcrsdkOutputTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void saveToFile() throws IOException {
		File outFile = new File("target/testOutput.txt");
		OcrsdkOutput output = new OcrsdkOutput();
		output.setLocalUri(outFile.toURI());
		InputStream toSave = new ByteArrayInputStream("test".getBytes());
		
		output.save(toSave);
		
		InputStream is = new FileInputStream(outFile);
		String savedString = IOUtils.toString(is);
		assertEquals("test", savedString);
	}

}
