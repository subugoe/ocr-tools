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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * The Interface OCRProcessMetadata can be used to obtain a description of the
 * {@link OCRProcess} and it's results This can be used to filter the results
 * for accuracy or to save it for further processing.
 * 
 * @version 0.9
 * @author cmahnke
 */
public interface OCRProcessMetadata {
	//Encoding settings
	/**
	 * Gets the encoding of the result file.
	 * 
	 * @return the encoding
	 */
	abstract public String getEncoding ();

	/**
	 * Gets a string representation of the line break used in the results. One
	 * of "CR, "CR/LF" or "LF".
	 * 
	 * @return the linebreak
	 */
	abstract public String getLinebreak ();

	/**
	 * Gets the format of the encoded result, consider using a controled
	 * vocabulary like this "SGML", "XML", "HTML", "TXT" and "XHTML". Note that
	 * implementations can return their own values since this is just a String.
	 * 
	 * @return the format
	 */
	abstract public String getFormat ();

	/**
	 * Gets the document type. This is valid for "SGML", "XML", "HTML" and
	 * "XHTML" and should reference a DTD or schema, if possible by using a URI.
	 * 
	 * @return the document type
	 */
	abstract public String getDocumentType ();

	/**
	 * Gets the document type version for the used document type.
	 * 
	 * @return the document type version
	 */
	abstract public String getDocumentTypeVersion ();

	//Creator settings
	/**
	 * Gets the name of the software used to encode / recognize the text.
	 * 
	 * @return the software name
	 */
	abstract public String getSoftwareName ();

	/**
	 * Gets the version of the software used to encode / recognize the text.
	 * 
	 * @return the software version
	 */
	abstract public String getSoftwareVersion ();

	//Language and script settings
	/**
	 * Gets the languages that are known to be used in the recognized text. This
	 * can be different from the languages used to recognize the text if the
	 * engine can detect languageses on their own.
	 * 
	 * @return the languages
	 */
	abstract public List<Locale> getLanguages ();

	/**
	 * Gets the scripts and / or fonts used in this text.
	 * 
	 * @return the scripts
	 */
	abstract public List<String> getScripts ();

	//Notes
	/**
	 * Gets a text note for the {OCRProcess}. This can be any string describing
	 * the recognized text.
	 * 
	 * @return the text note
	 */
	abstract public String getTextNote ();

	/**
	 * Gets processing note for the {OCRProcess}. This can be used to add a
	 * general note to the processing / recognition process. Implementors may
	 * choose this to encode additional machine readable data as escaped XML.
	 * 
	 * @return the processing note
	 */
	abstract public String getProcessingNote ();

	//Result specific metadata
	/**
	 * Gets the character accuracy. This is usually a engine specific setting.
	 * Implementations should try to convert this into a percentage value. If
	 * the engine isn't able to report the confidence level based on a process
	 * an {@link java.lang.UnsupportedOperationException} should be thrown.
	 * 
	 * @return the character accuracy
	 */
	abstract public BigDecimal getCharacterAccuracy ();

	/**
	 * Gets the word accuracy. This is usually a engine specific setting.
	 * Implementations should try to convert this into a percentage value. If
	 * the engine isn't able to report the confidence level based on a process
	 * an {@link java.lang.UnsupportedOperationException} should be thrown.
	 * 
	 * @return the word accuracy
	 */
	abstract public BigDecimal getWordAccuracy ();

	/**
	 * Gets the duration of a process.this returns 0 if the process hasn't
	 * started yet. The timing might be inaccurate if the process failed.
	 * Duration is expressed in milliseconds.
	 * 
	 * @return the duration
	 */
	abstract public Long getDuration ();

}
