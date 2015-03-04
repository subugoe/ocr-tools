package de.unigoettingen.sub.commons.ocr.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.OcrParameters;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.Validator;


public class OcrStarterTest {

	private OcrStarter ocrStarter;
	private Mailer mailerMock;
	private LogSelector logSelectorMock;
	private OcrEngineStarter engineStarterMock;
	private Validator validatorMock;
	
	@Before
	public void before() throws Exception {
		engineStarterMock = mock(OcrEngineStarter.class);
		mailerMock = mock(Mailer.class);
		logSelectorMock = mock(LogSelector.class);
		validatorMock = mock(Validator.class);

		ocrStarter = new OcrStarter();
		ocrStarter.setOcrEngineStarter(engineStarterMock);
		ocrStarter.setMailer(mailerMock);
		ocrStarter.setLogSelector(logSelectorMock);
		ocrStarter.setValidator(validatorMock);
		
		when(validatorMock.validateParameters(any(OcrParameters.class))).thenReturn("OK");
	}

	@Test
	public void shouldStartEngineWithParams() throws InterruptedException {
		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		
		ocrStarter.run();
		
		verify(engineStarterMock).startOcrWithParams(param);
	}
	
	@Test
	public void checkingValidParams() {
		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		assertEquals("validation message", "OK", validation);
	}
	
	@Test
	public void shouldKeepMessageFromValidator() {
		when(validatorMock.validateParameters(any(OcrParameters.class))).thenReturn("Unknown Error.");

		OcrParameters param = validParams();
		ocrStarter.setParameters(param);
		String validation = ocrStarter.checkParameters();
		
		assertThat(validation, containsString("Unknown Error."));
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
		
		verify(logSelectorMock).logToFile(startsWith("/tmp/log-"));
	}

	private OcrParameters validParams() {
		OcrParameters param = new OcrParameters();
		param.inputFolder = new File("src/test/resources/testInputs").getAbsolutePath();
		param.ocrEngine = "abbyy";
		param.inputLanguages = new String[]{"de"};
		param.inputTextType = "NORMAL";
		param.outputFormats = new String[]{"PDF"};
		param.outputFolder = "/tmp";
		param.inputFormats = new String[]{"tif"};
		param.props.setProperty("email", "test@test.com");
		return param;
	}

}
