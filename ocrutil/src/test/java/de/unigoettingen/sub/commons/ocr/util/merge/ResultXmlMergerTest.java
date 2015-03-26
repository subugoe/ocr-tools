package de.unigoettingen.sub.commons.ocr.util.merge;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ResultXmlMergerTest {

	private final String input1 = "<XmlResult>"
			+ "<Name>Test1of2.xml</Name>"
			+ "<InputFile></InputFile>"
			+ "<Statistics TotalCharacters='1000' UncertainCharacters='100'/>"
			+ "</XmlResult>";
	private final String input2 = "<XmlResult>"
			+ "<Name>Test2of2.xml</Name>"
			+ "<InputFile></InputFile>"
			+ "<Statistics TotalCharacters='500' UncertainCharacters='50'/>"
			+ "</XmlResult>";

	@Test
	public void shouldMergeTwoWellFormed() throws XpathException, SAXException, IOException {
		InputStream stream1 = new ByteArrayInputStream(input1.getBytes());
		InputStream stream2 = new ByteArrayInputStream(input2.getBytes());
		List<InputStream> inputs = Arrays.asList(stream1, stream2);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		Merger mergerSut = new ResultXmlMerger();
		mergerSut.mergeBuffered(inputs, output);	
		String result = output.toString();
		System.out.println(result);
		assertXpathEvaluatesTo("2", "count(//Statistics)", result);
		assertXpathEvaluatesTo("1500", "sum(//Statistics/@TotalCharacters)", result);
		assertXpathEvaluatesTo("150", "sum(//Statistics/@UncertainCharacters)", result);
	}

}
