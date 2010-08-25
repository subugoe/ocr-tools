package de.uni_goettingen.sub.commons.ocr.api.exceptions;

public class OCRException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8431331102096561551L;

	public OCRException (Exception e) {
		super(e);
	}
	
	public OCRException (String str) {
		super(str);
	}
	
}
