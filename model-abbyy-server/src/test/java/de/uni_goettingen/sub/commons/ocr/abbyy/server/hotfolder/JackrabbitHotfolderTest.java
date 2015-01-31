package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
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
	public void shouldCopyLocalToRemote() throws IOException, URISyntaxException {
		jackrabbitSut.copyFile(new URI("file:/test.jpg"), new URI("http://localhost/test.jpg"));
		
		verify(httpClientMock).executeMethod(any(PutMethod.class));
	}

}
