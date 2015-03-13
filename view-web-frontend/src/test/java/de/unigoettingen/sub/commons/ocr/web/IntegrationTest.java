package de.unigoettingen.sub.commons.ocr.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.FileAccessMockProvider;

public class IntegrationTest {

	private static Server jetty;
	public static int jettyPort = 9002;
	private HtmlForm form;

	private FileAccess fileAccessMock;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		startJetty();
		
	}

	private static void startJetty() throws Exception {
		jetty = new Server(jettyPort);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
        context.setWar("src/main/webapp");
        jetty.setHandler(context);
		
		jetty.start();
	}

	@Before
	public void beforeEachTest() throws Exception {
		fileAccessMock = mock(FileAccess.class);
		FileAccessMockProvider.mock = fileAccessMock;
		
		WebClient webClient = new WebClient();
		HtmlPage jsp = webClient.getPage("http://localhost:" + jettyPort + "/index.jsp");
		form = jsp.getFormByName("startOcr");
	}

	@Test
	public void shouldBreakUpIfNoInputs() throws FailingHttpStatusCodeException, IOException {
		HtmlSubmitInput button = form.getInputByName("submit");
		HtmlPage h = button.click();
		String returnedText = h.getBody().getTextContent();
		
		assertThat(returnedText, containsString("nicht vollständig oder inkorrekt"));
	}


	

}
