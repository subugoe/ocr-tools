package de.unigoettingen.sub.commons.ocr.web.testutil;

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

import de.unigoettingen.sub.commons.ocr.web.TestSuiteForJspsAndServlets;

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

	@Test
	public void twoSubsequentOkRequests() throws Exception {
		params.add(new NameValuePair("fakeValidationMessage", "OK"));
		request.setRequestParameters(params);
		TextPage page = webClient.getPage(request);
		assertEquals("Forwarded to view: ocr-started.jsp", page.getContent());
		TextPage page2 = webClient.getPage(request);
		assertEquals("Forwarded to view: ocr-started.jsp", page2.getContent());
	}

	// TODO: refactor the test
	@Test
	public void twoParallelOkRequests() throws Exception {
		new Thread(
		new Runnable() {
			private WebClient webClient1;
			private WebRequest request1;
			private List<NameValuePair> params1;
			public void run() {
				try {
				webClient1 = new WebClient();
				request1 = new WebRequest(new URL("http://localhost:" + jettyPort + "/ocr-servlet-child"), HttpMethod.POST);
				params1 = new ArrayList<NameValuePair>();
				params1.add(new NameValuePair("fakeValidationMessage", "OK"));
				request1.setRequestParameters(params1);
				webClient1.getPage(request1);
				} catch (Exception e) {
					System.out.println("my exxxxxxxxxx: " + e);
				}
			}
		}).start();
				
		Thread.sleep(2);
		new Thread(
		new Runnable() {
			private WebClient webClient2;
			private WebRequest request2;
			private List<NameValuePair> params2;
			public void run() {
				try {
				webClient2 = new WebClient();
				request2 = new WebRequest(new URL("http://localhost:" + jettyPort + "/ocr-servlet-child"), HttpMethod.POST);
				params2 = new ArrayList<NameValuePair>();
				params2.add(new NameValuePair("fakeValidationMessage", "OK"));
				request2.setRequestParameters(params2);
				webClient2.getPage(request2);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}).start();
		
		// TODO: join() ?
		Thread.sleep(100);
	}

}
