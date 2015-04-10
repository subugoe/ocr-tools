package de.uni_goettingen.sub.commons.ocr.tesseract;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.merge.Merger;
import de.unigoettingen.sub.commons.ocr.util.merge.MergerProvider;

public class TesseractProcessTest {

	private TesseractProcess processSut = new TesseractProcess();
	private TesseractProcess processSpy = spy(processSut);
	private Tesseract tesseractMock = mock(Tesseract.class);
	
	private BeanProvider fileProviderMock = mock(BeanProvider.class);
	private FileAccess fileAccessMock = mock(FileAccess.class);
	
	private MergerProvider mergerProviderMock = mock(MergerProvider.class);
	private Merger mergerMock = mock(Merger.class);
	
	@Before
	public void setUp() throws Exception {
		doReturn(tesseractMock).when(processSpy).createTesseract(any(File.class), any(File.class));
		
		when(fileProviderMock.getFileAccess()).thenReturn(fileAccessMock);
		when(mergerProviderMock.createMerger(any(OcrFormat.class))).thenReturn(mergerMock);
		
		processSpy.setBeanProvider(fileProviderMock);
		processSpy.setMergerProvider(mergerProviderMock);
	}

	@Test
	public void shouldAddOneImage() throws Exception {
		assertEquals("Number of images", 0, processSpy.getNumberOfImages());

		processSpy.addImage(new URI("file:/test.tif"));
		
		assertEquals("Number of images", 1, processSpy.getNumberOfImages());
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectRemoteUriForImage() throws Exception {
		processSpy.addImage(new URI("http://test.com/test.tif"));
	}

	@Test
	public void shouldAddOneOutput() throws Exception {
		assertEquals("Number of outputs", 0, processSpy.getAllOutputFormats().size());

		processSpy.addOutput(OcrFormat.HOCR);
		
		assertEquals("Number of outputs", 1, processSpy.getAllOutputFormats().size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectUnsupportedFormatForOutput() throws Exception {
		processSpy.addOutput(OcrFormat.PDF);
	}

	@Test
	public void shouldDoNothingWithoutImages() throws Exception {
		processSpy.addOutput(OcrFormat.HOCR);
		processSpy.start();
		
		verify(tesseractMock, never()).execute();
	}

	@Test
	public void shouldDoNothingWithoutOutputs() throws Exception {
		processSpy.addImage(new URI("file:/test.tif"));
		processSpy.start();
		
		verify(tesseractMock, never()).execute();
	}

	@Test
	public void shouldRunTesseract() throws Exception {
		processSpy.setName("testProcess");
		processSpy.addImage(new URI("file:/test.tif"));
		processSpy.addOutput(OcrFormat.TXT);
		
		processSpy.start();

		verify(tesseractMock).execute();
	}

}
