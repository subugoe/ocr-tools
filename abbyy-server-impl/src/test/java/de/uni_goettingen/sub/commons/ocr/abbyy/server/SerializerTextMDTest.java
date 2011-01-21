package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

public class SerializerTextMDTest {
	public static OCRProcessMetadata ocrProcess;
	public static AbbyySerializerTextMD serializerTextMD;
	final static Logger logger = LoggerFactory
			.getLogger(SerializerTextMDTest.class);
	public static File BASEFOLDER_FILE;
	public static File TextMD_FILE;
	public static List<Locale> langs;
	static {
		BASEFOLDER_FILE = new File(System.getProperty("user.dir")
				+ "/src/test/resources");
		TextMD_FILE = new File(BASEFOLDER_FILE.toString()
				+ "/textMD/textMD.xml");
	}

	@BeforeClass
	public static void init() {
		ocrProcess = new AbbyyOCRProcessMetadata();
		ocrProcess.setEncoding("UTF-8");
		ocrProcess.setLinebreak("LF");
		ocrProcess.setTextNote("general note on material");
		ocrProcess
				.setProcessingNote("general note about the processing of the file");
	}

	@Test
	public void writeTicket() throws IOException {
		serializerTextMD = new AbbyySerializerTextMD(ocrProcess);
		serializerTextMD.write(TextMD_FILE);
	}

}
