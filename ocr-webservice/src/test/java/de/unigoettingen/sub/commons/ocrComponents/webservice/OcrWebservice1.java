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


import javax.xml.ws.Endpoint;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.SoapUIException;
import static org.junit.Assert.assertTrue;

import java.io.IOException;


public class OcrWebservice1
{
	final static Logger logger = LoggerFactory.getLogger(OcrWebservice1.class);
	OcrServiceImpl serviceTest;

	WsdlProject project;
	
	@Before
	public void before() throws XmlException, SoapUIException {
		serviceTest = new OcrServiceImpl();
	}
	
	@Ignore
	@Test 
	public synchronized void two() throws InterruptedException, XmlException, IOException, SoapUIException, SubmitException {

		System.out.println("Starting Server");
		String address = "http://localhost:9000/helloWorld";

		Endpoint endpoint = Endpoint.publish(address, serviceTest);

		project = new WsdlProject();
		WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project,
				"http://localhost:9000/helloWorld?wsdl", true)[0];
		WsdlOperation operation = (WsdlOperation) iface
				.getOperationByName("ocrImageFileByUrl");
		String message = operation.createRequest(true);
		assertTrue(message
				.contains("<ocrPriorityType>HIGH</ocrPriorityType>"));
		assertTrue(message.contains("<textType>NORMAL</textType>"));
		assertTrue(message.contains("<outputFormat>TXT</outputFormat>"));
		 System.out.println( message );
		// create a new empty request for that operation
		WsdlRequest request = operation.addNewRequest("My request");
		// generate the request content from the schema
		request.setRequestContent(operation.createRequest(true));
		// submit the request
		WsdlSubmit submit = (WsdlSubmit) request.submit(new WsdlSubmitContext(
				request), false);

		// wait for the response
		Response response = submit.getResponse();

		// print the response
		String content = response.getContentAsString();
		logger.debug(content);
		logger.trace(content);
	//	System.out.println( content );
	//	JOptionPane.showMessageDialog(null, "Server beenden");
		endpoint.stop();
		
	}
}