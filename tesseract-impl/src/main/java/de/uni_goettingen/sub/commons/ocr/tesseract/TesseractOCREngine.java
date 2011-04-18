package de.uni_goettingen.sub.commons.ocr.tesseract;
import java.util.Observable;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;


public class TesseractOCREngine extends AbstractOCREngine implements OCREngine {

	private static TesseractOCREngine _instance;


	public static TesseractOCREngine getInstance() {
		
		if (_instance == null) {
			_instance = new TesseractOCREngine();
		}
		return _instance;
	}

	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable addOcrProcess(OCRProcess process) {
		// TODO: Check if this instanceof works as expected
		if (process instanceof TesseractOCRProcess) {
			ocrProcess.add((TesseractOCRProcess) process);
		} else {
			ocrProcess.add(new TesseractOCRProcess(process));
		}
		return null;
	}


	@Override
	public Boolean init() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable recognize(OCRProcess process) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable recognize() {
		
		for(OCRProcess process : ocrProcess) {
			((TesseractOCRProcess)process).start();
		}
		
		return null;
	}

	@Override
	public Boolean stop() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public OCRProcess newOcrProcess() {
		return new TesseractOCRProcess();
	}
}
