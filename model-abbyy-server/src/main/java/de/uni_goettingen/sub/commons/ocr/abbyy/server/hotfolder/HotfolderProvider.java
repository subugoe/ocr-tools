package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HotfolderProvider {

	public Hotfolder createHotfolder(String serverUrl, String username, String password) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("contextAbbyy.xml");
		ServerHotfolder hotfolder = ctx.getBean("hotfolderImplementation", ServerHotfolder.class);
		hotfolder.configureConnection(serverUrl, username, password);
		return hotfolder;
	}
	
}
