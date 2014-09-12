package de.uni_goettingen.sub.commons.ocr.tesseract;


import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;

public class TesseractFactory implements OcrFactory {

	@Override
	public OcrEngine createEngine() {
		return new TesseractOCREngine();
	}

	@Override
	public OcrProcess createProcess() {
		return new TesseractOCRProcess();
	}

}
