package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerOCREngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.OCRExecuter;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public class MultiUserAbbyyOCREngine extends AbbyyServerOCREngine {
	final static Logger logger = LoggerFactory
			.getLogger(MultiUserAbbyyOCREngine.class);

	private static MultiUserAbbyyOCREngine _instance;
	
	private MultiUserAbbyyOCREngine() throws ConfigurationException {
		super();
	}

	public static MultiUserAbbyyOCREngine getInstance() {

		if (_instance == null) {
			try {
				_instance = new MultiUserAbbyyOCREngine();
			} catch (ConfigurationException e) {
				logger.error("Can't read configuration", e);
				throw new OCRException(e);
			}
		}
		return _instance;
	}

	@Override
	protected OCRExecuter createPool() {
		return new HazelcastOCRExecutor(maxThreads, hotfolder, config);
	}

	@Override
	protected void cleanUp() {
		Hazelcast.getLifecycleService().shutdown();
	}
	
}
