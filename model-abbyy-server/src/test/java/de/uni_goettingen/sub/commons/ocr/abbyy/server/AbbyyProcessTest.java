package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.Pause;

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

	
	
	
	private Properties validProps() {
		Properties props = new Properties();
		// file properties
		props.setProperty("serverUrl", "http://test.com/");
		props.setProperty("inputFolder", "input");
		props.setProperty("outputFolder", "output");
		props.setProperty("errorFolder", "error");
		props.setProperty("resultXmlFolder", "control");
		return props;
	}

}
