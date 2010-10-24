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
import java.util.Observable;

/**
 * The Interface OCREngine is the main entry point for each engine. It also
 * works as simple factory for engine specific implementations of the API
 * interfaces. Note that this may change before version 1.0 will be published.
 * The return types for the {@link #recognize()} methods isn't also set in stone
 * yet since {@link java.util.Observable} isn't optimal.
 */
public interface OCREngine {

	/**
	 * Recognize. Starts the recognition of the {@link OCRProcess} hold in the
	 * internal queue. The returned {@link java.util.Observable} can be used to
	 * track the progress of the recognition process.
	 * 
	 * @param process
	 *            the process
	 * @return the observable
	 */
	public Observable recognize (OCRProcess process);

	/**
	 * Recognize the list of given OCRProcess. Throws an IllegalStateException
	 * if no process was added. Does nothing the recognizer is already working.
	 * The returned {@link java.util.Observable} can be used to track the
	 * progress of the recognition process.
	 * 
	 * @return the observer
	 * @see OCRProcess
	 */
	public Observable recognize ();

	/**
	 * Stops a running recognizer. Returns false if the recognizer isn't running
	 * or waits for a {@link OCRProcess} to finish.
	 * 
	 * @return true if the engine starts to shut down or already is shut down,
	 *         false otherwise.
	 */
	public Boolean stop ();

	/**
	 * Adds a OCR process. The returned {@link java.util.Observable} can be used
	 * to track the progress of the recognition process.
	 * 
	 * @param ocrp
	 *            the ocrp
	 * @return the observer
	 * @see OCRProcess
	 */

	public Observable addOcrProcess (OCRProcess ocrp);

	/**
	 * Adds OCR process.
	 * 
	 * @return the OCR process
	 * @see OCRProcess
	 */
	public List<OCRProcess> getOcrProcess ();

	/**
	 * New OCRImage. This method should return an engine specific implementation
	 * of {@link OCRImage}. Lazy implementers can choose to return an anonymous
	 * class that extends {@link AbstractOCRImage}, if it fits their needs.
	 * 
	 * @return the new created OCR image
	 * @see OCRImage
	 */
	public OCRImage newOCRImage ();

	/**
	 * New OCRProcess. This method should return an engine specific
	 * implementation of {@link OCRProcess}. Lazy implementers can choose to
	 * return an anonymous class that extends {@link AbstractOCRProcess}, if it
	 * fits their needs.
	 * 
	 * @return the new created OCR process
	 * @see OCRProcess
	 */
	public OCRProcess newOCRProcess ();

	/**
	 * New OCROutput. This method should return an engine specific
	 * implementation of {@link OCROutput}. Lazy implementers can choose to
	 * return an anonymous class that extends {@link AbstractOCROutput}, if it
	 * fits their needs.
	 * 
	 * @return the new created OCR output
	 * @see OCROutput
	 */
	public OCROutput newOCROutput ();

	/**
	 * Inits the OCREngine. This an be used to check if the engine is
	 * operational. Implementations should implement this method to check if an
	 * Engine is licensed or a server component can be reached. Note the the API
	 * doesn't prohibit the usage of an engine that failed to initialize. Use it
	 * on your own risk. If the engine was already initialized this also returns
	 * true.
	 * 
	 * @return true if the engine could be initialized, false otherwise
	 */
	public Boolean init ();

}
