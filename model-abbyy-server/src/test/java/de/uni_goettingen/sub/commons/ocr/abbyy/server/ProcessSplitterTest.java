package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OcrImage;

public class ProcessSplitterTest {

	private ProcessSplitter splitterSut;
	private ProcessMergingObserver mergerMock = mock(ProcessMergingObserver.class);
	private AbbyyProcess processMock = mock(AbbyyProcess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		splitterSut = new ProcessSplitter();
		splitterSut.setProcessMergingObserver(mergerMock);
		
	}

	@Test
	public void shouldNotSplitBecauseOfFewImages() {
		when(processMock.getNumberOfImages()).thenReturn(2);
		List<AbbyyProcess> splitResults = splitterSut.split(processMock, 2);
		
		assertEquals("Should not split a small process", processMock, splitResults.get(0));
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectProcessWithoutImages() {
		when(processMock.getNumberOfImages()).thenReturn(0);
		splitterSut.split(processMock, 1);
	}

	@Test
	public void shouldSplitOneProcessIntoTwo() throws URISyntaxException {
		when(processMock.getNumberOfImages()).thenReturn(2);
		when(processMock.getImages()).thenReturn(testImages());
		when(processMock.getName()).thenReturn("testMock");
		AbbyyProcess subProcessMock1 = mock(AbbyyProcess.class);
		AbbyyProcess subProcessMock2 = mock(AbbyyProcess.class);
		when(processMock.createSubProcess()).thenReturn(subProcessMock1, subProcessMock2);
		
		List<AbbyyProcess> splitResults = splitterSut.split(processMock, 1);
		
		assertEquals("Number of split subprocesses", 2, splitResults.size());
		assertSame("First subprocess", subProcessMock1, splitResults.get(0));
		assertSame("Second subprocess", subProcessMock2, splitResults.get(1));
		verify(subProcessMock1).setName("testMock_1of2");
		verify(subProcessMock2).setName("testMock_2of2");
	}
	
	private List<OcrImage> testImages() throws URISyntaxException {
		AbbyyImage image1 = new AbbyyImage();
		image1.setLocalUri(new URI("file:/1.tif"));
		AbbyyImage image2 = new AbbyyImage();
		image2.setLocalUri(new URI("file:/2.tif"));

		return Arrays.asList(new OcrImage[]{image1, image2});
	}

}
