package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class OcrServletTestPart {

	private static int jettyPort = TestSuiteForJspsAndServlets.jettyPort;
	private static Server jetty;
	
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

	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = new WebClient();
		webClient.getPage("http://localhost:" + jettyPort + "/ocr-servlet-test");
	}

}
