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

// TODO: Auto-generated Javadoc
//TODO: Look at http://sites.google.com/site/openjdklocale/Home for language and script representation.
// Or use http://icu-project.org/apiref/icu4j/com/ibm/icu/lang/UScript.html for scripts
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
	 * @return the set of languages
	 * @see java.util.Locale
	 */
	abstract public Set<Locale> getLanguages ();

	/**
	 * Sets the languages set for this process as List. These languages will be
	 * used for recognition. Not all engines are able to recognize each
	 * language. They will just ignore this setting.
	 * 
	 * @param langs
	 *            the Set of languages
	 * @see java.util.Locale
	 */
	abstract public void setLanguages (Set<Locale> langs);

	/**
	 * Gets a List of {@link OCRImage}. These are the images that will be
	 * recognized.
	 * 
	 * @return the ocr image
	 * @see OCRImage
	 */
	abstract public List<OCRImage> getOcrImages ();

	/**
	 * Sets a List of {@link OCRImage}. These are the images that will be
	 * recognized.
	 * 
	 * @param ocrImages
	 *            the new ocr images
	 * @see OCRImage
	 */
	abstract public void setOcrImages (List<OCRImage> ocrImages);

	/**
	 * Sets the ocr output. This Map contains settings for the creation of a
	 * output format.
	 * 
	 * @param ocrOutputs
	 *            the ocr output
	 * @see OCROutput
	 */
	abstract public void setOcrOutputs (Map<OCRFormat, OCROutput> ocrOutputs);

	/**
	 * Gets the ocr output as a Map. The keys of this map represent the
	 * different possible formats. The values contain references to the results.
	 * 
	 * @return the ocr output
	 * @see OCROutput
	 */
	abstract public Map<OCRFormat, OCROutput> getOcrOutputs ();

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
	abstract public void setName (String name);

	/**
	 * Gets the nameof this {@link OCRProcess}. The nae can be used by
	 * implementations to guess the name of the result file (if none is given)
	 * via {@link OCROutput}. The API doesn't guarantee that these names are
	 * unique, if you need it to be add a check in your {@link OCREngine}
	 * implementation.
	 * 
	 * @return the name
	 */
	abstract public String getName ();

	/**
	 * Gets the params that should be used for recognition. Since these a
	 * specific to a {@link OCRProcess}, they should only be used to adjust the
	 * recognition options, not the output.
	 * 
	 * @return the params
	 */
	abstract public Map<String, String> getParams ();

	/**
	 * Sets the params that should be used for recognition. Since these a
	 * specific to a {@link OCRProcess}, they should only be used to adjust the
	 * recognition options, not the output.
	 * 
	 * @param params
	 *            the params
	 */
	abstract public void setParams (Map<String, String> params);

	/**
	 * Checks if this {@link OCRProcess} is finished. This method may throw an
	 * 
	 * @return true if this {@link OCROutput} represents a result, false
	 *         otherwise {@link OCRException} if the process failed
	 */
	abstract public Boolean isFinished ();

	/**
	 * Gets the OCR output metadata for this {@link OCRProcess}. This can be
	 * used to filter the results for accuracy or to save it for further
	 * processing. Implementations not generating this information should throw
	 * a {@link java.lang.UnsupportedOperationException}. The location and / or
	 * name of this method may change in future releases.
	 * 
	 * @return the OCR output metadata
	 * @see OCRProcessMetadata
	 */
	abstract OCRProcessMetadata getOcrProcessMetadata ();

	/**
	 * Gets the quality that should be produced by an engine while processing
	 * this process. Engine specific implementations of OCRProcess might throw a
	 *
	 * @return the quality setting thats currently used.
	 * {@link java.lang.UnsupportedOperationException} it it's not possible to
	 * use this setting.
	 */
	abstract OCRQuality getQuality ();

	/**
	 * Sets the quality that should be produced by an engine while processing
	 * this process. Engine specific implementations of OCRProcess might throw a
	 *
	 * @param q the new ocr quality
	 * {@link java.lang.UnsupportedOperationException} it it's not possible to
	 * use this setting. Note: It's noramlly not possible to change the quality
	 * of a running process, calls to this method will be ignored in this case.
	 */
	abstract void setQuality (OCRQuality q);

	/**
	 * The Enum OCRQuality. This enum represents three states of different
	 * quality settings: BEST, BALANCED and FAST.
	 */
	public enum OCRQuality {

		/** The BEST available quality, usually takes longer to create. */
		BEST,
		/**
		 * The BALANCED quality, this represents an engine specific tradeoff
		 * between speed and quality.
		 */
		BALANCED,
		
		/** The FAST "quality", this should the engine to work as fast as possible. */
		FAST;
	}
	
	/**
	 * Gets the texttyp. to describe the type of recognized text
	 *
	 * @return the texttyp
	 */
	abstract OCRTextTyp getTextTyp ();
	
	/**
	 * Sets the texttyp. to describe the type of recognized text
	 *
	 * @param t the new texttyp
	 */
	abstract void setTextTyp (OCRTextTyp t);
	
	/**
	 * The Enum TextTyp. This enum represents 7 states of different
	 * Typ of recognized text: Normal, Typewriter, Matrix, OCR_A, OCR_B, MICR_E13B, Gothic.
	 * enumeration constants are used to describe the type of recognized text
	 */
	public enum OCRTextTyp{
		
		/** The Normal type, This value corresponds to common typographic text. */
		Normal, 
		
		/** The Typewriter type,This value tells Open API to presume that the text 
		 * on the image was typed on a typewriter. 
		 * */
		Typewriter, 
		
		/** The Matrix type, This value tells Open API to presume that the text on 
		 * the image was printed by means of a dot-matrix printer. 
		 * */
		Matrix, 
		
		/** The OCR_A type, This value corresponds to a monospaced font designed 
		 * specifically for Optical Character Recognition. Largely used by banks, 
		 * credit card companies and similar businesses. 
		 * */
		OCR_A, 
		
		/** The OCR_B type,This value corresponds to a font designed specifically 
		 * for Optical Character Recognition. 
		 * */
		OCR_B, 
		
		/** The MICR_E13B type, This value corresponds to a special set of numeric 
		 * characters printed with special magnetic inks. MICR (Magnetic Ink Character 
		 * Recognition) characters are found in a variety of places, including personal 
		 * checks. 
		 * */
		MICR_E13B, 
		
		/** The Gothic type, This value tells Open API to presume that the text on the 
		 * image was printed in Gothic type. 
		 * */
		Gothic
	}

}
