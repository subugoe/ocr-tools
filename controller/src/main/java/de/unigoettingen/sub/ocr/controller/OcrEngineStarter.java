package de.unigoettingen.sub.ocr.controller;

import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;

public class OcrEngineStarter {

	private FactoryProvider provider = new FactoryProvider();
	
	// for unit tests
	void setFactoryProvider(FactoryProvider newProvider) {
		provider = newProvider;
	}
	
	public void startOcrWithParams(OcrParameters params) {
		OcrFactory factory = provider.createFactory(params.ocrEngine);
	}
}
