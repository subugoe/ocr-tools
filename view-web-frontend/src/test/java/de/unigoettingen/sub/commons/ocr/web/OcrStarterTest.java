package de.unigoettingen.sub.commons.ocr.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import de.unigoettingen.sub.ocr.controller.OcrParameters;

@RunWith(MockitoJUnitRunner.class)
public class OcrStarterTest {

	private EngineProvider providerMock;
	private OCREngine engineMock;
	private OCRProcess processMock;
	private OCROutput outputMock;
	private OCRImage imageMock;
	private Mailer mailerMock;
	private OcrStarter ocrStarter;
	private LogSelector logSelectorMock;
	
	@Captor
	private ArgumentCaptor<Map<String,String>> optionsCaptor;
	
	@Before
	public void before() throws Exception {
		providerMock = mock(EngineProvider.class);
		engineMock = mock(OCREngine.class);
		processMock = mock(OCRProcess.class);
		outputMock = mock(OCROutput.class);
		imageMock = mock(OCRImage.class);
		mailerMock = mock(Mailer.class);
		logSelectorMock = mock(LogSelector.class);
		when(providerMock.getFromContext(anyString())).thenReturn(engineMock);
		when(engineMock.newOcrProcess()).thenReturn(processMock);
		when(engineMock.newOcrOutput()).thenReturn(outputMock);
		when(engineMock.newOcrImage(any(URI.class))).thenReturn(imageMock);

		ocrStarter = new OcrStarter();
		ocrStarter.setEngineProvider(providerMock);
		ocrStarter.setMailer(mailerMock);
		ocrStarter.setLogSelector(logSelectorMock);
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
		verify(processMock).addLanguage(any(Locale.class));
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
		param.props.setProperty("user", "newUser");
		ocrStarter.setParameters(param);
		
		ocrStarter.run();
		
		verify(engineMock).setOptions(optionsCaptor.capture());
		String capturedUser = optionsCaptor.getValue().get("user");
		assertEquals("captured user", "newUser", capturedUser);
	}

	@Test
	public void withAnotherPassword() {
		OcrParameters param = validParams();
		param.props.setProperty("password", "newPassword");
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
		assertThat(validation, containsString("Kein Eingabeordner"));
	}

	@Test
	public void checkingParamsInvalidInputFolder() {
		OcrParameters param = validParams();
		param.inputFolder = "not/absolute";
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Eingabeordner muss absoluter Pfad sein"));
	}

	@Test
	public void checkingParamsWithoutOutputFolder() {
		OcrParameters param = validParams();
		param.outputFolder = null;
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Kein Ausgabeordner"));
	}

	@Test
	public void checkingParamsInvalidOutputFolder() {
		OcrParameters param = validParams();
		param.outputFolder = "not/absolute";
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Ausgabeordner muss absoluter Pfad sein"));
	}

	@Test
	public void checkingParamsWithoutEmail() {
		OcrParameters param = validParams();
		param.props.setProperty("email", "");
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Keine Benachrichtigungsadresse"));
	}

	@Test
	public void checkingParamsInvalidEmail() {
		OcrParameters param = validParams();
		param.props.setProperty("email", "invalid");
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Inkorrekte Benachrichtigungsadresse"));
	}

	@Test
	public void checkingParamsWithoutLanguage() {
		OcrParameters param = validParams();
		param.inputLanguages = new String[]{};
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Keine Sprache"));
	}

	@Test
	public void checkingParamsWithoutOutputFormat() {
		OcrParameters param = validParams();
		param.outputFormats = null;
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertThat(validation, containsString("Kein Ausgabeformat"));
	}
	
	@Test
	public void shouldSendEmail() {
		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		ocrStarter.run();
		
		verify(mailerMock).sendFinished(param);
	}

	@Test
	public void shouldLogToFile() {
		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		ocrStarter.run();
		
		verify(logSelectorMock, times(1)).logToFile(startsWith("/tmp/log-"));
	}

	private OcrParameters validParams() {
		OcrParameters param = new OcrParameters();
		param.inputFolder = new File("src/test/resources/testInputs").getAbsolutePath();
		param.ocrEngine = "gbvAntiqua";
		param.inputLanguages = new String[]{"de"};
		param.inputTextType = "NORMAL";
		param.outputFormats = new String[]{"PDF"};
		param.outputFolder = "/tmp";
		param.inputFormats = new String[]{"tif"};
		param.props.setProperty("email", "test@test.com");
		return param;
	}

}
