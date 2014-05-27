package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

class FakeHandler extends AbstractHandler {
	
	private int returnCode;

	public FakeHandler(int returnCode) {
		this.returnCode = returnCode;
	}
	
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setStatus(returnCode);
		baseRequest.setHandled(true);
		response.setContentType("application/xml;charset=utf-8");

		String basicAuth = request.getHeader("Authorization");
		if (basicAuth != null) {
			basicAuth = basicAuth.substring("Basic: ".length());
			basicAuth = new String(Base64.decodeBase64(basicAuth));
		} else {
			basicAuth = "none";
		}
		
		String contentLength = request.getHeader("Content-Length");
		
		InputStream postContent = request.getInputStream();
		byte[] contentBytes = IOUtils.toByteArray(postContent);
		String content = "";
		for (byte b : contentBytes) {
			content += b;
		}
		
		PrintWriter out = response.getWriter(); 
		out.println("<response>");
		out.println("<httpMethod>" + request.getMethod() + "</httpMethod>");
		out.println("<basicAuth>" + basicAuth + "</basicAuth>");
		out.println("<contentLength>" + contentLength + "</contentLength>");
		out.println("<content>" + content + "</content>");
		out.println("</response>");
	}
}

public class HttpTest {

	private static Server server200;
	private static Server server401;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		server200 = new Server(10200);
		server200.setHandler(new FakeHandler(200));
		server200.start();
		server401 = new Server(10401);
		server401.setHandler(new FakeHandler(401));
		server401.start();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		server200.stop();
		server401.stop();
	}

	@Test
	public void sendingPost() throws Exception {
		Http http = new Http("", "");
		String response = http.submitPost("http://localhost:10200/", new byte[]{});

		assertThat(response, containsString("<httpMethod>POST</httpMethod>"));
	}

	@Test
	public void serverMustReceiveCredentials() throws Exception {
		Http http = new Http("user", "pw");
		String response = http.submitPost("http://localhost:10200/", new byte[]{});

		assertThat(response, containsString("<basicAuth>user:pw</basicAuth>"));
	}

	@Test
	public void serverMustReceivePostData() throws Exception {
		Http http = new Http("", "");
		byte[] bytesToSend = {4, 2};
		String response = http.submitPost("http://localhost:10200/", bytesToSend);

		assertThat(response, containsString("<contentLength>2</contentLength>"));
		assertThat(response, containsString("<content>42</content>"));
	}

	@Test
	public void sendingGet() throws Exception {
		Http http = new Http("", "");
		String response = http.submitGet("http://localhost:10200/");

		assertThat(response, containsString("<httpMethod>GET</httpMethod>"));
	}

	@Test(expected=RuntimeException.class)
	public void sendingPostToUnauthorized() throws Exception {
		Http http = new Http("", "");
		http.submitPost("http://localhost:10401/", new byte[]{});
	}

	@Test(expected=RuntimeException.class)
	public void sendingGetToUnauthorized() throws Exception {
		Http http = new Http("", "");
		http.submitGet("http://localhost:10401/");
	}

	@Test(expected=RuntimeException.class)
	public void sendingPostToUnreachable() throws Exception {
		Http http = new Http("", "");
		http.submitPost("http://localhost:10000/", new byte[]{});
	}

	@Test(expected=RuntimeException.class)
	public void sendingGetToUnreachable() throws Exception {
		Http http = new Http("", "");
		http.submitGet("http://localhost:10000/");
	}

	@Test
	public void sendingGetWithoutAuthentication() throws Exception {
		Http http = new Http("user", "pw");
		InputStream is = http.submitGetWithoutAuthentication("http://localhost:10200/");
		String response = IOUtils.toString(is);

		assertThat(response, containsString("<httpMethod>GET</httpMethod>"));
		assertThat(response, containsString("<basicAuth>none</basicAuth>"));
	}


}
