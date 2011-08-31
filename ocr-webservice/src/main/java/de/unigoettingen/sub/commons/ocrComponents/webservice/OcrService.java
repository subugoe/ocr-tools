package de.unigoettingen.sub.commons.ocrComponents.webservice;
/*

Copyright 2010 SUB Goettingen. All rights reserved.
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
import javax.jws.WebResult;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.annotations.WSDLDocumentation;
import org.apache.cxf.annotations.WSDLDocumentationCollection;

@WSDLDocumentationCollection({
		@WSDLDocumentation("Apply text recognition to image file (sending/receiving data by URL)"),
		@WSDLDocumentation(value = "IMPACT Abbyy Fine Reader 8.0 Service. This service provides the basic functionality of the Abbyy Fine Reader 8.0 text recogntion engine for applying OCR to an image file.", placement = WSDLDocumentation.Placement.TOP) })
@WebService(targetNamespace = "http://webservice.ocrComponents.commons.sub.unigoettingen.de/", name = "IMPACTServicePortType")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface OcrService {
	@WSDLDocumentation("inputTextType: Normal/Typewriter/Gothic/ToBeDetected...; outputFormatType: Text/XML/HTML/...; inputUrl: URL reference to image file")
	@WebResult(name = "ByUrlResponse", targetNamespace = "http://webservice.ocrComponents.commons.sub.unigoettingen.de/", partName = "part1")
	@WebMethod(action = "ocrImageFileByUrl")
	public ByUrlResponseType ocrImageFileByUrl(
			@WebParam(partName = "part1", name = "ByUrlRequest") ByUrlRequestType part1);

}
