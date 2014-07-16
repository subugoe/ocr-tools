package de.unigoettingen.sub.ocr.controller;

public class ValidatorGerman extends Validator {

	@Override
	protected String inputFolderError() {
		return "Eingabeordner nicht gefunden oder nicht lesbar. ";
	}
	
	@Override
	protected String outputFolderError() {
		return "Ausgabeordner nicht gefunden oder nicht schreibbar. ";
	}

	@Override
	protected String noInputFormats() {
		return "Keine Eingabeformate. ";
	}

	@Override
	protected String noInputTextType() {
		return "Kein Texttyp. ";
	}

	@Override
	protected String noInputLanguages() {
		return "Keine Sprachen. ";
	}

	@Override
	protected String noOutputFormats() {
		return "Keine Ausgabeformate. ";
	}

	@Override
	protected String noPriority() {
		return "Keine Priorität. ";
	}

	@Override
	protected String noOcrEngine() {
		return "Keine OCR-Engine. ";
	}

	@Override
	protected String nullProperties() {
		return "Properties dürfen nicht null sein. ";
	}

}
