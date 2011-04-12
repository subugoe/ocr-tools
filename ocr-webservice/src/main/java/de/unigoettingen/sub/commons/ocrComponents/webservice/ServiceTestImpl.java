package de.unigoettingen.sub.commons.ocrComponents.webservice;

import javax.jws.WebService;



@WebService(endpointInterface = "de.unigoettingen.sub.commons.ocrComponents.webservice.ServiceTest")
public class ServiceTestImpl implements ServiceTest{
	ByUrlResponseType byUrlResponseType;
	@Override
	public ByUrlResponseType ocrImageFileByUrl(ByUrlRequestType part1) {
		return byUrlResponseType;
	}

}
