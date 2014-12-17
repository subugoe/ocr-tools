package de.uni_goettingen.sub.commons.ocr.tesseract;


import de.uni_goettingen.sub.commons.ocr.api.AbstractEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;

/**
 * Implementation of the engine that uses the tesseract cli tool
 */
public class TesseractOCREngine extends AbstractEngine implements OcrEngine {


	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OcrEngine#addOcrProcess(de.uni_goettingen.sub.commons.ocr.api.OcrProcess)
	 */
	@Override
	public void addOcrProcess(OcrProcess process) {
		ocrProcesses.add(process);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OcrEngine#recognize()
	 */
	@Override
	public void recognize() {

		for (OcrProcess process : ocrProcesses) {
			((TesseractOCRProcess) process).start();
		}

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
