package de.uni_goettingen.sub.commons.ocr.api;

/*

Copyright 2010 SUB Goettingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOCRProcess implements OCRProcess,Serializable {

	private static final long serialVersionUID = 3302775196071887966L;
	private final static Logger logger = LoggerFactory.getLogger(AbstractOCRProcess.class);
	protected String name;

	transient protected List<OCRImage> ocrImages = new ArrayList<OCRImage>();

	protected Set<Locale> langs = new HashSet<Locale>();
	protected OCRQuality quality = OCRQuality.FAST;
	protected OCRTextType textType = OCRTextType.NORMAL;
	protected OCRPriority priority;
	transient protected List<OCROutput> ocrOutputs = new ArrayList<OCROutput>();
	protected File outputDir;
	
	@Override
	public void addLanguage (Locale locale) {
		langs.add(locale);
	}

	@Override
	public Set<Locale> getLanguages () {
		return langs;
	}

	public List<OCRImage> getImages () {
		return ocrImages;
	}
	
	@Override
	public int getNumberOfImages() {
		return ocrImages.size();
	}
	
	public boolean canBeStarted() {
		if (ocrOutputs == null || ocrOutputs.isEmpty()) {
			logger.warn("The OCR process has no outputs: " + name);
			return false;
		}

		if (ocrImages == null || ocrImages.isEmpty()) {
			logger.warn("The OCR process has no input images: " + name);
			return false;
		}
		return true;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<OCROutput> getOcrOutputs() {
		return this.ocrOutputs;
	}

	@Override
	public OCRQuality getQuality () {
		return quality;
	}

	@Override
	public void setQuality (OCRQuality quality) {
		this.quality = quality;
	}

	@Override
	public OCRTextType getTextType () {
		return this.textType;
	}
	
	@Override
	public void setTextType (OCRTextType t) {
		this.textType = t ;
	}
	
	@Override
	public OCRPriority getPriority() {
		return this.priority;
	}

	@Override
	public void setPriority(OCRPriority p) {
		this.priority = p;
	}

	public Set<OCRFormat> getAllOutputFormats() {
		Set<OCRFormat> formats = new HashSet<OCRFormat>();
		for (OCROutput output : ocrOutputs) {
			formats.add(output.getFormat());
		}
		return formats;
	}
	
	public URI getOutputUriForFormat(OCRFormat format) {
		for (OCROutput output : ocrOutputs) {
			if (output.getFormat().equals(format)) {
				return output.getLocalUri();
			}
		}
		return null;
	}
	
	@Override
	public void setOutputDir(File newDir) {
		outputDir = newDir;
	}
	
	protected URI constructLocalUri(OCRFormat format) {
		String fileName = name + "." + format.toString().toLowerCase();
		return new File(outputDir, fileName).toURI();
	}
	
}
