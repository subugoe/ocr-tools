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
import java.util.Locale;
import java.util.Set;



//TODO: Look at http://sites.google.com/site/openjdklocale/Home for language and script representation.
// Or use http://icu-project.org/apiref/icu4j/com/ibm/icu/lang/UScript.html for scripts
public interface OcrProcess {

	/**
	 * Gets the languages set for this process as List. These languages will be
	 * used for recognition. Not all engines are able to recognize each
	 * language. They will just ignore this setting.
	 * 
	 */
	public Set<Locale> getLanguages();

	public void addLanguage(Locale lang);
		
	public void addImage(URI localUri);

	public int getNumberOfImages();
	
	public void setOutputDir(File outputDir);

	public void addOutput(OcrFormat format);
	
	public void setName(String name);

	public String getName();
	
	public OcrQuality getQuality();

	public void setQuality(OcrQuality q);


	public OcrTextType getTextType();

	public void setTextType(OcrTextType t);

	public OcrPriority getPriority();

	public void setPriority(OcrPriority p);
	
}
