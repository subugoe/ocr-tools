package de.unigoettingen.sub.ocr.controller;

import java.util.Map;


public class OcrParameters {
	public String inputFolder;
	public String[] inputFormats;
	public String inputTextType;
	public String[] inputLanguages;
	public String outputFolder;
	public String[] outputFormats;
	public String ocrEngine;
	public Map<String, String> options;
}
