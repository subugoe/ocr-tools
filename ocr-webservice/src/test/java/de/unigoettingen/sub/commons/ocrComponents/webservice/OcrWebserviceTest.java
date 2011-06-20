package de.unigoettingen.sub.commons.ocrComponents.webservice;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.xml.ws.Endpoint;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

public class OcrWebserviceTest {
	ServiceTestImpl serviceTest;

	WsdlProject project;

	@Before
	public void before() throws XmlException, IOException, SoapUIException {
		// serviceTest = mock(ServiceTest.class);
		serviceTest = new ServiceTestImpl();
		// byUrlRequestType = new ByUrlRequestType();

	}

	@Ignore
	@Test
	public void test() throws InterruptedException, XmlException, IOException,
			SoapUIException, SubmitException {
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
				.contains("<ocrPriorityType>NORMAL</ocrPriorityType>"));
		assertTrue(message.contains("<textType>GOTHIC</textType>"));
		assertTrue(message.contains("<outputFormat>TXT</outputFormat>"));
		// System.out.println( message );
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
		assertTrue(content.contains("<success>true</success>"));
		assertTrue(content.contains("<returncode>10</returncode>"));
		assertTrue(content.contains("<message>Hallo Message</message>"));
		assertTrue(content
				.contains("<toolProcessingTime>20042011</toolProcessingTime>"));
		assertTrue(content
				.contains("<processingLog>ProcessingLog</processingLog>"));
		assertTrue(content
				.contains("<processingUnit>ProcessingLog</processingUnit>"));
		assertTrue(content
				.contains("<outputUrl>Output Url: http://webservice.xml</outputUrl>"));
		// System.out.println( content );

		JOptionPane.showMessageDialog(null, "Server beenden");
		endpoint.stop();

	}

}
