package de.unigoettingen.sub.commons.ocr.servlet;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
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
		InputStream changedWebXml = replacePaths(webXml);
		runner = new ServletRunner(changedWebXml, "/SimpleOCR");
	}

	@Before
	public void setUp() throws Exception {
		client = runner.newClient();
		request = new GetMethodWebRequest(
				"http://localhost:8080/SimpleOCR/");
	}

	@Test
	public void oneImage() throws SAXException, IOException {
		request.setParameter("path", "images");
		request.setParameter("imgrange", "1");
		WebResponse response = client.getResponse(request);
		
		assertEquals(200, response.getResponseCode());
		
		String htmlString = IOUtils.toString(response.getInputStream());
		assertTrue(htmlString.contains("<h1>Ergebnis:</h1>"));	
	}
	
	@Test
	public void threeImages() throws SAXException, IOException {
		request.setParameter("path", "images");
		request.setParameter("imgrange", "1-3");
		WebResponse response = client.getResponse(request);
		
		assertEquals(200, response.getResponseCode());
		
		String htmlString = IOUtils.toString(response.getInputStream());
		assertTrue(htmlString.contains("<h1>Ergebnis:</h1>"));	
	}
	
	
	
	private static InputStream replacePaths(File webXml) throws IOException {
		StringBuilder sb = new StringBuilder(FileUtils.readFileToString(webXml));
		
		String dirPrefix = new File("./src/test/resources/input").getCanonicalPath() + "/";
		int from = sb.indexOf("#DIR_PREFIX_TO_REPLACE#");
		int to = from + "#DIR_PREFIX_TO_REPLACE#".length();
		sb.replace(from, to, dirPrefix);
		
		String cacheDir = new File("./target").getCanonicalPath() + "/";
		from = sb.indexOf("#CACHE_DIR_TO_REPLACE#");
		to = from + "#CACHE_DIR_TO_REPLACE#".length();
		sb.replace(from, to, cacheDir);
		
		return IOUtils.toInputStream(sb.toString());
	}

}
