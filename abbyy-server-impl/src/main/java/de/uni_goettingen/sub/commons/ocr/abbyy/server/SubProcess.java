package de.uni_goettingen.sub.commons.ocr.abbyy.server;


import java.util.List;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class SubProcess extends AbbyyOCRProcess{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected SubProcess() {
		super(config);
		clearOcrImageList();
	}

	
	@Override
	public void addOutput(OCRFormat format, OCROutput output) {
		super.addOutput(format, output);
	}
	

	public void setListOfsp(List<SubProcess> listOfsp) {
		//TODO
	}
	
	@Override
	public Boolean getIsFinished() {
		return super.isFinished;
	}
	@Override
	public void setIsFinished(Boolean isFinished) {
		super.isFinished = isFinished;
	}
}
