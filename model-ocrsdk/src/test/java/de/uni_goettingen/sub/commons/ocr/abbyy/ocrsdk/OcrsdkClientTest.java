package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class OcrsdkClientTest {

	private static String sdkServer = "http://cloud.ocrsdk.com/";
	private Http httpMock = mock(Http.class);
	private byte[] fakeImage = {0};
	private String responseToSubmitImage = "<response>"
			+ "<task id=\"some-id\"/>"
			+ "</response>";
	private String responseCompleted = "<response>"
			+ "<task status=\"Completed\" resultUrl=\"http://xml-result\"/>"
			+ "</response>";
	private String responseCompletedWith3Urls = "<response>"
			+ "<task status=\"Completed\" resultUrl=\"http://xml-result\" resultUrl2=\"http://txt-result\" resultUrl3=\"http://rtf-result\"/>"
			+ "</response>";
	private String responseProcessingFailed = "<response>"
			+ "<task status=\"ProcessingFailed\" error=\"Internal error\"/>"
			+ "</response>";
	private String xmlResult = "<xml-document/>";
	private String txtResult = "txt-document";
	private String rtfResult = "rtf-document";
	private OcrsdkClient client;
	
	@Before
	public void setUp() throws Exception {
		when(httpMock.submitPost(anyString(), any(byte[].class))).thenReturn(responseToSubmitImage);
		when(httpMock.submitGet(sdkServer + "getTaskStatus?taskId=some-id")).thenReturn(responseCompleted);

		ByteArrayInputStream xmlStream = new ByteArrayInputStream(xmlResult.getBytes());
		when(httpMock.submitGetWithoutAuthentication("http://xml-result")).thenReturn(xmlStream);
		ByteArrayInputStream txtStream = new ByteArrayInputStream(txtResult.getBytes());
		when(httpMock.submitGetWithoutAuthentication("http://txt-result")).thenReturn(txtStream);
		ByteArrayInputStream rtfStream = new ByteArrayInputStream(rtfResult.getBytes());
		when(httpMock.submitGetWithoutAuthentication("http://rtf-result")).thenReturn(rtfStream);
		
		client = new OcrsdkClient("", "");
		client.setHttp(httpMock);
	}

	@Test
	public void submittingOneImage() {
		client.submitImage(fakeImage);

		verify(httpMock, times(1)).submitPost(sdkServer + "submitImage", fakeImage);
	}

	@Test
	public void submittingTwoImages() {
		client.submitImage(fakeImage);
		client.submitImage(fakeImage);

		verify(httpMock, times(1)).submitPost(sdkServer + "submitImage", fakeImage);
		verify(httpMock, times(1)).submitPost(sdkServer + "submitImage?taskId=some-id", fakeImage);
	}

	@Test
	public void submittingThreeImages() {
		client.submitImage(fakeImage);
		client.submitImage(fakeImage);
		client.submitImage(fakeImage);

		verify(httpMock, times(1)).submitPost(sdkServer + "submitImage", fakeImage);
		verify(httpMock, times(2)).submitPost(sdkServer + "submitImage?taskId=some-id", fakeImage);
	}
	
	@Test
	public void processingOneImage() {
		client.submitImage(fakeImage);
		client.processDocument();
		
		verify(httpMock, times(1)).submitGet(sdkServer + "getTaskStatus?taskId=some-id");
	}

	@Test
	public void processingWithOneLanguage() {
		client.addLanguage("English");
		client.submitImage(fakeImage);
		client.processDocument();
		
		verify(httpMock, times(1)).submitGet(sdkServer + "processDocument?taskId=some-id&language=English");
	}

	@Test
	public void processingWithTwoLanguages() {
		client.addLanguage("English");
		client.addLanguage("German");
		client.submitImage(fakeImage);
		client.processDocument();
		
		verify(httpMock, times(1)).submitGet(sdkServer + "processDocument?taskId=some-id&language=English,German");
	}

	@Test
	public void processingWithOneTextType() {
		client.addTextType("normal");
		client.submitImage(fakeImage);
		client.processDocument();
		
		verify(httpMock, times(1)).submitGet(sdkServer + "processDocument?taskId=some-id&textType=normal");
	}

	@Test
	public void processingWithTwoTextTypes() {
		client.addTextType("normal");
		client.addTextType("gothic");
		client.submitImage(fakeImage);
		client.processDocument();
		
		verify(httpMock, times(1)).submitGet(sdkServer + "processDocument?taskId=some-id&textType=normal,gothic");
	}

	@Test
	public void processingWithOneExportFormat() {
		client.addExportFormat("xml");
		client.submitImage(fakeImage);
		client.processDocument();
		
		verify(httpMock, times(1)).submitGet(sdkServer + "processDocument?taskId=some-id&exportFormat=xml");
	}

	@Test
	public void processingWithThreeExportFormats() {
		client.addExportFormat("xml");
		client.addExportFormat("txt");
		client.addExportFormat("docx");
		client.submitImage(fakeImage);
		client.processDocument();
		
		verify(httpMock, times(1)).submitGet(sdkServer + "processDocument?taskId=some-id&exportFormat=xml,txt,docx");
	}

	@Test(expected=IllegalStateException.class)
	public void processingWithFourExportFormats() {
		client.addExportFormat("xml");
		client.addExportFormat("txt");
		client.addExportFormat("docx");
		client.addExportFormat("pdfa");
	}

	@Test
	public void completeOcrWithOneResult() throws IOException {
		client.addExportFormat("xml");
		client.submitImage(fakeImage);
		client.processDocument();
		
		InputStream is = client.getResultForFormat("xml");
		String xmlResult = IOUtils.toString(is);
		assertEquals("returned xml", "<xml-document/>", xmlResult);
	}
	
	@Test
	public void completeOcrWithThreeResults() throws IOException {
		when(httpMock.submitGet(sdkServer + "getTaskStatus?taskId=some-id")).thenReturn(responseCompletedWith3Urls);
		
		client.addExportFormat("xml");
		client.addExportFormat("txt");
		client.addExportFormat("rtf");
		client.submitImage(fakeImage);
		client.processDocument();
		
		InputStream rtf = client.getResultForFormat("rtf");
		String rtfResult = IOUtils.toString(rtf);
		assertEquals("returned rtf", "rtf-document", rtfResult);
		InputStream is = client.getResultForFormat("xml");
		String xmlResult = IOUtils.toString(is);
		assertEquals("returned xml", "<xml-document/>", xmlResult);
		InputStream txt = client.getResultForFormat("txt");
		String txtResult = IOUtils.toString(txt);
		assertEquals("returned txt", "txt-document", txtResult);
	}
	
	@Test(expected=IllegalStateException.class)
	public void completeOcrWithFailure() throws IOException {
		when(httpMock.submitGet(sdkServer + "getTaskStatus?taskId=some-id")).thenReturn(responseProcessingFailed);
		
		client.addExportFormat("xml");
		client.submitImage(fakeImage);
		client.processDocument();
		
	}
}
