package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactoryWithProperties;

public class AbbyyServerFactory extends OcrFactoryWithProperties {

	public AbbyyServerFactory(Properties userProperties) {
		super(userProperties);
	}
	
	@Override
	public OcrEngine createEngine() {
		AbbyyServerOCREngine engine = new AbbyyServerOCREngine(userProperties);
		engine.initialize();
		return engine;
	}

	@Override
	public OcrProcess createProcess() {
		AbbyyOCRProcess process = new AbbyyOCRProcess();
		process.initialize(userProperties);
		return process;
	}

}
