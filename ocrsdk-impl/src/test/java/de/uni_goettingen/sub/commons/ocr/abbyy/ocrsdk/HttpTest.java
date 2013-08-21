package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Before;
import org.junit.Test;

class HelloHandler extends AbstractHandler {
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("application/xml;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.getWriter().println("<test>Hello World</test>");
	}
}

public class HttpTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws Exception {
		Server server = new Server(9101);
		server.setHandler(new HelloHandler());
		server.start();
		

		Thread.sleep(3000);
	}

}
