package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URL;

/**
 * The Interface OCRImage. This Interface represents the a single image file to be recognized.
 * Images are referenced by URL.
 */
public interface OCRImage {

	/**
	 * Get an URL of an image.
	 * 
	 * @return the url
	 */
	public URL getUrl ();

	/**
	 * Sets the URL of an image.
	 * 
	 * @param url
	 *            the new url
	 */
	public void setUrl (URL url);

	/**
	 * Gets the rotation.
	 * 
	 * @return the rotation
	 */
	public Orientation getOrientation ();

	/**
	 * Sets the rotation.
	 * 
	 * @param rotation
	 *            the new rotation
	 */
	public void setOrientation (Orientation orientation);

	/**
	 * The Enum Orientation.
	 */
	public enum Orientation {
		PORTRAIT(0), LANDSCAPE (90), COUNTER_PORTRAIT(180), COUNTER_LANDSCAPE(270);
		
		public final Integer DEGREE;
		
		Orientation (Integer degree) {
			this.DEGREE = degree;
		}
		
		public Integer getDegree () {
			return DEGREE;
		}
		
	}
	
}
