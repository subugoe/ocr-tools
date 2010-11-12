package de.uni_goettingen.sub.commons.ocr.api;

/*

Copyright 2010 SUB Goettingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

import java.net.URI;

import de.uni_goettingen.sub.commons.ocr.api.OCRImage.Orientation;

/**
 * The Class AbstractOCRImage is a abstract super class for {@link OCRImage}
 * implementations. {@link OCRImage} represents an image as a {@link URI}
 * reference and a orientation.
 * 
 * @version 0.9
 * @author cmahnke
 */
public abstract class AbstractOCRImage implements OCRImage {

	/**  uri to image wich will be sent to OCR Engine */
	protected URI imageUri = null;

	/** The Enum Orientation. Orientation is expressed clockwise. For
	 * calculations and display there is a method to get the rotation in
	 * degrees.
	 *  */
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
	 *            The OCRImage. This represents the a single
	 *            image file to be recognized. Images are referenced by URI.
	 *            {@link Orientation} is an Enum representing different possible
	 *            orientations of an image. Implementations should extend
	 *            {@link AbstractOCRImage} to add further methods for example
	 *            for handling Streams.
	 */
	public AbstractOCRImage(OCRImage i) {
		this(i.getUri(), i.getOrientation());
	}

	/**
	 * Instantiates a new abstract ocr image with the given arguments.
	 * 
	 * @param imageUri
	 *            the image, uri to image wich will be sent to OCR Engine
	 * @param orientation
	 *           	The Enum Orientation. Orientation is expressed clockwise. For
	 * 				calculations and display there is a method to get the rotation in
	 * 				degrees.
	 */
	public AbstractOCRImage(URI imageUri, Orientation orientation) {
		this.imageUri = imageUri;
		this.orientation = orientation;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRImage#getUri()
	 */
	public URI getUri () {
		return this.imageUri;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRImage#setUri(java.net.URI)
	 */
	public void setUri (URI uri) {
		this.imageUri = uri;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRImage#getOrientation()
	 */
	public Orientation getOrientation () {
		return orientation;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRImage#setOrientation(OCRImage.Orientation)
	 */
	public void setOrientation (Orientation orientation) {
		this.orientation = orientation;
	}

}
