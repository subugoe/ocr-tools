package de.uni_goettingen.sub.commons.ocr.tesseract;


import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;

public class TesseractFactory implements OcrFactory {

	@Override
	public OCREngine createEngine() {
		return new TesseractOCREngine();
	}

	@Override
	public OCRProcess createProcess() {
		return new TesseractOCRProcess();
	}

	@Override
	public OCRImage createImage() {
		return new AbstractOCRImage() {
		};
	}

	@Override
	public OCROutput createOutput() {
		return new AbstractOCROutput() {
		};
	}

}
