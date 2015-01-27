package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
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
		doReturn(ticketMock).when(processSut).createAbbyyTicket(any(AbbyyProcess.class));
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
		Answer<Object> withOneSecondDelay = new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Exception {
				Thread.sleep(1000);
				return null;
			}
		};
		//doAnswer(withOneSecondDelay).when(pauseMock).forMilliseconds(anyLong());
		
		processSut.initialize(validProps());

		processSut.run();
		
		verify(hotManagerMock).copyImagesToHotfolder(anyListOf(OcrImage.class));
		verify(hotManagerMock).retrieveResults(anyListOf(OcrOutput.class));
		verify(mergerMock).update();
		
		assertTrue("Process should finish", processSut.hasFinished());
	}
	
	@Test
	public void shouldFailWithTimeout() throws TimeoutException, IOException {
		doThrow(TimeoutException.class).when(hotManagerMock).waitForResults(anyLong(), anyLong(), anyListOf(OcrOutput.class), any(URI.class));
		processSut.initialize(validProps());
		processSut.run();
		
		verify(hotManagerMock, never()).retrieveResults(anyListOf(OcrOutput.class));
		assertTrue("Process must fail", processSut.hasFailed());
	}

	@Test
	public void shouldFailWithIO() throws IOException {
		doThrow(new IOException("IO problem")).when(hotManagerMock).retrieveResults(anyListOf(OcrOutput.class));
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
	
	@Test
	public void shouldAddOneImage() throws URISyntaxException {
		processSut.initialize(validProps());
		assertEquals("Collection size before", 0, processSut.getRemoteImageNames().size());
		processSut.addImage(new URI("file://test.tif"));
		
		assertEquals("Collection size after", 1, processSut.getRemoteImageNames().size());
	}
	
	@Test
	public void shouldNameImageCorrectly() throws URISyntaxException {
		processSut.initialize(validProps());
		processSut.setName("myProcess");
		processSut.addImage(new URI("file://test.tif"));
		
		String imageName = processSut.getRemoteImageNames().get(0);
		assertEquals("Image name", "myProcess-test.tif", imageName);
		
	}
	
	@Test
	public void shouldAddOutputsAndResultXml() {
		processSut.initialize(validProps());
		processSut.addOutput(OcrFormat.TXT);
		
		assertEquals("Number of outputs", 2, processSut.getAllOutputFormats().size());

		processSut.addOutput(OcrFormat.XML);
		
		assertEquals("Second number of outputs", 3, processSut.getAllOutputFormats().size());
	}
	
	@Test
	public void shouldMakeACopyWithEmptyImages() throws URISyntaxException {
		processSut.initialize(validProps());
		processSut.addImage(new URI("file://test.tif"));
		
		AbbyyProcess copy = processSut.createSubProcess();
		
		assertEquals("Images must be empty", 0, copy.getImages().size());
	}
	
	@Test
	public void shouldBeEqualWithSameProcessId() {
		AbbyyProcess firstProcess = new AbbyyProcess();
		AbbyyProcess secondProcess = new AbbyyProcess();
		firstProcess.setProcessId("id1");
		secondProcess.setProcessId("id2");
		
		assertFalse("Must not be equal", firstProcess.equals(secondProcess));
		
		firstProcess.setProcessId("id1");
		secondProcess.setProcessId("id1");
		
		assertTrue("Must be equal", firstProcess.equals(secondProcess));
		
	}
	
	@Test
	public void shouldBeStartableWithImagesAndOutputs() throws URISyntaxException {
		processSut.initialize(validProps());
		assertFalse("Not startable yet", processSut.hasImagesAndOutputs());
		
		processSut.addOutput(OcrFormat.XML);
		assertFalse("Still not startable", processSut.hasImagesAndOutputs());
		
		processSut.addImage(new URI("file://test.tif"));
		assertTrue("Now must be startable", processSut.hasImagesAndOutputs());
	}
	
	@Test
	public void shouldGetCorrectOutputUri() throws URISyntaxException {
		processSut.initialize(validProps());
		processSut.setOutputDir(new File("/tmp"));
		processSut.setName("result");
		
		URI nullUri = processSut.getOutputUriForFormat(OcrFormat.XML);
		assertNull(nullUri);
		
		processSut.addOutput(OcrFormat.XML);
		URI xmlUri = processSut.getOutputUriForFormat(OcrFormat.XML);
		assertEquals(new URI("file:/tmp/result.xml"), xmlUri);
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
