package de.uni_goettingen.sub.commons.ocr.api.exceptions;

// TODO: Auto-generated Javadoc
/**
 * The Class OCRException.
 */
public class OCRException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8431331102096561551L;

	/**
	 * Instantiates a new OCRException.
	 */
	public OCRException () {
		super();
	}
	
	/**
	 * Instantiates a new OCRException.
	 *
	 * @param e, a wrapped Exception.
	 */
	public OCRException (Exception e) {
		super(e);
	}
	
	/**
	 * Instantiates a new OCRException.
	 *
	 * @param str, a message as String.
	 */
	public OCRException (String str) {
		super(str);
	}
}
