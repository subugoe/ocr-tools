package de.uni_goettingen.sub.commons.ocr.api;

import java.util.Observer;

public interface OCREngine {

	
	  public void recognize ();
	
	  public void setOCRProcess(OCRProcess process);

	  public OCRProcess getOCRProcess();

	  public OCROutput getResult();

	  public void setObserver(Observer observer);
	  	  
}
