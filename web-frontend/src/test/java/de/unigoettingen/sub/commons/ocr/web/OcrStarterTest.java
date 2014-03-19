package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

import static org.mockito.Mockito.*;

public class OcrStarterTest {

	private EngineProvider providerMock = mock(EngineProvider.class);
	private OCREngine engineMock = mock(OCREngine.class);
	private OCRProcess processMock = mock(OCRProcess.class);
	private OCROutput outputMock = mock(OCROutput.class);
	private OCRImage imageMock = mock(OCRImage.class);

	
	@BeforeClass
	public void beforeClass() throws Exception {
		when(providerMock.getFromContext(anyString())).thenReturn(engineMock);
		when(engineMock.newOcrProcess()).thenReturn(processMock);
		when(engineMock.newOcrOutput()).thenReturn(outputMock);
		when(engineMock.newOcrImage(any(URI.class))).thenReturn(imageMock);
	}

	@Test
	public void test() throws InterruptedException {
		OcrStarter ocrStarter = new OcrStarter();
		ocrStarter.setEngineProvider(providerMock);
		
		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		
		ocrStarter.run();
	}

	private OcrParameters validParams() {
		OcrParameters param = new OcrParameters();
		param.inputFolder = new File("src/test/resources/testInputs").getAbsolutePath();
		param.ocrEngine = "gbvAntiqua";
		param.languages = new String[]{"de"};
		param.textType = "NORMAL";
		param.outputFormats = new String[]{"PDF"};
		param.outputFolder = "/tmp";
		param.imageFormat = "tif";
		return param;
	}

}
