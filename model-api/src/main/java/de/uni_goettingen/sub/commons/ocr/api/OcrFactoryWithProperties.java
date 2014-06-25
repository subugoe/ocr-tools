package de.uni_goettingen.sub.commons.ocr.api;

import java.util.Properties;

public abstract class OcrFactoryWithProperties implements OcrFactory {

	protected Properties userProperties;

	public OcrFactoryWithProperties(Properties initProperties) {
		userProperties = initProperties;
	}
	
}
