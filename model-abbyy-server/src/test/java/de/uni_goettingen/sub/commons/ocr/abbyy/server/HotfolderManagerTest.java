package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.unigoettingen.sub.commons.ocr.util.Pause;

public class HotfolderManagerTest {

	private HotfolderManager managerSut;
	private Hotfolder hotfolderMock = mock(Hotfolder.class);
	private Pause pauseMock = mock(Pause.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		managerSut = new HotfolderManager(hotfolderMock);
		managerSut.setPause(pauseMock);
	}

	@Test
	public void shouldDeleteRemoteOutputs() throws URISyntaxException, IOException {
		List<OcrOutput> outputsToDelete = validOutputs();
		managerSut.deleteOutputs(outputsToDelete);
		
		verify(hotfolderMock, times(2)).deleteIfExists(any(URI.class));
	}

	@Test
	public void shouldDeleteRemoteImages() throws URISyntaxException, IOException {
		List<OcrImage> imagesToDelete = validImages();
		managerSut.deleteImages(imagesToDelete);
		
		verify(hotfolderMock, times(4)).deleteIfExists(any(URI.class));
	}

	@Test
	public void shouldCopyImages() throws URISyntaxException, IOException {
		List<OcrImage> imagesToCopy = validImages();
		managerSut.copyImagesToHotfolder(imagesToCopy);
		
		verify(hotfolderMock).copyFile(new URI("file:/01.tif"), new URI("http://test/01.tif"));
		verify(hotfolderMock).copyFile(new URI("file:/02.tif"), new URI("http://test/02.tif"));
	}

	@Test
	public void shouldMoveResultOutputs() throws URISyntaxException, IOException {
		List<OcrOutput> outputsToMove = validOutputs();
		managerSut.retrieveResults(outputsToMove);
		
		verify(hotfolderMock).copyFile(new URI("http://test/out.xml"), new URI("file:/out.xml"));
		verify(hotfolderMock).deleteIfExists(new URI("http://test/out.xml"));
		verify(hotfolderMock).copyFile(new URI("http://test/out.txt"), new URI("file:/out.txt"));
		verify(hotfolderMock).deleteIfExists(new URI("http://test/out.txt"));
	}

	@Test
	public void shouldSendTicket() throws URISyntaxException, IOException {
		AbbyyTicket ticketMock = mock(AbbyyTicket.class);
		when(ticketMock.getRemoteInputUri()).thenReturn(new URI("http://test/ticket.xml"));
		
		managerSut.createAndSendTicket(ticketMock, "ticket");
		
		verify(hotfolderMock).copyTmpFile("ticket.xml", new URI("http://test/ticket.xml"));
	}
	
	@Test
	public void shouldWaitForResultsVeryShort() throws URISyntaxException, TimeoutException, IOException {
		List<OcrOutput> outputsToWaitFor = validOutputs();
		when(hotfolderMock.exists(any(URI.class))).thenReturn(true);
		when(hotfolderMock.exists(new URI("http://test/error.xml.result.xml"))).thenReturn(false);
		
		managerSut.waitForResults(10, 1, outputsToWaitFor, new URI("http://test/error.xml.result.xml"));
		
		verify(pauseMock, never()).forMilliseconds(anyLong());
	}
	
	@Test
	public void shouldWaitOneIterationForEachFile() throws URISyntaxException, IOException, TimeoutException {
		List<OcrOutput> outputsToWaitFor = validOutputs();
		when(hotfolderMock.exists(new URI("http://test/out.xml"))).thenReturn(false, true);
		when(hotfolderMock.exists(new URI("http://test/out.txt"))).thenReturn(false, true);
		when(hotfolderMock.exists(new URI("http://test/error.xml.result.xml"))).thenReturn(false);
		
		managerSut.waitForResults(10, 1, outputsToWaitFor, new URI("http://test/error.xml.result.xml"));
		
		verify(pauseMock, times(2)).forMilliseconds(1);
	}
	
	@Test(expected=TimeoutException.class)
	public void shouldProvokeTimeout() throws URISyntaxException, IOException, TimeoutException {
		List<OcrOutput> outputsToWaitFor = validOutputs();
		when(hotfolderMock.exists(new URI("http://test/out.xml"))).thenReturn(false);
		
		managerSut.waitForResults(1, 1, outputsToWaitFor, new URI("http://test/error.xml.result.xml"));
	}
	
	@Test(expected=IOException.class)
	public void shouldFindErrorFile() throws URISyntaxException, IOException, TimeoutException {
		List<OcrOutput> outputsToWaitFor = validOutputs();
		when(hotfolderMock.exists(new URI("http://test/error.xml.result.xml"))).thenReturn(true);
		
		managerSut.waitForResults(1, 1, outputsToWaitFor, new URI("http://test/error.xml.result.xml"));
	}
	
	@Test
	public void shouldAcceptMaxSizeOfAllFiles() throws IOException, URISyntaxException {
		when(hotfolderMock.getTotalSize(any(URI.class))).thenReturn(1L).thenReturn(2L).thenReturn(3L);
		
		managerSut.checkIfEnoughSpace(6, new URI("http://test/in"), new URI("http://test/out"), new URI("http://test/error"));
	}
	
	@Test(expected=IllegalStateException.class)
	public void shouldHaveTooLittleSpace() throws IOException, URISyntaxException {
		when(hotfolderMock.getTotalSize(any(URI.class))).thenReturn(1L).thenReturn(2L).thenReturn(3L);
		
		managerSut.checkIfEnoughSpace(5, new URI("http://test/in"), new URI("http://test/out"), new URI("http://test/error"));
	}
	
	@Test
	public void shouldReadErrorFromResultXml() throws IOException, URISyntaxException {
		XmlParser parserMock = mock(XmlParser.class);
		managerSut.setXmlParser(parserMock);
		when(hotfolderMock.exists(any(URI.class))).thenReturn(true);
		
		managerSut.readFromErrorFile(new URI("http://error.xml.result.xml"), "testBook");
		
		verify(parserMock).readErrorFromResultXml(null, "testBook");
	}
	
	private List<OcrImage> validImages() throws URISyntaxException {
		AbbyyImage image1 = new AbbyyImage();
		image1.setRemoteUri(new URI("http://test/01.tif"));
		image1.setErrorUri(new URI("http://error/01.tif"));
		image1.setLocalUri(new URI("file:/01.tif"));
		AbbyyImage image2 = new AbbyyImage();
		image2.setRemoteUri(new URI("http://test/02.tif"));
		image2.setErrorUri(new URI("http://error/02.tif"));
		image2.setLocalUri(new URI("file:/02.tif"));
		
		return Arrays.asList((OcrImage)image1, (OcrImage)image2);
	}
	private List<OcrOutput> validOutputs() throws URISyntaxException {
		AbbyyOutput out1 = new AbbyyOutput();
		out1.setRemoteUri(new URI("http://test/out.xml"));
		out1.setLocalUri(new URI("file:/out.xml"));
		AbbyyOutput out2 = new AbbyyOutput();
		out2.setRemoteUri(new URI("http://test/out.txt"));
		out2.setLocalUri(new URI("file:/out.txt"));
		
		return Arrays.asList((OcrOutput)out1, (OcrOutput)out2);
	}


}
