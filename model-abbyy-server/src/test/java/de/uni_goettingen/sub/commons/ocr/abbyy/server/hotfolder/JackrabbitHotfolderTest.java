package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.httpclient.HttpClient;
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
	public void beforeEachTest() {
		jackrabbitSut = new JackrabbitHotfolder();
		
		jackrabbitSut.setFileAccess(fileAccessMock);
		jackrabbitSut.setHttpClient(httpClientMock);
		jackrabbitSut.setPause(pauseMock);
	}
	
	@Test
	public void test() {
		
	}

}
