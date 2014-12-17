package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyFactory;
import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;

public class AbbyyMultiuserFactory  extends AbbyyFactory {

	public AbbyyMultiuserFactory(Properties userProperties) {
		super(userProperties);
	}
	
	@Override
	public OcrEngine createEngine() {
		AbbyyMultiuserEngine engine = new AbbyyMultiuserEngine();
		engine.initialize(getCombinedProps());
		return engine;
	}

}
