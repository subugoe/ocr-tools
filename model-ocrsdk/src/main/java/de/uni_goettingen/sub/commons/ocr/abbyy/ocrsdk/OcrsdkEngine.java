package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;


import de.uni_goettingen.sub.commons.ocr.api.AbstractEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;

/**
 * Encapsulates one or more OCR processes that can be started.
 * Also, serves as a factory for images, prorcesses, and outputs.
 * 
 * @author dennis
 *
 */
public class OcrsdkEngine extends AbstractEngine {

	/**
	 * Starts all processes that have been added before.
	 */
	@Override
	public void recognize() {
		for (OcrProcess process : ocrProcesses) {
			((OcrsdkProcess)process).start();
		}
	}

	@Override
	public void addOcrProcess(OcrProcess ocrp) {
		ocrProcesses.add(ocrp);
	}

	@Override
	public int getEstimatedDurationInSeconds() {
		int duration = 0;
		for (OcrProcess process : ocrProcesses) {
			int imagesInProcess = process.getNumberOfImages();
			duration += imagesInProcess * 5;
		}
		return duration;
	}

}
