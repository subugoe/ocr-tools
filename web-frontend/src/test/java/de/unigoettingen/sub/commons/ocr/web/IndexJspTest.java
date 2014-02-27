package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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
		assertThat("No textType radio buttons found", form.getRadioButtonsByName("textType"), is(not(empty())));
		form.getSelectByName("languages");
		form.getSelectByName("outputFormats");
		form.getInputByName("email");
		form.getSelectByName("ocrEngine");
		form.getInputByName("userName");
		form.getInputByName("password");
		form.getInputByName("submit");
	}
	
	@Test
	public void submittingTheFormShouldPostAllParameters() throws Exception {
		form.getInputByName("inputFolder").setValueAttribute("/home/test/in");
		form.getInputByName("outputFolder").setValueAttribute("/home/test/out");
		form.getSelectByName("imageFormat").setSelectedAttribute("jpg", true);
		form.getRadioButtonsByName("textType").get(1).click();
		form.getSelectByName("languages").setSelectedAttribute("en", true);
		form.getSelectByName("outputFormats").setSelectedAttribute("XML", true);
		form.getInputByName("email").setValueAttribute("mail@test.de");
		form.getSelectByName("ocrEngine").setSelectedAttribute("gbvGothic", true);
		form.getInputByName("userName").setValueAttribute("user1");
		form.getInputByName("password").setValueAttribute("passwd");
		
		HtmlSubmitInput button = form.getInputByName("submit");
		TextPage fakeServlet = button.click();
		String textFromFakeServlet = fakeServlet.getContent();
		
		assertThat(textFromFakeServlet, containsString("/home/test/in"));
		assertThat(textFromFakeServlet, containsString("/home/test/out"));
		assertThat(textFromFakeServlet, containsString("jpg"));
		assertThat(textFromFakeServlet, containsString("GOTHIC"));
		assertThat(textFromFakeServlet, containsString("en"));
		assertThat(textFromFakeServlet, containsString("XML"));
		assertThat(textFromFakeServlet, containsString("mail@test.de"));
		assertThat(textFromFakeServlet, containsString("gbvGothic"));
		assertThat(textFromFakeServlet, containsString("user1"));
		assertThat(textFromFakeServlet, containsString("passwd"));
	}
	
	public static void startJetty() throws Exception {
		jetty = new Server(jettyPort);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
        context.setWar("src/main/webapp");
        context.setDescriptor("src/test/resources/fake-web.xml");
        jetty.setHandler(context);
		
		jetty.start();
	}


}
