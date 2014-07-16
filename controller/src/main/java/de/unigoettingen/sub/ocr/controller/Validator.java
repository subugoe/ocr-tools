package de.unigoettingen.sub.ocr.controller;

import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileManager;

public class Validator {

	private BeanProvider beanProvider = new BeanProvider();
	private FileManager manager;

	// for unit tests
	void setBeanProvider(BeanProvider newProvider) {
		beanProvider = newProvider;
	}

	public String validateParameters(OcrParameters params) {
		manager = beanProvider.getFileManager();
		String validationMessage = "";
		if (!isReadable(params.inputFolder)) {
			validationMessage += inputFolderError();
		}
		if (isEmpty(params.inputTextType)) {
			validationMessage += noInputTextType();
		}
		if (isEmpty(params.inputLanguages)) {
			validationMessage += noInputLanguages();
		}
		if (!isWritable(params.outputFolder)) {
			validationMessage += outputFolderError();
		}
		if (isEmpty(params.outputFormats)) {
			validationMessage += noOutputFormats();
		}
		if (isEmpty(params.inputFormats)) {
			validationMessage += noInputFormats();
		}
		if (isEmpty(params.priority)) {
			validationMessage += noPriority();
		}
		if (isEmpty(params.ocrEngine)) {
			validationMessage += noOcrEngine();
		}
		if (params.props == null) {
			validationMessage += nullProperties();
		}
		
		if (validationMessage.isEmpty()) {
			return "OK";
		} else {
			return validationMessage;
		}
	}

	private boolean isReadable(String inputFolder) {
		return manager.isReadableFolder(inputFolder);
	}

	private boolean isWritable(String outputFolder) {
		return manager.isWritableFolder(outputFolder);
	}

	private boolean isEmpty(String[] array) {
		return array == null || array.length == 0;
	}

	private boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}

	protected String inputFolderError() {
		return "Input folder not found or it is not readable. ";
	}

	protected String outputFolderError() {
		return "Output folder not found or it is not writable. ";
	}

	protected String noInputFormats() {
		return "No input formats. ";
	}

	protected String noInputTextType() {
		return "No input text type. ";
	}

	protected String noInputLanguages() {
		return "No input languages. ";
	}

	protected String noOutputFormats() {
		return "No output formats. ";
	}

	protected String noPriority() {
		return "No priority. ";
	}

	protected String noOcrEngine() {
		return "No OCR engine. ";
	}

	protected String nullProperties() {
		return "Properties may not be null. ";
	}


}
