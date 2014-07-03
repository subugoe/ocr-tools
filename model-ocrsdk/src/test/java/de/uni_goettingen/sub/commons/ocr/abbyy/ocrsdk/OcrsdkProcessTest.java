package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;

public class OcrsdkProcessTest {

	private byte[] fakeImage = {0};
	private String resultXml = "<xml-document/>";
	private String resultTxt = "txt-document";
	
	private OcrsdkImage imageMock = mock(OcrsdkImage.class);
	private OcrsdkOutput outputMock = mock(OcrsdkOutput.class);
	private OcrsdkClient clientMock = mock(OcrsdkClient.class);
	
	private OcrsdkProcess process;
	
	@Before
	public void setUp() throws Exception {
		when(imageMock.getAsBytes()).thenReturn(fakeImage);
		
		InputStream xmlOutput = new ByteArrayInputStream(resultXml.getBytes());
		when(clientMock.getResultForFormat("xml")).thenReturn(xmlOutput);
		InputStream txtOutput = new ByteArrayInputStream(resultTxt.getBytes());
		when(clientMock.getResultForFormat("txt")).thenReturn(txtOutput);
		
		process = new OcrsdkProcess("user", "pass");
		
		
		process.setClient(clientMock);
	}

	//@Test
	public void test() {
		OcrsdkImage image = new OcrsdkImage(new File("src/test/resources/Picture_010.tif").toURI());
		OcrsdkOutput outputXml = new OcrsdkOutput();
		outputXml.setUri(new File("target/testResult.xml").toURI());
		OcrsdkOutput outputTxt = new OcrsdkOutput();
		outputTxt.setUri(new File("target/testResult.txt").toURI());
		OcrsdkProcess process = new OcrsdkProcess("", "");
		process.addImage(image);
		process.addOutput(OCRFormat.XML, outputXml);
		process.addOutput(OCRFormat.TXT, outputTxt);
		process.addLanguage(Locale.ENGLISH);
		process.addLanguage(Locale.GERMAN);
		process.start();
	}

	@Test
	public void usesTheRestClientCorrectly() {
		process.addImage(imageMock);
		process.addOutput(OCRFormat.XML, outputMock);
		process.start();
		
		verify(clientMock, times(1)).submitImage(fakeImage);
		verify(clientMock, times(1)).addExportFormat("xml");
		verify(clientMock, times(1)).processDocument();
	}

	@Test
	public void forwardsSeveralOutputFormats() {
		process.addImage(imageMock);
		process.addOutput(OCRFormat.XML, outputMock);
		process.addOutput(OCRFormat.TXT, outputMock);
		process.start();
		
		verify(clientMock, times(1)).addExportFormat("xml");
		verify(clientMock, times(1)).addExportFormat("txt");
	}

	@Test
	public void forwardsTwoImages() {
		process.addImage(imageMock);
		process.addImage(imageMock);
		process.addOutput(OCRFormat.XML, outputMock);
		process.start();
		
		verify(clientMock, times(2)).submitImage(fakeImage);
	}

	@Test
	public void forwardsTwoLanguages() {
		process.addImage(imageMock);
		process.addOutput(OCRFormat.XML, outputMock);
		process.addLanguage(Locale.ENGLISH);
		process.addLanguage(Locale.GERMAN);
		process.start();
		
		verify(clientMock, times(1)).addLanguage("English");
		verify(clientMock, times(1)).addLanguage("German");
	}

	@Test
	public void forwardsTextType() {
		process.addImage(imageMock);
		process.addOutput(OCRFormat.XML, outputMock);
		process.setTextType(OCRTextType.GOTHIC);
		process.start();
		
		verify(clientMock, times(1)).addTextType("gothic");
	}

	@Test
	public void canSaveReceivedXmlResult() throws IOException {
		process.addImage(imageMock);
		process.addOutput(OCRFormat.XML, outputMock);
		process.start();
		
		ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
		verify(outputMock, times(1)).save(argument.capture());

		InputStream is = argument.getValue();
		assertEquals("saved result document", "<xml-document/>", IOUtils.toString(is));
	}
	
	@Test
	public void canSaveTwoReceivedResults() throws IOException {
		process.addImage(imageMock);
		process.addOutput(OCRFormat.XML, outputMock);
		process.addOutput(OCRFormat.TXT, outputMock);
		process.start();
		
		ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
		verify(outputMock, times(2)).save(argument.capture());

		List<InputStream> iss = argument.getAllValues();
		assertEquals("saved xml document", "<xml-document/>", IOUtils.toString(iss.get(0)));
		assertEquals("saved text document", "txt-document", IOUtils.toString(iss.get(1)));
	}
	
}
