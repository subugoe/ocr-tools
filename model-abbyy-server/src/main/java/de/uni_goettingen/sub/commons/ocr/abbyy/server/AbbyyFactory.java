package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactoryWithProperties;

public class AbbyyFactory extends OcrFactoryWithProperties {

	public AbbyyFactory(Properties userProperties) {
		super(userProperties);
	}
	
	@Override
	public OcrEngine createEngine() {
		AbbyyEngine engine = new AbbyyEngine(userProperties);
		engine.initialize();
		return engine;
	}

	@Override
	public OcrProcess createProcess() {
		AbbyyProcess process = new AbbyyProcess();
		process.initialize(userProperties);
		return process;
	}

}
