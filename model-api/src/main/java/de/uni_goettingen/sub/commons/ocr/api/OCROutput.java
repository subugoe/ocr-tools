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
 * The Interface OCROutput represents the expected results before processing and
 * references to the results if the processing is done. Implementations should
 * extend {@link AbstractOCROutput} to add further methods for example for
 * handling Streams. It's also possible to add preconfigured params there.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public interface OCROutput {

	/**
	 * Gets the uri where the results should be stored. If {@link #isResult()}
	 * is true, the result should be at this location.
	 * 
	 * @return the uri
	 */
	public URI getLocalUri();

	/**
	 * Sets the uri for the result. If {@link #isResult()} is true, the result
	 * should be at this location.
	 * 
	 * @param uri
	 *            the new uri
	 */
	public void setUri(URI uri);
	
	/**
	 * Gets the String Dir where the results should be stored. If {@link #isResult()}
	 * is true, the result should be at this location.
	 * 
	 * @return the String dir
	 */
	public String getLocalDir();
	
	/**
	 * Sets the String Dir for the result. If {@link #isResult()} is true, the result
	 * should be at this location.
	 * 
	 * @param uri the new dir
	 */
	public void setLocalDir(String dir);
	
	public void setFormat(OCRFormat format);
	public OCRFormat getFormat();
	
}
