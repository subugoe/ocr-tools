package de.uni_goettingen.sub.commons.ocr.api;

public interface OcrFactory {

	public OCREngine createEngine();
	
	public OCRProcess createProcess();
	
	public OCRImage createImage();
	
	public OCROutput createOutput();
	
}
