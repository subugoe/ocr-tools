package de.unigoettingen.sub.commons.ocr.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class BeanProvider {

	public FileManager getFileManager() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("util-context.xml");
		return ctx.getBean("fileManager", FileManager.class);

	}

}
