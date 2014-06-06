package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;

public class AbbyyServerFactory implements OcrFactory {

	@Override
	public OCREngine createEngine() {
		return new AbbyyServerOCREngine();
	}

	@Override
	public OCRProcess createProcess() {
		return new AbbyyOCRProcess(new ConfigParser().parse());
	}

	@Override
	public OCRImage createImage() {
		return new AbbyyOCRImage();
	}

	@Override
	public OCROutput createOutput() {
		return new AbbyyOCROutput();
	}

}
