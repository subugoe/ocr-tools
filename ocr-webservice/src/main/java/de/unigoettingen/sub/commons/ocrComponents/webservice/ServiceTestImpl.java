package de.unigoettingen.sub.commons.ocrComponents.webservice;

import javax.jws.WebService;

@WebService(endpointInterface = "de.unigoettingen.sub.commons.ocrComponents.webservice.ServiceTest")
public class ServiceTestImpl implements ServiceTest {
	ByUrlResponseType byUrlResponseType = new ByUrlResponseType();

	@Override
	public ByUrlResponseType ocrImageFileByUrl(ByUrlRequestType part1) {
		
		
		byUrlResponseType.setMessage("Hallo Message");
		byUrlResponseType.setOutputUrl("Output Url: http://webservice.xml");
		byUrlResponseType.setProcessingLog("ProcessingLog");
		byUrlResponseType.setProcessingUnit("ProcessingLog");
		byUrlResponseType.setReturncode(10);
		byUrlResponseType.setSuccess(true);
		byUrlResponseType.setToolProcessingTime(20042011);
		return byUrlResponseType;
	}

}
