package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URL;

public abstract class AbstractOCRImage implements OCRImage {
	protected URL imageUrl = null;
	
	/** rotation of the image. */
	protected Orientation orientation;

	public AbstractOCRImage(URL imageUrl) {
		this.imageUrl = imageUrl;
	}

	protected AbstractOCRImage () {
		
	}
	
	public AbstractOCRImage(OCRImage i) {
		this.imageUrl = i.getUrl();
		this.orientation = i.getOrientation();
	}

	/**
	 * get an Url for a image.
	 * 
	 * @return the url
	 */
	public URL getUrl () {
		return this.imageUrl;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            the new url
	 */
	public void setUrl (URL url) {
		this.imageUrl= url;
	}

	/**
	 * Gets the rotation.
	 * 
	 * @return the rotation
	 */
	public Orientation getOrientation () {
		return orientation;
	}

	/**
	 * Sets the rotation.
	 * 
	 * @param rotation
	 *            the new rotation
	 */
	public void setOrientation (Orientation orientation) {
		this.orientation = orientation;
	}

}
