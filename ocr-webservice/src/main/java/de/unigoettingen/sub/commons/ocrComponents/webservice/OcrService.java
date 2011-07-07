package de.unigoettingen.sub.commons.ocrComponents.webservice;

import javax.jws.WebResult;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.annotations.WSDLDocumentation;
import org.apache.cxf.annotations.WSDLDocumentationCollection;

@WSDLDocumentationCollection({
		@WSDLDocumentation("Apply text recognition to image file (sending/receiving data by URL)"),
		@WSDLDocumentation(value = "IMPACT Abbyy Fine Reader 2 Service. This service provides the basic functionality of the Abbyy Fine Reader 2 text recogntion engine for applying OCR to an image file.", placement = WSDLDocumentation.Placement.TOP) })
@WebService(targetNamespace = "http://webservice.ocrComponents.commons.sub.unigoettingen.de/", name = "IMPACTServicePortType")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface OcrService {
	@WSDLDocumentation("inputTextType: Normal/Typewriter/Gothic/ToBeDetected...; outputFormatType: Text/XML/HTML/...; inputUrl: URL reference to image file")
	@WebResult(name = "ByUrlResponse", targetNamespace = "http://webservice.ocrComponents.commons.sub.unigoettingen.de/", partName = "part1")
	@WebMethod(action = "ocrImageFileByUrl")
	public ByUrlResponseType ocrImageFileByUrl(
			@WebParam(partName = "part1", name = "ByUrlRequest") ByUrlRequestType part1);

}
