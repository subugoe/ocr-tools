package de.unigoettingen.sub.commons.ocr.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

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

public class WebIntegrationTest {

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
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		HtmlPage jsp = webClient.getPage("http://localhost:" + jettyPort + "/index.jsp");
		form = jsp.getFormByName("startOcr");
	}

	@Test
	public void shouldBreakUpIfNoInputs() throws IOException {
		HtmlSubmitInput button = form.getInputByName("submit");
		HtmlPage h = button.click();
		String returnedText = h.getBody().getTextContent();
		
		assertThat(returnedText, containsString("fehlerhaft oder unvollständig"));
	}

	@Test
	public void shouldCompleteByDownloadingTheResult() throws IOException, URISyntaxException {
		when(fileAccessMock.isReadableFolder("/tmp/in")).thenReturn(true);
		when(fileAccessMock.isWritableFolder("/tmp/out")).thenReturn(true);		
		when(fileAccessMock.getPropertiesFromFile(anyString())).thenReturn(validFileProps());
		when(fileAccessMock.getAllFolders(anyString(), any(String[].class))).thenReturn(new File[]{new File("/tmp/in")});
		when(fileAccessMock.getAllImagesFromFolder(any(File.class), any(String[].class))).thenReturn(new File[]{new File("/tmp/in/01.tif")});
		
		when(hotfolderMock.createTmpFile(anyString())).thenReturn(mock(OutputStream.class, withSettings().serializable()));
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in.xml.result.xml"))).thenReturn(true);
		when(hotfolderMock.exists(new URI("http://localhost:9001/output/in.xml"))).thenReturn(true);
		
		form.getInputByName("inputFolder").setValueAttribute("/tmp/in");
		form.getInputByName("outputFolder").setValueAttribute("/tmp/out");
		form.getRadioButtonsByName("textType").get(1).click();
		form.getSelectByName("languages").setSelectedAttribute("en", true);
		form.getSelectByName("outputFormats").setSelectedAttribute("XML", true);
		form.getSelectByName("outputFormats").setSelectedAttribute("PDF", false);
		form.getInputByName("email").setValueAttribute("mail@test.de");
		
		HtmlSubmitInput button = form.getInputByName("submit");
		HtmlPage h = button.click();
		String returnedText = h.getBody().getTextContent();
		
		verify(hotfolderMock, timeout(10000)).download(new URI("http://localhost:9001/output/in.xml"), new File("/tmp/out/in.xml").toURI());
		
		assertThat(returnedText, containsString("OCR-Auftrag wird bearbeitet"));
	}
	
	private Properties validFileProps() {
		Properties fileProps = new Properties();
		fileProps.setProperty("serverUrl", "http://localhost:9001/");
		fileProps.setProperty("outputFolder", "output");
		fileProps.setProperty("resultXmlFolder", "output");
		fileProps.setProperty("maxImagesInSubprocess", "1");
		fileProps.setProperty("maxParallelProcesses", "3");
		fileProps.setProperty("maxServerSpace", "1000000000");
		fileProps.setProperty("minMillisPerFile", "10");
		fileProps.setProperty("maxMillisPerFile", "100");
		fileProps.setProperty("checkInterval", "1");
		return fileProps;
	}


}
