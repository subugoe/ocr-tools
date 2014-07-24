package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
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

	@Override
	public OCRImage createImage() {
		return new OcrsdkImage();
	}

	@Override
	public OCROutput createOutput() {
		return new OcrsdkOutput();
	}

}