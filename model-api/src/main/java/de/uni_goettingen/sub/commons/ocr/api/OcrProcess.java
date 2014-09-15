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

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OcrException;



//TODO: Look at http://sites.google.com/site/openjdklocale/Home for language and script representation.
// Or use http://icu-project.org/apiref/icu4j/com/ibm/icu/lang/UScript.html for scripts
/**
 * The Class OcrProcess represent an {@link OcrProcess}. Implementations should
 * extend {@link AbstractProcess} to add further methods for example for
 * handling Streams. It's also possible to add preconfigured params there.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public interface OcrProcess {

	/**
	 * Gets the languages set for this process as List. These languages will be
	 * used for recognition. Not all engines are able to recognize each
	 * language. They will just ignore this setting.
	 * 
	 * @return the set of languages
	 * @see java.util.Locale
	 */
	public Set<Locale> getLanguages();

	public void addLanguage(Locale lang);
		
	public void addImage(URI localUri, long fileSize);

	public int getNumberOfImages();
	
	public void setOutputDir(File outputDir);

	/**
	 * Gets the ocr output as a Map. The keys of this map represent the
	 * different possible formats. The values contain references to the results.
	 * 
	 * @return the ocr output
	 * @see OcrOutput
	 */
	public List<OcrOutput> getOcrOutputs();

	/**
	 * Adds the output for the given format
	 * 
	 * @param format
	 *            the format to add
	 * @param output
	 *            the output, the output settings for the given format
	 * 
	 */
	public void addOutput(OcrFormat format);
	
	/**
	 * Sets the name of this {@link OcrProcess}. The nmae can be used by
	 * implementations to guess the name of the result file (if none is given)
	 * via {@link OcrOutput}. The API doesn't guarantee that these names are
	 * unique, if you need it to be add a check in your {@link OcrEngine}
	 * implementation.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name);

	/**
	 * Gets the nameof this {@link OcrProcess}. The nae can be used by
	 * implementations to guess the name of the result file (if none is given)
	 * via {@link OcrOutput}. The API doesn't guarantee that these names are
	 * unique, if you need it to be add a check in your {@link OcrEngine}
	 * implementation.
	 * 
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Gets the quality that should be produced by an engine while processing
	 * this process. Engine specific implementations of OcrProcess might throw a
	 * 
	 * @return the quality setting thats currently used.
	 *         {@link java.lang.UnsupportedOperationException} it it's not
	 *         possible to use this setting.
	 */
	public OcrQuality getQuality();

	/**
	 * Sets the quality that should be produced by an engine while processing
	 * this process. Engine specific implementations of OcrProcess might throw a
	 * 
	 * @param q
	 *            the new ocr quality
	 *            {@link java.lang.UnsupportedOperationException} it it's not
	 *            possible to use this setting. Note: It's noramlly not possible
	 *            to change the quality of a running process, calls to this
	 *            method will be ignored in this case.
	 */
	public void setQuality(OcrQuality q);


	/**
	 * Gets the texttyp. to describe the type of recognized text
	 * 
	 * @return the texttyp
	 */
	public OcrTextType getTextType();

	/**
	 * Sets the texttyp. to describe the type of recognized text
	 * 
	 * @param t
	 *            the new texttyp
	 */
	public void setTextType(OcrTextType t);

	/**
	 * The Enum TextTyp. This enum represents 7 states of different
	 * Typ of recognized text: Normal, Typewriter, Matrix, OCR_A, OCR_B, MICR_E13B, Gothic.
	 * enumeration constants are used to describe the type of recognized text
	 */

	/**
	 * Gets the priority. to describe the level of the job.
	 * 
	 * @return the priority
	 */
	public OcrPriority getPriority();

	/**
	 * Sets the priority. to describe the level of the job.
	 * 
	 * @param p
	 *            the new priority
	 */
	public void setPriority(OcrPriority p);
	
}
