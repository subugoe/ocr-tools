package de.unigoettingen.sub.commons.ocr.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class BeanProvider {

	public FileAccess getFileAccess() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("util-context.xml");
		return ctx.getBean("fileAccess", FileAccess.class);
	}

	public Mailer getMailer() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("util-context.xml");
		return ctx.getBean("mailer", Mailer.class);
	}
}
