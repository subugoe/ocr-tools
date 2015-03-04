package de.unigoettingen.sub.ocr.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.OcrParameters;

public class OcrEngineStarterTest {

	private OcrEngineStarter starterSut = new OcrEngineStarter();
	private FactoryProvider providerMock = mock(FactoryProvider.class);
	private OcrFactory factoryMock = mock(OcrFactory.class);
	private BeanProvider beanProviderMock = mock(BeanProvider.class);
	private FileAccess fileAccessMock = mock(FileAccess.class);
	private OcrEngine engineMock = mock(OcrEngine.class);
	private OcrProcess processMock = mock(OcrProcess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		when(providerMock.createFactory(anyString(), any(Properties.class))).thenReturn(factoryMock);
		when(beanProviderMock.getFileAccess()).thenReturn(fileAccessMock);
		when(factoryMock.createEngine()).thenReturn(engineMock);
		when(factoryMock.createProcess()).thenReturn(processMock);
		
		starterSut.setFactoryProvider(providerMock);
		starterSut.setBeanProvider(beanProviderMock);
	}

	@Test
	public void shouldStartEngineDespiteNoFolders() {
		when(fileAccessMock.getAllFolders(anyString(), any(String[].class))).thenReturn(new File[]{});
		
		starterSut.startOcrWithParams(validParams());
		
		verify(engineMock).recognize();
	}
	
	@Test
	public void shouldStartWithBookAndImage() {
		prepareOneBookWithOneImage();
		
		starterSut.startOcrWithParams(validParams());
		
		verify(processMock).setName("book1");
		verify(engineMock).recognize();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectIllegalOutputFormat() {
		prepareOneBookWithOneImage();
		
		OcrParameters params = validParams();
		params.outputFormats = new String[]{"illegal"};
		starterSut.startOcrWithParams(params);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectIllegalTextType() {
		prepareOneBookWithOneImage();
		
		OcrParameters params = validParams();
		params.inputTextType = "illegal";
		starterSut.startOcrWithParams(params);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectIllegalPriority() {
		prepareOneBookWithOneImage();
		
		OcrParameters params = validParams();
		params.priority = "-10";
		starterSut.startOcrWithParams(params);
	}
	
	@Test
	public void shouldReturnZeroSeconds() {
		assertEquals("Job duration", 0, starterSut.getEstimatedDurationInSeconds());
	}

	@Test
	public void shouldDelegateDurationRequest() {
		prepareOneBookWithOneImage();
		
		starterSut.startOcrWithParams(validParams());
		starterSut.getEstimatedDurationInSeconds();
		
		verify(engineMock).getEstimatedDurationInSeconds();
	}

	private void prepareOneBookWithOneImage() {
		File[] book = new File[]{new File("tmp/book1")};
		when(fileAccessMock.getAllFolders(anyString(), any(String[].class))).thenReturn(book);
		File[] image = new File[]{new File("tmp/book1/image1.jpg")};
		when(fileAccessMock.getAllImagesFromFolder(any(File.class), any(String[].class))).thenReturn(image);
	}
	
	private OcrParameters validParams() {
		OcrParameters params = new OcrParameters();
		params.inputFolder = "tmp/book1";
		params.outputFolder = "tmp/output";
		params.outputFormats = new String[]{"pdf", "xml"};
		params.inputLanguages = new String[]{"de", "en"};
		params.inputTextType = "normal";
		return params;
	}


}
