package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.mockito.Mockito.*;
import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrPriority;
import de.uni_goettingen.sub.commons.ocr.api.OcrQuality;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;

public class AbbyyTicketTest {

	private AbbyyTicket ticketSut;
	private AbbyyProcess processMock = mock(AbbyyProcess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		when(processMock.getRemoteImageNames()).thenReturn(Arrays.asList("01.tif", "02.tif"));
		when(processMock.getPriority()).thenReturn(OcrPriority.NORMAL);
		when(processMock.getTextType()).thenReturn(OcrTextType.NORMAL);
		
		when(processMock.getLanguages()).thenReturn(new HashSet<Locale>(Arrays.asList(Locale.GERMAN, Locale.ENGLISH)));
		
		when(processMock.getQuality()).thenReturn(OcrQuality.BEST);
		when(processMock.hasImagesAndOutputs()).thenReturn(true);
				
		when(processMock.getAllOutputFormats()).thenReturn(Collections.singleton(OcrFormat.XML));
		when(processMock.getOutputUriForFormat(OcrFormat.XML)).thenReturn(new URI("file:/test/result.xml"));
		
		when(processMock.getWindowsPathForServer()).thenReturn("c://temp");
		
		ticketSut = new AbbyyTicket(processMock);
	}

	@Test
	public void shouldCreateCorrectTicket() throws IOException, SAXException, XpathException {
		String xml = createTicket();
		
		assertXpathEvaluatesTo("Normal", "/XmlTicket/@Priority", xml);
		assertXpathEvaluatesTo("2", "count(//InputFile)", xml);
		assertXpathEvaluatesTo("Thorough", "//RecognitionParams/@RecognitionQuality", xml);
		assertXpathEvaluatesTo("Normal", "//TextType", xml);
		assertXpathEvaluatesTo("English", "//Language[1]", xml);
		assertXpathEvaluatesTo("German", "//Language[2]", xml);
		assertXpathEvaluatesTo("MergeIntoSingleFile", "//ExportParams/@DocumentSeparationMethod", xml);
		assertXpathEvaluatesTo("XML", "//ExportFormat/@OutputFileFormat", xml);
		assertXpathEvaluatesTo("result.xml", "//NamingRule", xml);
		assertXpathEvaluatesTo("c://temp", "//OutputLocation", xml);
	}

	@Test
	public void shouldAdaptPriority() throws IOException, XpathException, SAXException {
		when(processMock.getPriority()).thenReturn(OcrPriority.LOW);
		String xml = createTicket();
		
		assertXpathEvaluatesTo("Low", "/XmlTicket/@Priority", xml);		
	}

	@Test
	public void shouldAdaptQuality() throws IOException, XpathException, SAXException {
		when(processMock.getQuality()).thenReturn(OcrQuality.FAST);
		String xml = createTicket();
		
		assertXpathEvaluatesTo("Fast", "//RecognitionParams/@RecognitionQuality", xml);
	}

	@Test
	public void shouldAdaptTextType() throws IOException, XpathException, SAXException {
		when(processMock.getTextType()).thenReturn(OcrTextType.GOTHIC);
		String xml = createTicket();
		
		assertXpathEvaluatesTo("Gothic", "//TextType", xml);
	}

	@Test
	public void shouldAdaptLanguage() throws IOException, XpathException, SAXException {
		when(processMock.getLanguages()).thenReturn(new HashSet<Locale>(Arrays.asList(Locale.FRENCH)));
		String xml = createTicket();
		
		assertXpathEvaluatesTo("French", "//Language", xml);
	}

	@Test
	public void shouldAdaptOutputFormats() throws IOException, XpathException, SAXException, URISyntaxException {
		when(processMock.getAllOutputFormats()).thenReturn(new HashSet<OcrFormat>(Arrays.asList(OcrFormat.PDF, OcrFormat.DOC)));
		when(processMock.getOutputUriForFormat(OcrFormat.PDF)).thenReturn(new URI("file:/test/result.pdf"));
		when(processMock.getOutputUriForFormat(OcrFormat.DOC)).thenReturn(new URI("file:/test/result.doc"));
		String xml = createTicket();
		
		assertXpathExists("//ExportFormat[@OutputFileFormat='PDF']", xml);
		assertXpathExists("//ExportFormat[@OutputFileFormat='MSWord']", xml);
	}

	private String createTicket() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ticketSut.write(baos);
		
		String xml = baos.toString();
		System.out.println(xml);
		return xml;
	}

}
