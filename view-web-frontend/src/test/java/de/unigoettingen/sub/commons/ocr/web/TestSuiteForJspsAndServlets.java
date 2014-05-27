package de.unigoettingen.sub.commons.ocr.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.unigoettingen.sub.commons.ocr.web.testutil.IndexJspTestPart;
import de.unigoettingen.sub.commons.ocr.web.testutil.OcrServletTestPart;

@RunWith(Suite.class)
@SuiteClasses({ IndexJspTestPart.class, OcrServletTestPart.class })
public class TestSuiteForJspsAndServlets {

	private static Server jetty;
	public static int jettyPort = 9001;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		startJetty();
		
	}

	
	private static void startJetty() throws Exception {
		jetty = new Server(jettyPort);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
        context.setWar("src/main/webapp");
        context.setDescriptor("src/test/resources/fake-web.xml");
        jetty.setHandler(context);
		
		jetty.start();
	}

	
}
