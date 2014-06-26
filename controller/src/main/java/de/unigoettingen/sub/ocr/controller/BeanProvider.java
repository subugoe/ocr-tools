package de.unigoettingen.sub.ocr.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.unigoettingen.sub.commons.ocr.util.FileManager;


public class BeanProvider {

	public FileManager getFileManager() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("util-context.xml");
		return ctx.getBean("fileManager", FileManager.class);

	}

}
