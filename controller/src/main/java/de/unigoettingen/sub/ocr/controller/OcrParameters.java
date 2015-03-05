package de.unigoettingen.sub.ocr.controller;

import java.util.Properties;


public class OcrParameters {
	public String inputFolder;
	public String inputTextType;
	public String[] inputLanguages;
	public String outputFolder;
	public String[] outputFormats;
	public String[] inputFormats = new String[]{"tif", "jpg", "gif", "tiff", "png", "jpeg"};
	public String priority = "0";
	public String ocrEngine = "abbyy";
	public Properties props = new Properties();
}
