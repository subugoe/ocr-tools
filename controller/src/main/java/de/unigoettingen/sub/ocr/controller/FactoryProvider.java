package de.unigoettingen.sub.ocr.controller;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerFactory;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;

public class FactoryProvider {

	public OcrFactory createFactory(String id) {
		if ("abbyy".equals(id)) {
			return new AbbyyServerFactory();
		} else {
			throw new IllegalArgumentException("Unknown argument: " + id);
		}
	}
}
