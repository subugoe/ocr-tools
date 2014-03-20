package de.unigoettingen.sub.commons.ocr.web;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngineFactory;

public class EngineProvider {

	public OCREngine getFromContext(String engineName) {
		ApplicationContext ac = new ClassPathXmlApplicationContext(engineName + "-context.xml");
		OCREngineFactory ocrEngineFactory = (OCREngineFactory) ac
					.getBean("OCREngineFactory");

		OCREngine engine = ocrEngineFactory.newOcrEngine();
		return engine;
	}
}
