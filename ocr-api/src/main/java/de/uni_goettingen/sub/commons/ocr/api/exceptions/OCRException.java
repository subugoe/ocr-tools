package de.uni_goettingen.sub.commons.ocr.api.exceptions;

// TODO: Auto-generated Javadoc
/**
 * The Class OCRException is a {@link RuntimeException} to signal OCR related
 * errors. <b>Please note: The API isn't using any checked exceptions.</b>
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public class OCRException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8431331102096561551L;

	/**
	 * Instantiates a new OCRException.
	 */
	public OCRException() {
		super();
	}

	/**
	 * Instantiates a new OCRException.
	 * 
	 * @param t
	 *            the wrapped Throwable
	 */
	public OCRException(Throwable t) {
		super(t);
	}

	/**
	 * Instantiates a new OCRException.
	 * 
	 * @param str
	 *            the message as String.
	 */
	public OCRException(String str) {
		super(str);
	}

	/**
	 * Instantiates a new OCRException.
	 * 
	 * @param str
	 *            the message as String.
	 * @param t
	 *            the wrapped Throwable
	 */
	public OCRException(String str, Throwable t) {
		super(str, t);
	}
}
