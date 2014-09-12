package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactoryWithProperties;

public class OcrsdkFactory extends OcrFactoryWithProperties {

	public OcrsdkFactory(Properties userProperties) {
		super(userProperties);
	}

	@Override
	public OcrEngine createEngine() {
		return new OcrsdkEngine();
	}

	@Override
	public OcrProcess createProcess() {
		return new OcrsdkProcess(userProperties);
	}

}
