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

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Class OCRProcess represent an {@link OCRProcess}. Implementations should
 * extend {@link AbstractOCRProcess} to add further methods for example for
 * handling Streams. It's also possible to add preconfigured params there.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public interface OCRProcess {

	/**
	 * Gets the languages set for this process as List. These languages will be
	 * used for recognition. Not all engines are able to recognize each
	 * language. They will just ignore this setting.
	 * 
	 * @return the langs
	 * @see java.util.Locale
	 */
	public Set<Locale> getLangs ();

	/**
	 * Sets the languages set for this process as List. These languages will be
	 * used for recognition. Not all engines are able to recognize each
	 * language. They will just ignore this setting.
	 * 
	 * @param langs
	 *            the language
	 * @see java.util.Locale
	 */
	public void setLangs (Set<Locale> langs);

	/**
	 * Gets a List of {@link OCRImage}. These are the images that will be
	 * recognized.
	 * 
	 * @return the ocr image
	 * @see OCRImage
	 */
	public List<OCRImage> getOcrImages ();

	/**
	 * Sets a List of {@link OCRImage}. These are the images that will be
	 * recognized.
	 * 
	 * @param ocrImages
	 *            the new ocr images
	 * @see OCRImage
	 */
	public void setOcrImages (List<OCRImage> ocrImages);

	/**
	 * Sets the ocr output. This Map contains settings for the creation of a
	 * output format.
	 * 
	 * @param ocrOutput
	 *            the ocr output
	 * @see OCROutput
	 */
	public void setOcrOutput (Map<OCRFormat, OCROutput> ocrOutput);

	/**
	 * Gets the ocr output as a Map. The keys of this map represent the
	 * different possible formats. The values contain references to the results.
	 * 
	 * @return the ocr output
	 * @see OCROutput
	 */
	public Map<OCRFormat, OCROutput> getOcrOutput ();

	/**
	 * Sets the name of this {@link OCRProcess}. The nmae can be used by
	 * implementations to guess the name of the result file (if none is given)
	 * via {@link OCROutput}. The API doesn't guarantee that these names are
	 * unique, if you need it to be add a check in your {@link OCREngine}
	 * implementation.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName (String name);

	/**
	 * Gets the nameof this {@link OCRProcess}. The nae can be used by
	 * implementations to guess the name of the result file (if none is given)
	 * via {@link OCROutput}. The API doesn't guarantee that these names are
	 * unique, if you need it to be add a check in your {@link OCREngine}
	 * implementation.
	 * 
	 * @return the name
	 */
	public String getName ();

	/**
	 * Gets the params that should be used for recognition. Since these a
	 * specific to a {@link OCRProcess}, they should only be used to adjust the
	 * recognition options, not the output.
	 * 
	 * @return the params
	 */
	public Map<String, String> getParams ();

	/**
	 * Sets the params that should be used for recognition. Since these a
	 * specific to a {@link OCRProcess}, they should only be used to adjust the
	 * recognition options, not the output.
	 * 
	 * @param params
	 *            the params
	 */
	public void setParams (Map<String, String> params);

	/**
	 * Checks if this {@link OCRProcess} is finished. This method may throw an
	 * {@link OCRException} if the process failed
	 * 
	 * @return true if this {@link OCROutput} represents a result, false
	 *         otherwise
	 */
	public Boolean isFinished ();

}
