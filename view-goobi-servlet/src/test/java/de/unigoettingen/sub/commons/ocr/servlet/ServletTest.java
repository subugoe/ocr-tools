package de.unigoettingen.sub.commons.ocr.servlet;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ServletTest {

	private static ServletRunner runner;
	
	private ServletUnitClient client;
	private WebRequest request;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File webXml = new File("./src/test/resources/web.xml");
		runner = new ServletRunner(webXml, "/SimpleOCR");
	}

	@Before
	public void setUp() throws Exception {
		client = runner.newClient();
		request = new GetMethodWebRequest(
				"http://localhost/SimpleOCR/");
	}

	@Test
	public void oneImage() throws SAXException, IOException {
		request.setParameter("path", "images");
		request.setParameter("imgrange", "1");
		WebResponse response = client.getResponse(request);
		
		assertEquals(200, response.getResponseCode());
		
		String htmlString = IOUtils.toString(response.getInputStream());
		assertThat(htmlString, containsString("<h1>Ergebnis:</h1>"));	
	}
	
	@Test
	public void threeImages() throws SAXException, IOException {
		request.setParameter("path", "images");
		request.setParameter("imgrange", "1-3");
		WebResponse response = client.getResponse(request);
		
		assertEquals(200, response.getResponseCode());
		
		String htmlString = IOUtils.toString(response.getInputStream());
		assertThat(htmlString, containsString("<h1>Ergebnis:</h1>"));	
	}
	
	@Test(expected=Exception.class)
	public void shouldComplainAboutImagesRange() throws SAXException, IOException {
		request.setParameter("path", "images");
		client.getResponse(request);
	}
	
	@Test(expected=Exception.class)
	public void shouldComplainAboutPath() throws SAXException, IOException {
		request.setParameter("imgrange", "1-3");
		client.getResponse(request);
	}
	
	@Test(expected=Exception.class)
	public void shouldComplainAboutIncorrectRange() throws SAXException, IOException {
		request.setParameter("path", "images");
		request.setParameter("imgrange", "2-1");
		client.getResponse(request);
	}
	
}
