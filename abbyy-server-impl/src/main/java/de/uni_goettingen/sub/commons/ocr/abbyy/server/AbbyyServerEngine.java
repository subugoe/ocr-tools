package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Observer;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;


public class AbbyyServerEngine implements OCREngine{
	
	protected Integer maxThreads = 5;
	protected ExecutorService pool = new OCRExecuter(maxThreads);

	
	
	public AbbyyServerEngine(){		
	}
	
	@Override
	public void recognize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOCRProcess(OCRProcess ocrp) {
		// TODO Auto-generated method stub
		//this.ocrp = ocrp; 
	}

	@Override
	public OCRProcess getOCRProcess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OCROutput getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	protected finalize () {
		pool.shutdown();
		try {
			//TODO: Calculate the right expected timeout
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
	}

}
