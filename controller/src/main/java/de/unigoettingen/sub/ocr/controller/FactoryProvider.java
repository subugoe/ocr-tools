package de.unigoettingen.sub.ocr.controller;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk.OcrsdkFactory;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyFactory;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser.AbbyyMultiuserFactory;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.uni_goettingen.sub.commons.ocr.tesseract.TesseractFactory;

public class FactoryProvider {

	public OcrFactory createFactory(String factoryId, Properties userProps) {
		if ("abbyy".equals(factoryId)) {
			return new AbbyyFactory(userProps);
		} else if ("abbyy-multiuser".equals(factoryId)) {
			return new AbbyyMultiuserFactory(userProps);
		} else if ("tesseract".equals(factoryId)) {
			return new TesseractFactory();
		} else if ("ocrsdk".equals(factoryId)) {
			return new OcrsdkFactory(userProps);
		} else {
			throw new IllegalArgumentException("Unknown argument: " + factoryId);
		}
	}
}
