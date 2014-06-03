package de.unigoettingen.sub.ocr.controller;

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
		if (!isReadableFolder(params.inputFolder)) {
			validationMessage += "Input folder not found.";
		}
		
		if (validationMessage.isEmpty()) {
			return "OK";
		} else {
			return validationMessage;
		}
	}

	private boolean isReadableFolder(String inputFolder) {
		return manager.isReadableFolder(inputFolder);
	}
}
