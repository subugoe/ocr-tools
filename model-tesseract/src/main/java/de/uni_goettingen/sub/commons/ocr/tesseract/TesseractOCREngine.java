package de.uni_goettingen.sub.commons.ocr.tesseract;

import java.util.Observable;

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
		if (process instanceof TesseractOCRProcess) {
			ocrProcess.add((TesseractOCRProcess) process);
		} else {
			ocrProcess.add(new TesseractOCRProcess(process));
		}
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#init()
	 */
	@Override
	public Boolean init() {
		throw new UnsupportedOperationException();
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

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#stop()
	 */
	@Override
	public Boolean stop() {
		throw new UnsupportedOperationException();
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
