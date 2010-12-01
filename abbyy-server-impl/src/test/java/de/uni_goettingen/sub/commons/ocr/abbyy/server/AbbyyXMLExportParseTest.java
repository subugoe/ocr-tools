package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.apache.log4j.helpers.Loader;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument;
import com.abbyy.fineReaderXml.fineReader6SchemaV1.DocumentDocument.Document;

public class AbbyyXMLExportParseTest {

	protected static DocumentDocument documentDocument;
	Document doc;
	private static InputStream isDoc, isDocString;
	final static Logger logger = LoggerFactory.getLogger(AbbyyXMLExportParseTest.class);
	
	//public static final String NAMESPACE = "http://www.abbyy.com/FineReader_xml/FineReader6-schema-v1.xml";
	
	
	
	@Before
	public void init() throws XmlException, IOException{
		File filexmlExport = getBaseFolderAsFile();
		filexmlExport = new File(getBaseFolderAsFile().getAbsolutePath()+ "/xmlExport.xml");
		isDoc = new FileInputStream(filexmlExport);
		isDocString = new FileInputStream(filexmlExport);
		documentDocument = DocumentDocument.Factory.parse(isDoc);
		doc = documentDocument.getDocument();
	}
	
	@Test
	public void getSoftwareNameAndVersion() throws IOException{
		String xmlexport = doc.getProducer();
		String[] splittArray = xmlexport.split(" ");
		assertTrue( splittArray[0].equals("FineReader"));
		assertTrue(splittArray[1].equals("8.0"));
		
	}
	
	@Test
	public void getDocumentType(){
		String xmlexport = doc.toString();
		String[] splittArray1 , documentType;
		splittArray1 = xmlexport.split("schemaLocation=");
		documentType = splittArray1[1].split(" ");
		System.out.println("Value: " + documentType[0].substring(1));
		assertTrue((documentType[0].substring(1)).equals("http://www.abbyy.com/FineReader_xml/FineReader6-schema-v1.xml"));
	}
	
	@Test
	public void getProcessingNote() throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(isDocString));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line + "\n");
	    }
	    isDocString.close();
	    assertTrue(sb.toString()!= "");
	   // System.out.println(sb.toString());
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
