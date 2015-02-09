package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.Pause;

public class JackrabbitHotfolderTest {

	private FileAccess fileAccessMock = mock(FileAccess.class);
	private Pause pauseMock = mock(Pause.class);
	private HttpClient httpClientMock = mock(HttpClient.class);
	private JackrabbitHotfolder jackrabbitSut;
	
	@Before
	public void beforeEachTest() throws IOException {
		jackrabbitSut = new JackrabbitHotfolder();
		
		when(fileAccessMock.fileExists(any(File.class))).thenReturn(true);
		when(httpClientMock.executeMethod(any(HttpMethod.class))).thenReturn(200);
		
		jackrabbitSut.setFileAccess(fileAccessMock);
		jackrabbitSut.setHttpClient(httpClientMock);
		jackrabbitSut.setPause(pauseMock);
	}
	
	@Test
	public void shouldUploadToRemote() throws IOException, URISyntaxException {
		jackrabbitSut.upload(new URI("file:/test.jpg"), new URI("http://localhost/test.jpg"));
		
		verify(httpClientMock).executeMethod(any(PutMethod.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldFailWithNotExistingFile() throws IOException, URISyntaxException {
		when(fileAccessMock.fileExists(any(File.class))).thenReturn(false);
		
		jackrabbitSut.upload(new URI("file:/test.jpg"), new URI("http://localhost/test.jpg"));
	}

	@Test(expected=IllegalStateException.class)
	public void shouldFailWhenIllegalStatusCode() throws IOException, URISyntaxException {
		when(httpClientMock.executeMethod(any(HttpMethod.class))).thenReturn(403);
		
		jackrabbitSut.upload(new URI("file:/test.jpg"), new URI("http://localhost/test.jpg"));
	}

	@Test
	public void shouldIgnoreOneIllegalStatusCode() throws IOException, URISyntaxException {
		when(httpClientMock.executeMethod(any(HttpMethod.class))).thenReturn(403, 200);
		
		jackrabbitSut.upload(new URI("file:/test.jpg"), new URI("http://localhost/test.jpg"));
		
		verify(httpClientMock, times(2)).executeMethod(any(PutMethod.class));
	}

	@Test
	public void shouldDownloadToLocal() throws IOException, URISyntaxException {
		jackrabbitSut.download(new URI("http://localhost/test.jpg"), new URI("file:/test.jpg"));
		
		verify(httpClientMock).executeMethod(any(GetMethod.class));
		verify(fileAccessMock).copyStreamToFile(any(InputStream.class), any(File.class));
	}
	
	@Test
	public void shouldDelete() throws IOException, URISyntaxException {
		jackrabbitSut.delete(new URI("http://localhost/test.jpg"));
		
		verify(httpClientMock).executeMethod(any(DeleteMethod.class));
	}
	
	@Test
	public void uriShouldExist() throws HttpException, IOException, URISyntaxException {
		when(httpClientMock.executeMethod(any(HeadMethod.class))).thenReturn(200);
		
		assertTrue("URI must exist", jackrabbitSut.exists(new URI("http://localhost/test.tif")));
	}

	@Test
	public void uriShouldNotExist() throws HttpException, IOException, URISyntaxException {
		when(httpClientMock.executeMethod(any(HeadMethod.class))).thenReturn(401);
		
		assertFalse("URI must not exist", jackrabbitSut.exists(new URI("http://localhost/test.tif")));
	}
	
	@Test
	public void shouldAskForUsedSpace() throws IOException, URISyntaxException, DavException {
		JackrabbitHotfolder jackrabbitSpy = spy(jackrabbitSut);
		MultiStatus multiMock = mock(MultiStatus.class);
		when(multiMock.getResponses()).thenReturn(new MultiStatusResponse[]{});
		doReturn(multiMock).when(jackrabbitSpy).getMultiStatus(any(PropFindMethod.class));
		
		long spaceInBytes = jackrabbitSpy.getUsedSpace(new URI("http://localhost/input"));
		
		assertEquals(0, spaceInBytes);
		verify(httpClientMock).executeMethod(any(PropFindMethod.class));
	}

	@Test(expected=IOException.class)
	public void shouldFailToGetUsedSpace() throws IOException, URISyntaxException, DavException {
		JackrabbitHotfolder jackrabbitSpy = spy(jackrabbitSut);
		doThrow(new DavException(0)).when(jackrabbitSpy).getMultiStatus(any(PropFindMethod.class));
		
		jackrabbitSpy.getUsedSpace(new URI("http://localhost/input"));
	}
	
	@Test
	public void shouldTryToGetResponse() throws IOException, URISyntaxException {
		byte[] response = jackrabbitSut.getResponse(new URI("http://localhost/test.xml"));
		
		verify(httpClientMock).executeMethod(any(GetMethod.class));
		assertFalse("Response may not be null", response == null);
		assertTrue("Response should be empty", response.length == 0);
	}

}
