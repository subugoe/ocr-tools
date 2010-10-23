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
import java.util.Observer;

// TODO: Auto-generated Javadoc
/**
 * The Interface OCREngine.
 */
public interface OCREngine {

	/**
	 * Recognize. Start
	 * 
	 * @param process
	 *            the process
	 * @return the observer
	 */
	public Observer recognize (OCRProcess process);

	/**
	 * Recognize the list of given OCRProcess. Throws an IllegalStateException
	 * if no process was added. Does nothing the recognizer is already working.
	 * 
	 * @return the observer
	 */
	public Observer recognize ();

	/**
	 * Stops a running recognizer. Returns false if the recognizer isn't
	 * running.
	 * 
	 * @return the boolean
	 */
	public Boolean stop ();

	/**
	 * Adds a OCR process.
	 * 
	 * @param ocrp
	 *            the ocrp
	 * @return the observer
	 */

	public Observer addOcrProcess (OCRProcess ocrp);

	/**
	 * Gets the OCR process.
	 * 
	 * @return the oCR process
	 */
	public List<OCRProcess> getOcrProcess ();

	/**
	 * New ocr image.
	 * 
	 * @return the OCR image
	 */
	public OCRImage newOCRImage ();

	/**
	 * New ocr process.
	 * 
	 * @return the OCR process
	 */
	public OCRProcess newOCRProcess ();

	/**
	 * New ocr output.
	 * 
	 * @return the oCR output
	 */
	public OCROutput newOCROutput ();

	/**
	 * Inits the OCREngine. This an be used to check if the engine is
	 * operational. Implementtions should implement this metho to chek if an
	 * Engine is licenced or a server component can be reached.
	 * 
	 * @return the boolean
	 */
	public Boolean init ();

}
