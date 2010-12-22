package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.helpers.Loader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

public class SerializerTextMDTest {
	static OCRProcessMetadata ocrProcessMetadata;
	final static Logger logger = LoggerFactory.getLogger(SerializerTextMDTest.class);
	public static File BASEFOLDER_FILE;
	public static File TICKET_FILE;
	public static List<Locale> langs;
	static {
		BASEFOLDER_FILE = getBaseFolderAsFile();
		File TICKET_FILE = new File(BASEFOLDER_FILE.getAbsolutePath() + "/textMD.xml");
	}
	
	@BeforeClass
	public static void init (){
		ocrProcessMetadata = new OCRProcessMetadataImpl();
		ocrProcessMetadata.setLinebreak("LF");
		ocrProcessMetadata.setTextNote("textNote TEST");
		/*langs = new ArrayList<Locale>();
		langs.add(new Locale("de"));
		ocrProcessMetadata.setLanguages(langs);*/
	}
	
	@Test
	public void writeTicket () throws IOException{
		File TICKET_FILE = new File(BASEFOLDER_FILE.getAbsolutePath() + "/textMD.xml");

		SerializerTextMD serializerTextMD = new SerializerTextMD(ocrProcessMetadata);
		serializerTextMD.write(TICKET_FILE);
	}
	
	
	public static File getBaseFolderAsFile () {
		File basefolder;
		// TODO: GDZ: Do wee really need to depend on Log4J here? I don't think so...
		URL url = Loader.getResource("");
		try {
			basefolder = new File(url.toURI());
		} catch (URISyntaxException ue) {
			basefolder = new File(url.getPath());
		}
		return basefolder;
	}
}
