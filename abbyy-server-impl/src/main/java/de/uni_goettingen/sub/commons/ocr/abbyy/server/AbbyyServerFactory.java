package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;

public class AbbyyServerFactory implements OcrFactory {

	@Override
	public OCREngine createEngine() {
		return new AbbyyServerOCREngine();
	}

}
