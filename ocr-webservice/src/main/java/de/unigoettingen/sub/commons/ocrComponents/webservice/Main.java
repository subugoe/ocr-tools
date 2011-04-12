package de.unigoettingen.sub.commons.ocrComponents.webservice;

import javax.xml.ws.Endpoint;

public class Main {

	public static void main(String[] args) {

		System.out.println("Starting Server");
		ServiceTestImpl implementor = new ServiceTestImpl();
		String address = "http://localhost:9000/helloWorld";
		Endpoint.publish(address, implementor);

	}
}
