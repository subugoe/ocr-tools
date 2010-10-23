package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URI;

/**
 * The Class AbstractOCRImage is a abstract super class for {@link OCRImage}
 * implementations. {@link OCRImage} represents an image as a {@link URI}
 * reference and a orientation.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public abstract class AbstractOCRImage implements OCRImage {

	/** The image uri. */
	protected URI imageUri = null;

	/** orietation of the image. */
	protected Orientation orientation;

	/**
	 * Instantiates a new abstract OCRImage using a given {@link URI}.
	 * 
	 * @param imageUri
	 *            the image uri
	 */
	public AbstractOCRImage(URI imageUri) {
		this.imageUri = imageUri;
	}

	/**
	 * Instantiates a new abstract ocr image.
	 */
	protected AbstractOCRImage() {

	}

	/**
	 * Instantiates a new abstract ocr image from a given {@link OCRImage}. This
	 * is a simple copy constructor that can be used by subclasses. It can be
	 * used to convert different subclasses into each other
	 * 
	 * @param i
	 *            the i
	 */
	public AbstractOCRImage(OCRImage i) {
		this(i.getUri(), i.getOrientation());
	}

	/**
	 * Instantiates a new abstract ocr image with the given arguments.
	 * 
	 * @param imageUri
	 *            the image uri
	 * @param orientation
	 *            the orientation
	 */
	public AbstractOCRImage(URI imageUri, Orientation orientation) {
		this.imageUri = imageUri;
		this.orientation = orientation;
	}

	/**
	 * get an Uri for a image.
	 * 
	 * @return the uri
	 */
	public URI getUri () {
		return this.imageUri;
	}

	/**
	 * Sets the {@link URI} of an image. URI is used to be able to use different
	 * resolvers to return an {@link InputStream}.
	 * 
	 * @param uri
	 *            the new uri
	 */
	public void setUri (URI uri) {
		this.imageUri = uri;
	}

	/**
	 * Gets the rotation of an image.
	 * 
	 * @return the {@link Orientation}
	 */
	public Orientation getOrientation () {
		return orientation;
	}

	/**
	 * Sets the rotation of an image, this can be used to ensure that images are
	 * recognized in the right viewing direction
	 * 
	 * @param orientation
	 *            the new the {@link Orientation}
	 */
	public void setOrientation (Orientation orientation) {
		this.orientation = orientation;
	}

}
