package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.apache.log4j.helpers.Loader;
import org.apache.xmlbeans.XmlOptions;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.InputFile;

import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument;
import com.abbyy.recognitionServer10Xml.xmlResultSchemaV1.XmlResultDocument.XmlResult;




public class AbbyyXMLResultParseTest {
	
	protected static XmlResultDocument xmlResultDocument; 
	XmlResult xm ;
	private static InputStream isResult;
	final static Logger logger = LoggerFactory.getLogger(AbbyyXMLResultParseTest.class);
	
	public static final String NAMESPACE = "http://www.abbyy.com/RecognitionServer1.0_xml/XmlResult-schema-v1.xsd";
	
	//	private static final Factory NewInstanceInstantiator = null;

	@Before
	public void init() throws Exception {
		File fileresult = getBaseFolderAsFile();
		fileresult = new File(System.getProperty("user.dir") + File.separator +"src/test/resources/hotfolder/"+ "xmlresult.xml.result.xml");
		isResult = new FileInputStream(fileresult);
		XmlOptions options = new XmlOptions();
		// Set the namespace 
		options.setLoadSubstituteNamespaces(Collections.singletonMap("", NAMESPACE));		
		xmlResultDocument = XmlResultDocument.Factory.parse(isResult, options);
		xm = xmlResultDocument.getXmlResult();
	}
	@Test
	public void getTotalCharactersfromInputfileListe() throws Exception {
		BigDecimal a = new BigDecimal(0);
		for(InputFile l : xm.getInputFileList()){
			BigDecimal bd = new BigDecimal(l.getStatistics().getTotalCharacters());
			a = a.add(bd);			
		}
		assertTrue(a.equals(new BigDecimal(719)) );		
	}
	
	@Test
	public void getTotalUncertainCharacters() throws Exception {
		BigDecimal c = new BigDecimal(xm.getStatistics().getUncertainCharacters());
	    String strr = c.toString();
	    assertTrue(strr.equals("101") );
	}
	
	@Test
	public void getTotalUncertainCharactersfromInputfileListe() throws Exception {
		BigDecimal d = new BigDecimal(0);
		for(InputFile l : xm.getInputFileList()){
			BigDecimal bd = new BigDecimal(l.getStatistics().getUncertainCharacters());
			d = d.add(bd);	
		}
		System.out.println("ssss" +d);
		assertTrue(d.equals(new BigDecimal(101)) );	
	}
	
	@Test
	public void getTotalCharacters() throws Exception {
		BigDecimal b = new BigDecimal(xm.getStatistics().getTotalCharacters());
	    String str = b.toString();
	    System.out.println(str);
	    assertTrue(str.equals("719") );
	}
	
	@Test
	public void getCharacterAccuracy() throws Exception {
		BigDecimal totalChar = new BigDecimal(xm.getStatistics().getTotalCharacters());
		BigDecimal totalUncerChar = new BigDecimal(xm.getStatistics().getUncertainCharacters());
	    BigDecimal prozent = (totalUncerChar.divide(totalChar, 8, BigDecimal.ROUND_UP)).multiply(new BigDecimal(100));
	    System.out.println(prozent);
	    assertTrue((prozent.toString()).equals("14.04728800") );
	 //   System.out.println(xm.toString());
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
