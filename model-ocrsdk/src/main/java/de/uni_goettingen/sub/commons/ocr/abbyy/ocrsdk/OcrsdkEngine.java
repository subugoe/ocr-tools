package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.util.Observable;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

/**
 * Encapsulates one or more OCR processes that can be started.
 * Also, serves as a factory for images, prorcesses, and outputs.
 * 
 * @author dennis
 *
 */
public class OcrsdkEngine extends AbstractOCREngine {

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
	public Observable addOcrProcess(OCRProcess ocrp) {
		ocrProcess.add(ocrp);
		return null;
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
