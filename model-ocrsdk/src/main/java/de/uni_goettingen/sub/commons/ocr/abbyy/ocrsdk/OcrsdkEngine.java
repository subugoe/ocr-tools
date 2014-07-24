package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;


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
	 * Starts all processes that have been added before.
	 */
	@Override
	public void recognize() {
		for (OCRProcess process : ocrProcess) {
			((OcrsdkProcess)process).start();
		}
	}

	@Override
	public void addOcrProcess(OCRProcess ocrp) {
		ocrProcess.add(ocrp);
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
