package de.uni_goettingen.sub.commons.ocr.api;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractOCREngine implements OCREngine {
	
	protected List<OCRProcess> ocrProcess = new ArrayList<OCRProcess>();

	protected Boolean started = false;

	
	public List<OCRProcess> getOcrProcess () {
		return ocrProcess;
	}

	public void addOcrProcess (OCRProcess ocrp) {
		this.ocrProcess.add(ocrp);
	}

	public String getName () {
		return null;
	}
	
	public String getVersion () {
		return null;
	}

}
