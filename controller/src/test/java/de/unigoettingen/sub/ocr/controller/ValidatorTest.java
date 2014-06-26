package de.unigoettingen.sub.ocr.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileManager;

public class ValidatorTest {

	private BeanProvider beanProviderMock;
	private FileManager fileManagerMock;
	private Validator validatorSut;
	
	@Before
	public void beforeEachTest() {
		beanProviderMock = mock(BeanProvider.class);
		fileManagerMock = mock(FileManager.class);
		when(beanProviderMock.getFileManager()).thenReturn(fileManagerMock);
		validatorSut = new Validator();
		validatorSut.setBeanProvider(beanProviderMock);
	}
	
	@Test
	public void happyPath() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		
		String validation = validatorSut.validateParameters(params);
		
		verify(fileManagerMock).isReadableFolder("/tmp/input");
		assertEquals("OK", validation);
	}

	@Test
	public void shouldNotFindInputFolder() {
		when(fileManagerMock.isReadableFolder("/tmp/input")).thenReturn(false);
		when(fileManagerMock.isWritableFolder("/tmp/output")).thenReturn(true);
		OcrParameters params = validParams();

		String validation = validatorSut.validateParameters(params);

		assertEquals("Input folder not found. ", validation);
	}
	
	@Test
	public void shouldNotFindOutputFolder() {
		when(fileManagerMock.isReadableFolder("/tmp/input")).thenReturn(true);
		when(fileManagerMock.isWritableFolder("/tmp/output")).thenReturn(false);
		OcrParameters params = validParams();

		String validation = validatorSut.validateParameters(params);

		assertEquals("Output folder not found or it is protected. ", validation);
	}
	
	@Test
	public void shouldDetectMissingInputFormats() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		params.inputFormats = new String[]{};
		String validation = validatorSut.validateParameters(params);
		
		assertEquals("No input formats. ", validation);
	}

	@Test
	public void shouldDetectMissingInputTextType() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		params.inputTextType = " ";
		String validation = validatorSut.validateParameters(params);
		
		assertEquals("No input text type. ", validation);
	}
	
	@Test
	public void shouldDetectMissingLanguages() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		params.inputLanguages = null;
		String validation = validatorSut.validateParameters(params);
		
		assertEquals("No input languages. ", validation);
	}
	
	@Test
	public void shouldDetectMissingOutputFormats() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		params.outputFormats = null;
		String validation = validatorSut.validateParameters(params);
		
		assertEquals("No output formats. ", validation);
	}
	
	@Test
	public void shouldDetectMissingPriority() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		params.priority = "";
		String validation = validatorSut.validateParameters(params);
		
		assertEquals("No priority. ", validation);
	}
	
	@Test
	public void shouldDetectMissingOcrEngine() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		params.ocrEngine = null;
		String validation = validatorSut.validateParameters(params);
		
		assertEquals("No OCR engine. ", validation);
	}
	
	@Test
	public void shouldDetectNullOptions() {
		whenAskedThenFindFolders();
		OcrParameters params = validParams();
		params.props = null;
		String validation = validatorSut.validateParameters(params);
		
		assertEquals("Properties may not be null. ", validation);
	}
	
	private void whenAskedThenFindFolders() {
		when(fileManagerMock.isReadableFolder("/tmp/input")).thenReturn(true);
		when(fileManagerMock.isWritableFolder("/tmp/output")).thenReturn(true);
	}

	private OcrParameters validParams() {
		OcrParameters params = new OcrParameters();
		params.inputFolder = "/tmp/input";
		params.outputFolder = "/tmp/output";
		params.inputFormats = new String[]{"tif", "gif"};
		params.inputTextType = "normal";
		params.inputLanguages = new String[]{"de", "en"};
		params.outputFormats = new String[]{"pdf", "xml"};
		params.priority = "0";
		params.ocrEngine = "abbyy";
		params.props = new Properties();
		return params;
	}
	
}
