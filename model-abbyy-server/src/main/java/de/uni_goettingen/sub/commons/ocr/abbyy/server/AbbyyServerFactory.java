package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactoryWithProperties;

public class AbbyyServerFactory extends OcrFactoryWithProperties {

	public AbbyyServerFactory(Properties userProperties) {
		super(userProperties);
	}
	
	@Override
	public OCREngine createEngine() {
		AbbyyServerOCREngine engine = new AbbyyServerOCREngine(userProperties);
		engine.initialize();
		return engine;
	}

	@Override
	public OCRProcess createProcess() {
		AbbyyOCRProcess process = new AbbyyOCRProcess();
		process.initialize(userProperties);
		return process;
	}

	@Override
	public OCROutput createOutput() {
		return new AbbyyOCROutput();
	}

}
