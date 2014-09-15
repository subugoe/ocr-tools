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

public abstract class AbstractProcess implements OcrProcess,Serializable {

	private static final long serialVersionUID = 3302775196071887966L;
	private final static Logger logger = LoggerFactory.getLogger(AbstractProcess.class);
	protected String name;

	transient protected List<OcrImage> ocrImages = new ArrayList<OcrImage>();

	protected Set<Locale> langs = new HashSet<Locale>();
	protected OcrQuality quality = OcrQuality.BALANCED;
	protected OcrTextType textType = OcrTextType.NORMAL;
	protected OcrPriority priority;
	transient protected List<OcrOutput> ocrOutputs = new ArrayList<OcrOutput>();
	protected File outputDir;
	
	@Override
	public void addLanguage (Locale locale) {
		langs.add(locale);
	}

	@Override
	public Set<Locale> getLanguages () {
		return langs;
	}

	public List<OcrImage> getImages () {
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
	public List<OcrOutput> getOcrOutputs() {
		return this.ocrOutputs;
	}

	@Override
	public OcrQuality getQuality () {
		return quality;
	}

	@Override
	public void setQuality (OcrQuality quality) {
		this.quality = quality;
	}

	@Override
	public OcrTextType getTextType () {
		return this.textType;
	}
	
	@Override
	public void setTextType (OcrTextType t) {
		this.textType = t ;
	}
	
	@Override
	public OcrPriority getPriority() {
		return this.priority;
	}

	@Override
	public void setPriority(OcrPriority p) {
		this.priority = p;
	}

	public Set<OcrFormat> getAllOutputFormats() {
		Set<OcrFormat> formats = new HashSet<OcrFormat>();
		for (OcrOutput output : ocrOutputs) {
			formats.add(output.getFormat());
		}
		return formats;
	}
	
	public URI getOutputUriForFormat(OcrFormat format) {
		for (OcrOutput output : ocrOutputs) {
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
	
	protected URI constructLocalUri(OcrFormat format) {
		String fileName = name + "." + format;
		return new File(outputDir, fileName).toURI();
	}
	
}
