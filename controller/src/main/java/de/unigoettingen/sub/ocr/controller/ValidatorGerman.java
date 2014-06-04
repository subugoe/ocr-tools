package de.unigoettingen.sub.ocr.controller;

public class ValidatorGerman extends Validator {

	@Override
	protected String inputFolderNotFound() {
		return "Eingabeordner nicht gefunden. ";
	}
}
