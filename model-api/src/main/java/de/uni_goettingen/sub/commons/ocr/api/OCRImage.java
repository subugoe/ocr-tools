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
	 * Gets the {@link URI} of the image. URI is used to be able to use
	 * different resolvers to return an {@link java.io.InputStream}.
	 * 
	 * @return the uri
	 */
	abstract public URI getLocalUri();

	/**
	 * Sets the {@link URI} of the image. URI is used to be able to use
	 * different resolvers to return an {@link java.io.InputStream}.
	 * 
	 * @param uri
	 *            the new uri
	 */
	abstract public void setLocalUri(URI localUri);
	
	/**
	 * Sets the size of file
	 * @param size 
	 */
	public void setFileSize(long fileSize);
	
	public long getFileSize();

}
