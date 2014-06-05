package de.unigoettingen.sub.ocr.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class BeanProvider {

	public FileManager getFileManager() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("controller-context.xml");
		return ctx.getBean("fileManager", FileManager.class);

	}

}
