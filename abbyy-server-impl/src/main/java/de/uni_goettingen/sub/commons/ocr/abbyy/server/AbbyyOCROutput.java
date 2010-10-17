package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class AbbyyOCROutput extends AbstractOCROutput {

	protected String remoteLocation;
	
	public AbbyyOCROutput(OCROutput ocrOutput) {
		super(ocrOutput);
	}

	public AbbyyOCROutput() {
		
	}

	/**
	 * @return the remoteLocation
	 */
	public String getRemoteLocation () {
		return remoteLocation;
	}

	/**
	 * @param remoteLocation the remoteLocation to set
	 */
	public void setRemoteLocation (String remoteLocation) {
		this.remoteLocation = remoteLocation;
	}
	
	

}
