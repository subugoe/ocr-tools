package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OcrStarterTest {

	private EngineProvider providerMock;
	private OCREngine engineMock;
	private OCRProcess processMock;
	private OCROutput outputMock;
	private OCRImage imageMock;
	private OcrStarter ocrStarter;
	
	@Captor
	private ArgumentCaptor<Map<String,String>> optionsCaptor;
	
	@Before
	public void before() throws Exception {
		providerMock = mock(EngineProvider.class);
		engineMock = mock(OCREngine.class);
		processMock = mock(OCRProcess.class);
		outputMock = mock(OCROutput.class);
		imageMock = mock(OCRImage.class);
		when(providerMock.getFromContext(anyString())).thenReturn(engineMock);
		when(engineMock.newOcrProcess()).thenReturn(processMock);
		when(engineMock.newOcrOutput()).thenReturn(outputMock);
		when(engineMock.newOcrImage(any(URI.class))).thenReturn(imageMock);

		ocrStarter = new OcrStarter();
		ocrStarter.setEngineProvider(providerMock);
	}

	@Test
	public void withAntiquaEngine() throws InterruptedException {
		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		
		ocrStarter.run();
		
		verify(providerMock).getFromContext("abbyy-multiuser");
		verify(engineMock).newOcrProcess();
		verify(engineMock).setOptions(anyMapOf(String.class, String.class));
		verify(processMock).setName("book1");
		verify(processMock).setOcrImages(anyListOf(OCRImage.class));
		verify(processMock).setPriority(OCRPriority.NORMAL);
		verify(processMock).setLanguages(anySetOf(Locale.class));
		verify(processMock).setTextType(OCRTextType.NORMAL);
		verify(processMock).setSplitProcess(true);
		verify(processMock).addOutput(eq(OCRFormat.PDF), any(OCROutput.class));
		verify(outputMock).setUri(any(URI.class));
		verify(outputMock).setlocalOutput(anyString());
		verify(imageMock, times(2)).setSize(0L);
	}
	
	@Test
	public void withFrakturEngine() {
		OcrParameters param = validParams();
		param.ocrEngine = "gbvFraktur";
		ocrStarter.setParameters(param);
		
		ocrStarter.run();

		verify(providerMock).getFromContext("abbyy-multiuser");
	}

	@Test
	public void withAbbyyCloudEngine() {
		OcrParameters param = validParams();
		param.ocrEngine = "abbyyCloud";
		ocrStarter.setParameters(param);
		
		ocrStarter.run();

		verify(providerMock).getFromContext("ocrsdk");
	}

	@Test(expected=IllegalArgumentException.class)
	public void withUnknownEngine() {
		OcrParameters param = validParams();
		param.ocrEngine = "notAnEngine";
		ocrStarter.setParameters(param);
		
		ocrStarter.run();
	}

	@Test
	public void withAnotherUser() {
		OcrParameters param = validParams();
		param.user = "newUser";
		ocrStarter.setParameters(param);
		
		ocrStarter.run();
		
		verify(engineMock).setOptions(optionsCaptor.capture());
		String capturedUser = optionsCaptor.getValue().get("user");
		assertEquals("captured user", "newUser", capturedUser);
	}

	@Test
	public void withAnotherPassword() {
		OcrParameters param = validParams();
		param.password = "newPassword";
		ocrStarter.setParameters(param);
		
		ocrStarter.run();
		
		verify(engineMock).setOptions(optionsCaptor.capture());
		String capturedPassword = optionsCaptor.getValue().get("password");
		assertEquals("captured password", "newPassword", capturedPassword);
	}
	
	@Test
	public void checkingValidParams() {
		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertEquals("validation message", "OK", validation);
	}

	@Test
	public void checkingParamsWithoutInputFolder() {
		OcrParameters param = validParams();
		param.inputFolder = "";
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("No input folder"));
	}

	@Test
	public void checkingParamsInvalidInputFolder() {
		OcrParameters param = validParams();
		param.inputFolder = "not/absolute";
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Input folder must be absolute"));
	}

	@Test
	public void checkingParamsWithoutOutputFolder() {
		OcrParameters param = validParams();
		param.outputFolder = null;
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("No output folder"));
	}

	@Test
	public void checkingParamsInvalidOutputFolder() {
		OcrParameters param = validParams();
		param.outputFolder = "not/absolute";
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Output folder must be absolute"));
	}

	@Test
	public void checkingParamsWithoutEmail() {
		OcrParameters param = validParams();
		param.email = null;
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("No email"));
	}

	@Test
	public void checkingParamsInvalidEmail() {
		OcrParameters param = validParams();
		param.email = "invalid";
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Invalid email"));
	}

	@Test
	public void checkingParamsWithoutLanguage() {
		OcrParameters param = validParams();
		param.languages = new String[]{};
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("No language"));
	}

	@Test
	public void checkingParamsWithoutOutputFormat() {
		OcrParameters param = validParams();
		param.outputFormats = null;
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("No output format"));
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
		param.email = "test@test.com";
		return param;
	}

}
