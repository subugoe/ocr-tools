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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class OCRProcess represent an {@link OCRProcess}
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public interface OCRProcess {

	/**
	 * Gets the languages set for this process as List.
	 * 
	 * @return the langs
	 */
	public Set<Locale> getLangs ();

	/**
	 * Gets a List of {@link OCRImage}.
	 * 
	 * @return the ocr image
	 */
	public List<OCRImage> getOcrImages ();

	/**
	 * Sets a List of {@link OCRImage}.
	 * 
	 * @param ocrImages
	 *            the new ocr images
	 */
	public void setOcrImages (List<OCRImage> ocrImages);

	/**
	 * Sets the ocr output.
	 * 
	 * @param ocrOutput
	 *            the ocr output
	 */
	public void setOcrOutput (Map<OCRFormat, OCROutput> ocrOutput);

	/**
	 * Gets the ocr output as a Map. The keys of this map represent the different
	 * possible formats. The values contain references to the results.
	 * 
	 * @return the ocr output
	 */
	public Map<OCRFormat, OCROutput> getOcrOutput ();

	/**
	 * Sets the name of this {@link OCRProcess}. The nae can be used by implementations
	 * to guess the name of the result file (if none is given) via {@link OCROutput}.
	 * The API doesn't guarantee that these names are unique.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName (String name);

	/**
	 * Gets the nameof this {@link OCRProcess}. The nae can be used by implementations
	 * to guess the name of the result file (if none is given) via {@link OCROutput}.
	 * The API doesn't guarantee that these names are unique.
	 * 
	 * @return the name
	 */
	public String getName ();
	
	/**
	 * Gets the params that should be used for recognition. Since these a
	 * specific to a {@link OCRFormat}, they should only be used to adjust the
	 * output, not for recognition options.
	 * 
	 * @return the params
	 */
	public Map<String, String> getParams ();

	/**
	 * Sets the params.
	 * 
	 * @param params
	 *            the params
	 */
	public void setParams (Map<String, String> params);

}
