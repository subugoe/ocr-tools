package de.unigoettingen.sub.commons.ocr.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;


public class IndexJspTest {
	private static Server jetty;
	private static int jettyPort = 9001;
	private HtmlForm form;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		startJetty();
	}

	@Before
	public void setUp() throws Exception {
		WebClient webClient = new WebClient();
		HtmlPage jsp = webClient.getPage("http://localhost:" + jettyPort + "/index.jsp");
		form = jsp.getFormByName("startOcr");
	}

	@Test
	public void shouldContainAllInputFields() {
		form.getInputByName("inputFolder");
		form.getInputByName("outputFolder");
		form.getSelectByName("imageFormat");
	}
	
	@Test
	public void testJetty() throws Exception {
		HtmlTextInput textField = form.getInputByName("inputFolder");
		textField.setValueAttribute("/home/test");
		HtmlSubmitInput button = form.getInputByName("submit");
		
		TextPage page2 = button.click();
		
		System.out.println(page2.getContent());
	}
	
	public static void startJetty() throws Exception {
		jetty = new Server(jettyPort);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
        context.setWar("src/main/webapp");
        context.setDescriptor("src/test/resources/fake-web.xml");
        jetty.setHandler(context);
		
		jetty.start();
		//Thread.sleep(20000);
	}


}
