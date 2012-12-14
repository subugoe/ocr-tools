package de.unigoettingen.sub.commons.ocrComponents.webservice;

/*

© 2010, SUB Göttingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.ws.Endpoint;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Response;


public class WebServiceTest
{
	private final static Logger logger = LoggerFactory.getLogger(WebServiceTest.class);
	private  static Endpoint endpoint;;

	private final static File TARGET = new File(
			System.getProperty("user.dir") + "/target");
	private final static File WEB_SERVICE_OUTPUT = new File(TARGET, "wsOutput");

	private static Server webServer;
	private static final String SERVICE_URL = "http://localhost:9004/testService";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WEB_SERVICE_OUTPUT.mkdirs();
		// In the real environment, Tomcat will set this property to its root dir
		System.setProperty("ocrWebservice.root", WEB_SERVICE_OUTPUT.getAbsolutePath());
		
		// http server with the input images
		startWebServer(9003);

		System.out.println("Starting Web Service Container on " + SERVICE_URL);
		OcrServiceImpl service = new OcrServiceImpl();
		endpoint = Endpoint.publish(SERVICE_URL, service);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		webServer.stop();
		endpoint.stop();
	}

	@Test 
	public void usingDefaultsFromWsdl() throws Exception {
		WsdlProject project = new WsdlProject();
		WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project,
				SERVICE_URL + "?wsdl", true)[0];
		WsdlOperation operation = (WsdlOperation) iface
				.getOperationByName("ocrImageFileByUrl");
		
		// create a new empty request for that operation
		WsdlRequest request = operation.addNewRequest("My request");
		// generate the request content from the schema
		String message = operation.createRequest(true);
		request.setRequestContent(message);
		// submit the request
		WsdlSubmit submit = (WsdlSubmit) request.submit(new WsdlSubmitContext(
				request), false);

		// wait for the response
		Response response = submit.getResponse();

		// print the response
		String content = response.getContentAsString();
		//System.out.println(content);
		
		assertTrue(content.contains("<success>true</success>"));
		
		Pattern p = Pattern.compile("<outputUrl>.+/temp/(.+)</outputUrl>");
		Matcher m = p.matcher(content);
		m.find();
		String result = m.group(1);
		File resultFile = new File(WEB_SERVICE_OUTPUT + "/temp/" + result);
		assertTrue(resultFile.exists());
		
	}
	
	public static void startWebServer(int port) throws Exception {
		webServer = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		webServer.addConnector(connector);

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		//resource_handler.setWelcomeFiles(new String[] { "index.html" });

		String serverRoot = WebServiceTest.class.getResource("/input/").getFile();

		resource_handler.setResourceBase(serverRoot);

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler,
				new DefaultHandler() });
		webServer.setHandler(handlers);

		webServer.start();
	}

}