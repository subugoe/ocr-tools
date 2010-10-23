package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URI;

public abstract class AbstractOCRImage implements OCRImage {
	protected URI imageUri = null;

	/** rotation of the image. */
	protected Orientation orientation;

	public AbstractOCRImage(URI imageUri) {
		this.imageUri = imageUri;
	}

	protected AbstractOCRImage() {

	}

	public AbstractOCRImage(OCRImage i) {
		this(i.getUri(), i.getOrientation());
	}

	public AbstractOCRImage(URI imageUri, Orientation orientation) {
		this.imageUri = imageUri;
		this.orientation = orientation;
	}

	/**
	 * get an Url for a image.
	 * 
	 * @return the url
	 */
	public URI getUri () {
		return this.imageUri;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            the new url
	 */
	public void setUri (URI uri) {
		this.imageUri = uri;
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
