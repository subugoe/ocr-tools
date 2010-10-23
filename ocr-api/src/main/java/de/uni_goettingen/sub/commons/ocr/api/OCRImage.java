package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URI;

// TODO: Auto-generated Javadoc
/**
 * The Interface OCRImage. This Interface represents the a single image file to
 * be recognized. Images are referenced by URI.
 */
public interface OCRImage {

	/**
	 * Get an URI of an image.
	 * 
	 * @return the uri
	 */
	public URI getUri ();

	/**
	 * Sets the URL of an image.
	 * 
	 * @param uri
	 *            the new uri
	 */
	public void setUri (URI uri);

	/**
	 * Gets the rotation.
	 * 
	 * @return the rotation
	 */
	public Orientation getOrientation ();

	/**
	 * Sets the rotation.
	 *
	 * @param orientation the new orientation
	 */
	public void setOrientation (Orientation orientation);

	/**
	 * The Enum Orientation.
	 */
	public enum Orientation {
		
		/** The PORTRAIT. */
		PORTRAIT(0), 
 /** The LANDSCAPE. */
 LANDSCAPE(90), 
 /** The COUNTE r_ portrait. */
 COUNTER_PORTRAIT(180), 
 /** The COUNTE r_ landscape. */
 COUNTER_LANDSCAPE(270);

		/** The DEGREE. */
		public final Integer DEGREE;

		/**
		 * Instantiates a new orientation.
		 *
		 * @param degree the degree
		 */
		Orientation(Integer degree) {
			this.DEGREE = degree;
		}

		/**
		 * Gets the degree.
		 *
		 * @return the degree
		 */
		public Integer getDegree () {
			return DEGREE;
		}

	}

}
