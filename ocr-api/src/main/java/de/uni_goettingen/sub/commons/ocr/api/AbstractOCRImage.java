package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URL;

public abstract class AbstractOCRImage implements OCRImage {
	protected URL imageUrl = null;

	public AbstractOCRImage(URL imageUrl) {
		this.imageUrl = imageUrl;
	}

	protected AbstractOCRImage () {
		
	}
	
	public AbstractOCRImage(OCRImage i) {
		this.imageUrl = i.getUrl();
		this.orientation = getOrientation();
	}

	
	/** directory of the images. */
	public URL url;

	/** rotation of the image. */
	public Orientation orientation;

	/**
	 * get an Url for a image.
	 * 
	 * @return the url
	 */
	public URL getUrl () {
		return this.url;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            the new url
	 */
	public void setUrl (URL url) {
		this.url = url;
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
