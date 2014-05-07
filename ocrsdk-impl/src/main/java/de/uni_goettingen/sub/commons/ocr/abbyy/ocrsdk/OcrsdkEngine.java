package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

/**
 * Encapsulates one or more OCR processes that can be started.
 * Also, serves as a factory for images, prorcesses, and outputs.
 * 
 * @author dennis
 *
 */
public class OcrsdkEngine extends AbstractOCREngine {

	private static OcrsdkEngine instance;
	private Map<String, String> extraOptions = new HashMap<String, String>();

	public static synchronized OcrsdkEngine getInstance() {

		if (instance == null) {
			instance = new OcrsdkEngine();
		}
		return instance;
	}

	// we need this for our Web Service, because each request needs its own instance
	public static OcrsdkEngine newOCREngine() {	
		return new OcrsdkEngine();
	}

	/**
	 * Starts the one process that is passed.
	 */
	@Override
	public Observable recognize(OCRProcess process) {
		addOcrProcess(process);
		recognize();
		return null;
	}

	/**
	 * Starts all processes that have been added before.
	 */
	@Override
	public Observable recognize() {
		for (OCRProcess process : ocrProcess) {
			((OcrsdkProcess)process).start();
		}
		return null;
	}

	@Override
	public OCRProcess newOcrProcess() {
		String appId = extraOptions.get("user");
		String password = extraOptions.get("password");
		if (appId == null || appId.equals("") || password == null || password.equals("")) {
			throw new IllegalArgumentException("You have to provide the AppId and the password.");
		}
		return new OcrsdkProcess(appId, password);
	}
	
	@Override
	public Observable addOcrProcess(OCRProcess ocrp) {
		ocrProcess.add(ocrp);
		return null;
	}

	@Override
	public void setOptions(Map<String, String> opts) {
		extraOptions = opts;
	}

	@Override
	public Map<String, String> getOptions() {
		return extraOptions;
	}

	@Override
	public OCRImage newOcrImage(URI imageUri) {
		return new OcrsdkImage(imageUri);
	}
	
	@Override
	public OCROutput newOcrOutput() {
		return new OcrsdkOutput();
	}
	
	@Override
	public Boolean stop() {
		return false;
	}
	
	@Override
	public Boolean init() {
		return false;
	}

	@Override
	public int getEstimatedDurationInSeconds() {
		int duration = 0;
		for (OCRProcess process : ocrProcess) {
			int imagesInProcess = process.getOcrImages().size();
			duration += imagesInProcess * 5;
		}
		return duration;
	}

}
