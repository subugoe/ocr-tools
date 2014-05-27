package de.unigoettingen.sub.commons.ocr.web;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;

public class EngineProvider {

	public OCREngine getFromContext(String engineName) {
		ApplicationContext ac = new ClassPathXmlApplicationContext(engineName + "-context.xml");

		OCREngine engine = (OCREngine) ac.getBean("ocrEngine");
		return engine;
	}
}
