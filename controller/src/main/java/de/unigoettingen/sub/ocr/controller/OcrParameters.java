package de.unigoettingen.sub.ocr.controller;

import java.util.Properties;


public class OcrParameters {
	public String inputFolder;
	public String[] inputFormats;
	public String inputTextType;
	public String[] inputLanguages;
	public String outputFolder;
	public String[] outputFormats;
	public String priority;
	public String ocrEngine;
	public Properties props = new Properties();
}
