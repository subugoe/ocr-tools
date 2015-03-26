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

public class HocrMergerTest {

	private final String input1 = "<html><head></head>"
			+ "<body>"
			+ "<div class='ocr_page' id='page_1'>"
			+ "<div class='ocr_carea'>"
			+ "<p class='ocr_par'>"
			+ "<span class='ocr_line'>"
			+ "<span class='ocr_word'>"
			+ "<span class='xocr_word'>test1</span>"
			+ "</span>"
			+ "</span>"
			+ "</p>"
			+ "</div>"
			+ "</div>"
			+ "</body>"
			+ "</html>";
	private final String input2 = "<html><head></head>"
			+ "<body>"
			+ "<div class='ocr_page' id='page_2'>"
			+ "<div class='ocr_carea'>"
			+ "<p class='ocr_par'>"
			+ "<span class='ocr_line'>"
			+ "<span class='ocr_word'>"
			+ "<span class='xocr_word'>test2</span>"
			+ "</span>"
			+ "</span>"
			+ "</p>"
			+ "</div>"
			+ "</div>"
			+ "</body>"
			+ "</html>";
	
	@Test
	public void test() throws XpathException, SAXException, IOException {
		InputStream stream1 = new ByteArrayInputStream(input1.getBytes());
		InputStream stream2 = new ByteArrayInputStream(input2.getBytes());
		List<InputStream> inputs = Arrays.asList(stream1, stream2);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Merger mergerSut = new HocrMerger();
		mergerSut.mergeBuffered(inputs, output);	
		String result = output.toString();

		assertXpathEvaluatesTo("2", "count(//*[local-name()='div' and @class='ocr_page'])", result);
		assertXpathEvaluatesTo("test1", "//*[local-name()='div' and @class='ocr_page'][1]//*[local-name()='span' and @class='xocr_word']", result);
		assertXpathEvaluatesTo("test2", "//*[local-name()='div' and @class='ocr_page'][2]//*[local-name()='span' and @class='xocr_word']", result);
	}

}
