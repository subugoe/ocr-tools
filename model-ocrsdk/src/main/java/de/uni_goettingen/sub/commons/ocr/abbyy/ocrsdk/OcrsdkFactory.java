package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactoryWithProperties;

public class OcrsdkFactory extends OcrFactoryWithProperties {

	public OcrsdkFactory(Properties userProperties) {
		super(userProperties);
	}

	@Override
	public OCREngine createEngine() {
		return new OcrsdkEngine();
	}

	@Override
	public OCRProcess createProcess() {
		return new OcrsdkProcess(userProperties);
	}

}
