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
import java.util.HashMap;
import java.util.Map;

/**
 * The Class AbstractOCROutput is a abstract super class for {@link OCRImage}
 * implementations. To support different unterlying {@link OCREngine}
 * implementations parameters can set as a simple {@link Map}.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 * 
 */
public abstract class AbstractOCROutput implements OCROutput {

	/** The URI where the output file should be stored. */
	protected URI outputUri;
	
	/** The URI as String where the output file should be stored. */
	protected String outputDir;

	private OCRFormat format;
	
	/**
	 * Instantiates a new abstract ocr output.
	 */
	protected AbstractOCROutput() {

	}

	/**
	 * Instantiates a new abstract ocr output from a given {@link OCROutput}.
	 * This is a simple copy constructor that can be used by subclasses. It can
	 * be used to convert different subclasses into each other
	 * 
	 * @param ocrOutput
	 *            the ocr output
	 */
	public AbstractOCROutput(OCROutput ocrOutput) {
		this(ocrOutput.getUri(), ocrOutput.getlocalOutput(), ocrOutput.getFormat());
	}

	/**
	 * Instantiates a new abstract ocr output.
	 * 
	 * @param uri
	 *            the uri where the results should be stored.
	 * @param params
	 *            the params, set variants of the output like different versions of PDF.
	 * @param outputDir
	 * 			  The URI as String where the output file should be stored.           
	 */
	public AbstractOCROutput(URI uri, String outputDir, OCRFormat format) {
		this.outputUri = uri;
		this.outputDir = outputDir;
		this.format = format;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#getUri()
	 */
	public URI getUri () {
		return this.outputUri;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#setUri(java.net.URI)
	 */
	public void setUri (URI uri) {
		this.outputUri = uri;

	}
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#getlocalOutput()
	 */
	public String getlocalOutput (){
		return this.outputDir;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#setlocalOutput(java.lang.String)
	 */
	public void setlocalOutput (String outputDir){
		this.outputDir = outputDir;
	}
	
	public void setFormat(OCRFormat newFormat) {
		format = newFormat;
	}
	
	public OCRFormat getFormat() {
		return format;
	}

}
