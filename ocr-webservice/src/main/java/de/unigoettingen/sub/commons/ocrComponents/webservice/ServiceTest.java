package de.unigoettingen.sub.commons.ocrComponents.webservice;


import javax.jws.WebResult;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

//import org.apache.cxf.annotations.WSDLDocumentation;

@WebService(targetNamespace = "http://webservice.ocrComponents.commons.sub.unigoettingen.de/", name = "IMPACTServicePortType")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface ServiceTest {
	
    @WebResult(name = "ByUrlResponse", targetNamespace = "http://webservice.ocrComponents.commons.sub.unigoettingen.de/", partName = "part1")
	@WebMethod(action = "ocrImageFileByUrl")
	public ByUrlResponseType ocrImageFileByUrl(
			@WebParam(partName = "part1",name = "ByUrlRequest")
		    ByUrlRequestType part1
	);
		

}
