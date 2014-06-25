package de.unigoettingen.sub.ocr.controller;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerFactory;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser.AbbyyMultiuserFactory;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;

public class FactoryProvider {

	public OcrFactory createFactory(String factoryId, Properties userProps) {
		if ("abbyy".equals(factoryId)) {
			return new AbbyyServerFactory(userProps);
		} else if ("abbyy-multiuser".equals(factoryId)) {
			return new AbbyyMultiuserFactory(userProps);
		} else {
			throw new IllegalArgumentException("Unknown argument: " + factoryId);
		}
	}
}
