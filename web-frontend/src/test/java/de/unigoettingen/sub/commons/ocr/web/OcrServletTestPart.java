package de.unigoettingen.sub.commons.ocr.web;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class OcrServletTestPart {

	private static int jettyPort = TestSuiteForJspsAndServlets.jettyPort;
	private WebClient webClient;
	private WebRequest request;
	private List<NameValuePair> params;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
	
	@Before
	public void setUp() throws Exception {
		webClient = new WebClient();
		request = new WebRequest(new URL("http://localhost:" + jettyPort + "/ocr-servlet-child"), HttpMethod.POST);
		params = new ArrayList<NameValuePair>();
	}

	@Test
	public void servletShouldStartOcr() throws Exception {
		params.add(new NameValuePair("fakeValidationMessage", "OK"));
		request.setRequestParameters(params);
		TextPage page = webClient.getPage(request);
		assertEquals("Forwarded to view: ocr-started.jsp", page.getContent());
	}

	@Test
	public void servletShouldDenyOcr() throws Exception {
		params.add(new NameValuePair("fakeValidationMessage", "Error"));
		request.setRequestParameters(params);
		TextPage page = webClient.getPage(request);
		assertEquals("Forwarded to view: invalid-parameters.jsp", page.getContent());
	}

}
