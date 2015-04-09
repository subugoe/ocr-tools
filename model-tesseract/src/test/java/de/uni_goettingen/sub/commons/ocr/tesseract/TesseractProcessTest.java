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

	//@Test
	public void test() throws Exception {
		processSpy.addImage(new URI("file:/test.tif"));
		processSpy.addOutput(OcrFormat.TXT);
		
		processSpy.start();
	}

}
