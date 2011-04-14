package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class TesseractOCRProcess extends AbstractOCRProcess implements
		OCRProcess {

	
	public TesseractOCRProcess(OCRProcess process){
		super(process);
	}
	public TesseractOCRProcess(){
		super();
	}
	
	@Override
	public void addOutput(OCRFormat format, OCROutput output) {
		
		if (ocrOutputs == null) {
			// We use a LinkedHashMap to get the order of the elements
			// predictable
			ocrOutputs = new LinkedHashMap<OCRFormat, OCROutput>();
		}
		ocrOutputs.put(format, output);
	}

	
}
