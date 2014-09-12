package de.uni_goettingen.sub.commons.ocr.api.exceptions;

/**
 * The Class OcrException is a {@link RuntimeException} to signal OCR related
 * errors. <b>Please note: The API isn't using any checked exceptions.</b>
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public class OcrException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8431331102096561551L;

	/**
	 * Instantiates a new OcrException.
	 */
	public OcrException() {
		super();
	}

	/**
	 * Instantiates a new OcrException.
	 * 
	 * @param t
	 *            the wrapped Throwable
	 */
	public OcrException(Throwable t) {
		super(t);
	}

	/**
	 * Instantiates a new OcrException.
	 * 
	 * @param str
	 *            the message as String.
	 */
	public OcrException(String str) {
		super(str);
	}

	/**
	 * Instantiates a new OcrException.
	 * 
	 * @param str
	 *            the message as String.
	 * @param t
	 *            the wrapped Throwable
	 */
	public OcrException(String str, Throwable t) {
		super(str, t);
	}
}
