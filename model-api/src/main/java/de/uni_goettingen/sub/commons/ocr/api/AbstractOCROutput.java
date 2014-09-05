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
 * The Class AbstractOCROutput is a abstract super class for {@link OCRImage}
 * implementations. To support different unterlying {@link OCREngine}
 * implementations parameters can set as a simple {@link Map}.
 * 
 * @author abergna
 * @author cmahnke
 * 
 */
public abstract class AbstractOCROutput implements OCROutput {

	protected URI localUri;
	
	private OCRFormat format;
	
	protected AbstractOCROutput() {

	}

	public AbstractOCROutput(OCROutput ocrOutput) {
		this(ocrOutput.getLocalUri(), ocrOutput.getFormat());
	}

	public AbstractOCROutput(URI uri, OCRFormat format) {
		this.localUri = uri;
		this.format = format;
	}

	@Override
	public URI getLocalUri() {
		return this.localUri;
	}

	@Override
	public void setLocalUri(URI uri) {
		this.localUri = uri;

	}
	
	@Override
	public void setFormat(OCRFormat newFormat) {
		format = newFormat;
	}
	
	@Override
	public OCRFormat getFormat() {
		return format;
	}

}
