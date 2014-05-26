package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static de.uni_goettingen.sub.commons.ocr.abbyy.server.PathConstants.*;

public class XMLParserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws FileNotFoundException, XMLStreamException {
		XmlParser parser = new XmlParser();
		InputStream is = new FileInputStream(new File(LOCAL_INPUT, "error.xml.result.xml"));
		String error = parser.xmlresultErrorparse(is, "someId");
		assertTrue(error.contains("Frensch"));
	}

}
