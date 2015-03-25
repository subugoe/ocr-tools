package de.unigoettingen.sub.commons.ocr.util.merge;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class PdfMergerTest {

	@Test
	public void test() throws DocumentException, IOException {
		PdfMerger mergerSut = new PdfMerger();
		List<InputStream> inputs = Arrays.asList(new InputStream[]{createPdf("test1"), createPdf("test2")});
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		mergerSut.merge(inputs, output);
		
		String result = readFromPdf(output);

		assertThat(result, containsString("test1"));
		assertThat(result, containsString("test2"));
	}
	
	private ByteArrayInputStream createPdf(String text) throws DocumentException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document document = new Document();
		PdfWriter.getInstance(document, baos);
		document.open();
		document.add(new Paragraph(text));
		document.close();
		
		return new ByteArrayInputStream(baos.toByteArray());
	}
	
	private String readFromPdf(ByteArrayOutputStream output) throws IOException {
		PdfReader reader = new PdfReader(output.toByteArray());

		return new String(reader.getPageContent(1)) + new String(reader.getPageContent(2));
	}

}
