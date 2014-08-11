package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;

public class AbbyyMultiuserFactory  extends AbbyyServerFactory {

	public AbbyyMultiuserFactory(Properties userProperties) {
		super(userProperties);
	}
	
	@Override
	public OCREngine createEngine() {
		MultiUserAbbyyOCREngine engine = new MultiUserAbbyyOCREngine(userProperties);
		engine.initialize();
		return engine;
	}

}
