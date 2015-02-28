package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

public class XmlParserTest {

	@Test
	public void shouldParseOutErrorMessage() throws IOException {
		XmlParser parserSut = new XmlParser();
		
		String errorXml = "<XmlResult><Error>An error has occurred</Error></XmlResult>";
		ByteArrayInputStream baos = new ByteArrayInputStream(errorXml.getBytes());
		
		String errorMessage = parserSut.readErrorFromResultXml(baos, "testId");
		
		assertEquals("Message", "An error has occurred", errorMessage);
	}

}
