package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.net.URI;
import java.util.Map;
import java.util.Observable;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class OcrsdkEngine extends AbstractOCREngine {

	private static OcrsdkEngine instance;

	public static synchronized OcrsdkEngine getInstance() {

		if (instance == null) {
			instance = new OcrsdkEngine();
		}
		return instance;
	}

	@Override
	public Observable recognize(OCRProcess process) {
		addOcrProcess(process);
		recognize();
		return null;
	}

	@Override
	public Observable recognize() {
		for (OCRProcess process : ocrProcess) {
			((OcrsdkProcess)process).start();
		}
		return null;
	}

	@Override
	public OCRProcess newOcrProcess() {
		return new OcrsdkProcess("", "");
	}
	
	@Override
	public Observable addOcrProcess(OCRProcess ocrp) {
		ocrProcess.add(ocrp);
		return null;
	}

	@Override
	public OCRImage newOcrImage(URI imageUri) {
		return new OcrsdkImage(imageUri);
	}
	
	@Override
	public Boolean stop() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Boolean init() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(Map<String, String> params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> getOptions() {
		// TODO Auto-generated method stub
		return null;
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

}
