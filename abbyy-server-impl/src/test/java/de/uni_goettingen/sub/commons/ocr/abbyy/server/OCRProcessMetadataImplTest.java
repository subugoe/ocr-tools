package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.helpers.Loader;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCRProcessMetadataImplTest {
	private static InputStream isResult, isDoc, isDocd;
	OCRProcessMetadataImpl ocrProcessMetadataImpl;
	final static Logger logger = LoggerFactory.getLogger(OCRProcessMetadataImplTest.class);
	
	@Before
	public void init() throws Exception {
		File fileresult = getBaseFolderAsFile();
		fileresult = new File(getBaseFolderAsFile().getAbsolutePath()+ "/xmlresult.xml.result.xml");
		isResult = new FileInputStream(fileresult);
		File filexmlexport = getBaseFolderAsFile();
		filexmlexport = new File(getBaseFolderAsFile().getAbsolutePath()+ "/xmlExport.xml");
		isDoc = new FileInputStream(filexmlexport);
		isDocd = new FileInputStream(filexmlexport);
		ocrProcessMetadataImpl = new OCRProcessMetadataImpl(isResult, isDoc ,isDocd);
	}
	
	@Test
	public void getDocumentType(){
		assertTrue((ocrProcessMetadataImpl.getDocumentType()).equals("http://www.abbyy.com/FineReader_xml/FineReader6-schema-v1.xml"));
	}
	
	@Test
	public void getSoftwareName(){
		assertTrue((ocrProcessMetadataImpl.getSoftwareName()).equals("FineReader"));
	}
	
	@Test
	public void getSoftwareVersion(){
		assertTrue((ocrProcessMetadataImpl.getSoftwareVersion()).equals("8.0"));
	}
	
	@Test
	public void getCharacterAccuracy(){
		System.out.println(ocrProcessMetadataImpl.getCharacterAccuracy());
		assertTrue((ocrProcessMetadataImpl.getCharacterAccuracy().toString()).equals("6.29916100") );
	}
	
	@Test
	public void getProcessingNote() throws IOException{
	    String inputStreamprocessingNote = ocrProcessMetadataImpl.getProcessingNote();	   
	    System.out.println(inputStreamprocessingNote);
	    assertTrue(inputStreamprocessingNote.toString()!="");

	}
	
	public static File getBaseFolderAsFile() {
		File basefolder;
		// TODO: GDZ: Do wee really need to depend on Log4J here? I don't think
		// so...
		URL url = Loader.getResource("");
		try {
			basefolder = new File(url.toURI());
		} catch (URISyntaxException ue) {
			basefolder = new File(url.getPath());
		}
		return basefolder;
	}
}
