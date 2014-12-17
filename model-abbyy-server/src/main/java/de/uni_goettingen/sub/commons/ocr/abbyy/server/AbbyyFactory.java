package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Properties;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactoryWithProperties;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class AbbyyFactory extends OcrFactoryWithProperties {

	private BeanProvider beanProvider = new BeanProvider();

	public AbbyyFactory(Properties userProperties) {
		super(userProperties);
	}

	// for unit tests
	void setBeanProvider(BeanProvider newProvider) {
		beanProvider = newProvider;
	}

	@Override
	public OcrEngine createEngine() {
		AbbyyEngine engine = new AbbyyEngine();
		engine.initialize(getCombinedProps());
		return engine;
	}

	@Override
	public OcrProcess createProcess() {
		AbbyyProcess process = new AbbyyProcess();
		process.initialize(getCombinedProps());
		return process;
	}
	
	protected Properties getCombinedProps() {
		String configFile = userProperties.getProperty("abbyy.config", "gbv-antiqua.properties");
		FileAccess fileAccess = beanProvider.getFileAccess();
		Properties fileProps = fileAccess.getPropertiesFromFile(configFile);

		Properties combinedProps = new Properties(fileProps);
		for (String userKey : userProperties.stringPropertyNames()) {
			combinedProps.setProperty(userKey, userProperties.getProperty(userKey));
		}
		return combinedProps;
	}

}
