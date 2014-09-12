package de.uni_goettingen.sub.commons.ocr.api;

public interface OcrFactory {

	public OcrEngine createEngine();
	
	public OcrProcess createProcess();
		
}
