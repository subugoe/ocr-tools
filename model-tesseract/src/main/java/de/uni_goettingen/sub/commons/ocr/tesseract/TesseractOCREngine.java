package de.uni_goettingen.sub.commons.ocr.tesseract;


import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

/**
 * Implementation of the engine that uses the tesseract cli tool
 */
public class TesseractOCREngine extends AbstractOCREngine implements OCREngine {


	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#addOcrProcess(de.uni_goettingen.sub.commons.ocr.api.OCRProcess)
	 */
	@Override
	public void addOcrProcess(OCRProcess process) {
		ocrProcess.add(process);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#recognize()
	 */
	@Override
	public void recognize() {

		for (OCRProcess process : ocrProcess) {
			((TesseractOCRProcess) process).start();
		}

	}

	@Override
	public int getEstimatedDurationInSeconds() {
		int duration = 0;
		for (OCRProcess process : ocrProcess) {
			int imagesInProcess = process.getNumberOfImages();
			duration += imagesInProcess * 5;
		}
		return duration;
	}
}
