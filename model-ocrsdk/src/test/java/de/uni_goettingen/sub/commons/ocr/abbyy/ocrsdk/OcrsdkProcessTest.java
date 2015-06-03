package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;

public class OcrsdkProcessTest {

	private byte[] fakeImage = {0};
	private String resultXml = "<xml-document/>";
	private String resultTxt = "txt-document";
	
	private OcrsdkImage imageMock = mock(OcrsdkImage.class);
	private OcrsdkOutput outputXmlMock = mock(OcrsdkOutput.class);
	private OcrsdkOutput outputTxtMock = mock(OcrsdkOutput.class);
	private OcrsdkClient clientMock = mock(OcrsdkClient.class);
	
	private OcrsdkProcess process;
	
	@Before
	public void setUp() throws Exception {
		when(imageMock.getAsBytes()).thenReturn(fakeImage);
		
		InputStream xmlOutput = new ByteArrayInputStream(resultXml.getBytes());
		when(clientMock.getResultForFormat("xml")).thenReturn(xmlOutput);
		InputStream txtOutput = new ByteArrayInputStream(resultTxt.getBytes());
		when(clientMock.getResultForFormat("txt")).thenReturn(txtOutput);
		
		when(outputXmlMock.getFormat()).thenReturn(OcrFormat.XML);
		when(outputTxtMock.getFormat()).thenReturn(OcrFormat.TXT);
		
		process = new OcrsdkProcess("user", "pass");
		
		
		process.setClient(clientMock);
	}

	//@Test
	public void test() {
		OcrsdkImage image = new OcrsdkImage();
		image.setLocalUri(new File("src/test/resources/Picture_010.tif").toURI());
		OcrsdkOutput outputXml = new OcrsdkOutput();
		outputXml.setLocalUri(new File("target/testResult.xml").toURI());
		outputXml.setFormat(OcrFormat.XML);
		OcrsdkOutput outputTxt = new OcrsdkOutput();
		outputTxt.setLocalUri(new File("target/testResult.txt").toURI());
		outputTxt.setFormat(OcrFormat.TXT);
		OcrsdkProcess process = new OcrsdkProcess("", "");
		process.addImage(image.getLocalUri());
//		process.addOutput(outputXml);
//		process.addOutput(outputTxt);
		process.addLanguage(Locale.ENGLISH);
		process.addLanguage(Locale.GERMAN);
		process.start();
	}

	//@Test
	public void usesTheRestClientCorrectly() {
//		process.addImage(imageMock);
//		process.addOutput(outputXmlMock);
		process.start();
		
		verify(clientMock, times(1)).submitImage(fakeImage);
		verify(clientMock, times(1)).addExportFormat("xml");
		verify(clientMock, times(1)).processDocument();
	}

	//@Test
	public void forwardsSeveralOutputFormats() {
//		process.addImage(imageMock);
//		process.addOutput(outputXmlMock);
//		process.addOutput(outputTxtMock);
		process.start();
		
		verify(clientMock, times(1)).addExportFormat("xml");
		verify(clientMock, times(1)).addExportFormat("txt");
	}

	//@Test
	public void forwardsTwoImages() {
//		process.addImage(imageMock);
//		process.addImage(imageMock);
//		process.addOutput(outputXmlMock);
		process.start();
		
		verify(clientMock, times(2)).submitImage(fakeImage);
	}

	//@Test
	public void forwardsTwoLanguages() {
//		process.addImage(imageMock);
//		process.addOutput(outputXmlMock);
		process.addLanguage(Locale.ENGLISH);
		process.addLanguage(Locale.GERMAN);
		process.start();
		
		verify(clientMock, times(1)).addLanguage("English");
		verify(clientMock, times(1)).addLanguage("German");
	}

//	@Test
	public void forwardsTextType() {
//		process.addImage(imageMock);
//		process.addOutput(outputXmlMock);
		process.setTextType(OcrTextType.GOTHIC);
		process.start();
		
		verify(clientMock, times(1)).addTextType("gothic");
	}

//	@Test
	public void canSaveReceivedXmlResult() throws IOException {
//		process.addImage(imageMock);
//		process.addOutput(outputXmlMock);
		process.start();
		
		ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
		verify(outputXmlMock, times(1)).save(argument.capture());

		InputStream is = argument.getValue();
		assertEquals("saved result document", "<xml-document/>", IOUtils.toString(is));
	}
	
//	@Test
	public void canSaveTwoReceivedResults() throws IOException {
//		process.addImage(imageMock);
//		process.addOutput(outputXmlMock);
//		process.addOutput(outputTxtMock);
		process.start();
		
		ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
		verify(outputXmlMock, times(1)).save(argument.capture());

		InputStream iss = argument.getValue();
		assertEquals("saved xml document", "<xml-document/>", IOUtils.toString(iss));
		
		verify(outputTxtMock, times(1)).save(argument.capture());

		iss = argument.getValue();
		assertEquals("saved text document", "txt-document", IOUtils.toString(iss));
	}
	
}
