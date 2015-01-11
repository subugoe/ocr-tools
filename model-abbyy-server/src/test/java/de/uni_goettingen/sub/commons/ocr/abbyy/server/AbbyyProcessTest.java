package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.Pause;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;

public class AbbyyProcessTest {

	private AbbyyProcess processSut;
	private AbbyyTicket ticketMock = mock(AbbyyTicket.class);
	private HotfolderManager hotManagerMock = mock(HotfolderManager.class);
	private Pause pauseMock = mock(Pause.class);
	private ProcessMergingObserver mergerMock = mock(ProcessMergingObserver.class);
	
	
	@Before
	public void beforeEachTest() throws Exception {
		AbbyyProcess processSutNoSpy = new AbbyyProcess();
		processSut = spy(processSutNoSpy);
		doReturn(ticketMock).when(processSut).createAbbyyTicket();
		doReturn(hotManagerMock).when(processSut).createHotfolderManager();
		processSut.setPause(pauseMock);
		processSut.setMerger(mergerMock);
	}

	@Test
	public void shouldInitializeTicket() throws URISyntaxException {
		processSut.initialize(validProps());
		
		verify(ticketMock).setRemoteInputFolder(new URI("http://test.com/input/"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAcceptIllegalUri() throws URISyntaxException {
		Properties props = validProps();
		props.setProperty("serverUrl", ">not-a-url");
		processSut.initialize(props);
	}

	@Test
	public void shouldRunOcr() throws IOException {
		processSut.initialize(validProps());

		processSut.run();
		
		verify(hotManagerMock).copyImagesToHotfolder(any(List.class));
		verify(hotManagerMock).retrieveResults(any(List.class));
		verify(mergerMock).update();
		
		assertTrue("Process should finish", processSut.getIsFinished());
	}
	
	@Test
	public void shouldFailWithTimeout() throws TimeoutException, IOException {
		doThrow(TimeoutException.class).when(hotManagerMock).waitForResults(anyLong(), anyLong(), any(List.class), any(URI.class));
		processSut.initialize(validProps());
		processSut.run();
		
		verify(hotManagerMock, never()).retrieveResults(any(List.class));
		assertTrue("Process must fail", processSut.hasFailed());
	}

	@Test
	public void shouldFailWithIO() throws IOException {
		doThrow(IOException.class).when(hotManagerMock).retrieveResults(any(List.class));
		processSut.initialize(validProps());
		processSut.run();
		
		assertTrue("Process must fail", processSut.hasFailed());
	}
	
	@Test
	public void shouldFailWithUriSyntax() throws IOException, URISyntaxException {
		doThrow(URISyntaxException.class).when(hotManagerMock).createAndSendTicket(any(AbbyyTicket.class), anyString());
		processSut.initialize(validProps());
		processSut.run();
		
		assertTrue("Process must fail", processSut.hasFailed());
	}
	
	private Properties validProps() {
		Properties props = new Properties();
		// file properties
		props.setProperty("serverUrl", "http://test.com/");
		props.setProperty("inputFolder", "input");
		props.setProperty("outputFolder", "output");
		props.setProperty("errorFolder", "error");
		props.setProperty("resultXmlFolder", "control");
		props.setProperty("maxMillisPerFile", "2000");
		props.setProperty("minMillisPerFile", "1000");
		props.setProperty("checkInterval", "300");
		return props;
	}

}
