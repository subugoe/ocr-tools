package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.merge.Merger;
import de.unigoettingen.sub.commons.ocr.util.merge.MergerProvider;

public class ProcessMergingObserverTest {

	private ProcessMergingObserver observerSut;
	private MergerProvider mergerProviderMock = mock(MergerProvider.class);
	private Merger mergerMock = mock(Merger.class);
	private FileAccess fileAccessMock = mock(FileAccess.class);
	private AbbyyProcess parentProcessMock = mock(AbbyyProcess.class);
	private AbbyyProcess subProcessMock1 = mock(AbbyyProcess.class);
	private AbbyyProcess subProcessMock2 = mock(AbbyyProcess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		observerSut = new ProcessMergingObserver();

		when(mergerProviderMock.createMerger(any(OcrFormat.class))).thenReturn(mergerMock);
		observerSut.setMergerProvider(mergerProviderMock);
		observerSut.setFileAccess(fileAccessMock);
		observerSut.setParentProcess(parentProcessMock);
		observerSut.addSubProcess(subProcessMock1);
		observerSut.addSubProcess(subProcessMock2);

		when(subProcessMock1.hasFinished()).thenReturn(true);
		when(subProcessMock2.hasFinished()).thenReturn(true);
}

	@Test
	public void shouldDoNothingWhenAllSubprocessesNotFinished() {
		when(subProcessMock1.hasFinished()).thenReturn(false);
		when(subProcessMock2.hasFinished()).thenReturn(false);
		
		observerSut.update();
		
		verifyZeroInteractions(parentProcessMock, mergerMock, fileAccessMock);
	}

	@Test
	public void shouldDoNothingWhenOneSubprocessNotFinished() {
		when(subProcessMock1.hasFinished()).thenReturn(false);
		when(subProcessMock2.hasFinished()).thenReturn(true);
		
		observerSut.update();
		
		verifyZeroInteractions(parentProcessMock, mergerMock, fileAccessMock);
	}

	@Test(expected=IllegalStateException.class)
	public void shouldBreakUpWhenOneFails() {
		when(subProcessMock1.hasFailed()).thenReturn(true);
		
		observerSut.update();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldMergeIntoOneAndDeleteTheParts() throws URISyntaxException, IOException {
		Set<OcrFormat> formats = new HashSet<OcrFormat>();
		formats.add(OcrFormat.TXT);
		when(parentProcessMock.getAllOutputFormats()).thenReturn(formats);
		when(subProcessMock1.getOutputUriForFormat(OcrFormat.TXT)).thenReturn(new URI("file:/part1.txt"));
		when(subProcessMock2.getOutputUriForFormat(OcrFormat.TXT)).thenReturn(new URI("file:/part2.txt"));
		when(parentProcessMock.getOutputUriForFormat(OcrFormat.TXT)).thenReturn(new URI("file:/result.txt"));
		
		observerSut.update();
		
		verify(mergerMock).merge(any(List.class), any(OutputStream.class));
		verify(fileAccessMock).deleteFile(new File("/part1.txt"));
		verify(fileAccessMock).deleteFile(new File("/part2.txt"));
	}

}
