package de.unigoettingen.sub.commons.ocr.util.merge;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class AbbyyXmlMergerTest {

	private final String input1 = "<document>"
			+ "<documentData></documentData>"
			+ "<page>"
			+ "<block>"
			+ "<text>"
			+ "<par>"
			+ "<line>"
			+ "<formatting>"
			+ "<charParams l='100'>a</charParams>"
			+ "</formatting>"
			+ "</line>"
			+ "</par>"
			+ "</text>"
			+ "</block>"
			+ "</page>"
			+ "</document>";
	private final String input2 = "<document>"
			+ "<documentData></documentData>"
			+ "<page>"
			+ "<block>"
			+ "<text>"
			+ "<par>"
			+ "<line>"
			+ "<formatting>"
			+ "<charParams l='200'>b</charParams>"
			+ "</formatting>"
			+ "</line>"
			+ "</par>"
			+ "</text>"
			+ "</block>"
			+ "</page>"
			+ "</document>";
	
	@Test
	public void shouldMergeTwoWellFormed() throws XpathException, SAXException, IOException {
		InputStream stream1 = new ByteArrayInputStream(input1.getBytes());
		InputStream stream2 = new ByteArrayInputStream(input2.getBytes());
		List<InputStream> inputs = Arrays.asList(stream1, stream2);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		Merger mergerSut = new AbbyyXmlMerger();
		mergerSut.mergeBuffered(inputs, output);	
		String result = output.toString();
		
		assertXpathEvaluatesTo("2", "count(//page)", result);
		assertXpathEvaluatesTo("a", "//page[1]//charParams", result);
		assertXpathEvaluatesTo("b", "//page[2]//charParams", result);
		assertXpathEvaluatesTo("100", "//page[1]//charParams/@l", result);
		assertXpathEvaluatesTo("200", "//page[2]//charParams/@l", result);
	}

}
