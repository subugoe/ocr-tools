package de.uni_goettingen.sub.commons.ocr.api;

/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://www.sub.uni-goettingen.de 
 * 
 * Copyright 2009, 2010, SUB Goettingen.
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URI;

/**
 * The Interface OCRImage. This Interface represents the a single image file to
 * be recognized. Images are referenced by URI. {@link Orientation} is an Enum
 * representing different possible orientations of an image. Implementations
 * should extend {@link AbstractOCRImage} to add further methods for example for
 * handling Streams.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public interface OCRImage {

	/**
	 * Get an URI of an image.
	 * 
	 * @return the uri
	 */
	public URI getUri ();

	/**
	 * Sets the URI of an image.
	 * 
	 * @param uri
	 *            the new uri
	 */
	public void setUri (URI uri);

	/**
	 * Gets the {@link Orientation} of an image. Not all engines support this
	 * setting and may return garbage. If in doubt rotate the image files before
	 * recognizing them.
	 * 
	 * @return the rotation
	 */
	public Orientation getOrientation ();

	/**
	 * Sets the rotation of an image. Not all engines support this setting and
	 * may return garbage. If in doubt rotate the image files before recognizing
	 * them.
	 * 
	 * @param orientation
	 *            the new {@link Orientation}
	 */
	public void setOrientation (Orientation orientation);

	/**
	 * The Enum Orientation. Orientation is expressed clockwise. For
	 * calculations and display there is a method to get the rotation in
	 * degrees.
	 * 
	 * @version 0.9
	 * @author cmahnke
	 */
	public enum Orientation {

		/** PORTRAIT represents a rotation of 0 degrees. */
		PORTRAIT(0),
		/** LANDSCAPE represents a rotation of 90 degrees. */
		LANDSCAPE(90),
		/** COUNTER_PORTRAIT represents a rotation of 180 degrees. */
		COUNTER_PORTRAIT(180),
		/** COUNTER_LANDSCAPE represents a rotation of 270 degrees. */
		COUNTER_LANDSCAPE(270),
		/**
		 * MIXED represents uncertain rotation or an image with text floating in
		 * multiple directions. It's expressed as -1
		 */
		MIXED(-1);

		/** The DEGREE set for this Enum */
		public final Integer DEGREE;

		/**
		 * Instantiates a new orientation.
		 * 
		 * @param degree
		 *            the degree
		 */
		Orientation(Integer degree) {
			this.DEGREE = degree;
		}

		/**
		 * Gets the degree.
		 * 
		 * @return the degree, returns -1 if {@link Orientation} is set to MIXED
		 */
		public Integer getDegree () {
			return DEGREE;
		}

	}

}
