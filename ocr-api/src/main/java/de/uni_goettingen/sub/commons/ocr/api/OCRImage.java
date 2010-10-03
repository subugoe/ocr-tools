package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URL;

/**
 * The Interface OCRImage. The Images which should be converted
 */
public interface OCRImage {

	/**
	 * get an Url for a image.
	 * 
	 * @return the url
	 */
	public URL getUrl ();

	/**
	 * Sets the url.
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

	public enum Orientation {
		PORTRAIT(0), LANDSCAPE (90), COUNTER_PORTRAIT(180), COUNTER_LANDSCAPE(270);
		
		public final Integer DEGREE;
		
		Orientation (Integer degree) {
			this.DEGREE = degree;
		}
		
	}
	
}
