package de.unigoettingen.sub.commons.ocr.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderMockProvider;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.ServerHotfolder;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.FileAccessMockProvider;
import de.unigoettingen.sub.commons.ocr.util.Mailer;
import de.unigoettingen.sub.commons.ocr.util.MailerMockProvider;

public class IntegrationTest {

	private static Server jetty;
	public static int jettyPort = 9002;
	private HtmlForm form;

	private FileAccess fileAccessMock = mock(FileAccess.class);;
	private ServerHotfolder hotfolderMock = mock(ServerHotfolder.class);
	private Mailer mailerMock = mock(Mailer.class);

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
		FileAccessMockProvider.mock = fileAccessMock;
		HotfolderMockProvider.mock = hotfolderMock;
		MailerMockProvider.mock = mailerMock;
		
		WebClient webClient = new WebClient();
		HtmlPage jsp = webClient.getPage("http://localhost:" + jettyPort + "/index.jsp");
		form = jsp.getFormByName("startOcr");
	}

	@Test
	public void shouldBreakUpIfNoInputs() throws IOException {
		HtmlSubmitInput button = form.getInputByName("submit");
		HtmlPage h = button.click();
		String returnedText = h.getBody().getTextContent();
		
		assertThat(returnedText, containsString("nicht vollständig oder inkorrekt"));
	}

	@Test
	public void should() throws IOException {
		when(fileAccessMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileAccessMock.isWritableFolder("/tmp/out")).thenReturn(true);		
		
		form.getInputByName("inputFolder").setValueAttribute("/tmp/in");
		form.getInputByName("outputFolder").setValueAttribute("/tmp/out");
		form.getRadioButtonsByName("textType").get(1).click();
		form.getSelectByName("languages").setSelectedAttribute("en", true);
		form.getSelectByName("outputFormats").setSelectedAttribute("XML", true);
		form.getInputByName("email").setValueAttribute("mail@test.de");
		
		HtmlSubmitInput button = form.getInputByName("submit");
		HtmlPage h = button.click();
		String returnedText = h.getBody().getTextContent();
		
		verify(fileAccessMock, timeout(5000)).getAllFolders(anyString(), any(String[].class));
		
		assertThat(returnedText, containsString("OCR-Auftrag wird bearbeitet"));
	}
	

}
