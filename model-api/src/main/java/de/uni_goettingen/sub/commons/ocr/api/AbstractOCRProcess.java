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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The Class AbstractOCRProcess is a abstract super class for {@link OCRProcess}
 * implementations. It also adds a few static utility methods for easier
 * creation of processes.
 * 
 */
public abstract class AbstractOCRProcess implements OCRProcess,Serializable {

	private static final long serialVersionUID = 3302775196071887966L;

	private final static Logger logger = LoggerFactory.getLogger(AbstractOCRProcess.class);
	/** The name of this process, this is needed for serialization of a process */
	protected String name;

	/** The Images which should be converted */
	transient protected List<OCRImage> ocrImages = new ArrayList<OCRImage>();

	/** The languages which should be recognized */
	protected Set<Locale> langs = new HashSet<Locale>();

	/** The quality that the implementing process should create, default is FAST */
	protected OCRQuality quality = OCRQuality.FAST;

	/** the texttyp. to describe the type of recognized text
	 * Typ of recognized text: Normal, Typewriter, Matrix, OCR_A, 
	 * OCR_B, MICR_E13B, Gothic.
	 *  */
	protected OCRTextType textType = OCRTextType.NORMAL;
	
	/**the priority level of the job. The default priority is Normal.
	 * priority level : Low, BelowNormal, Normal, AboveNormal, High ;
	 * */
	protected OCRPriority priority;
	/**
	 * The images that should be converted, are stored in the given format at
	 * the given location
	 */
	transient protected List<OCROutput> ocrOutputs = new ArrayList<OCROutput>();

	protected Boolean isFinished = false;

	/**
	 * Add a new language.
	 * 
	 * @param locale
	 *            the {@link Locale} representing the language to be added
	 */
	@Override
	public void addLanguage (Locale locale) {
		langs.add(locale);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getLanguages()
	 */
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
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getName()
	 */
	@Override
	public String getName () {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setName(java.lang.String)
	 */
	@Override
	public void setName (String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getOcrOutput()
	 */
	public List<OCROutput> getOcrOutputs () {
		return this.ocrOutputs;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#isFinished()
	 */
	public Boolean isFinished () {
		return isFinished;
	}


	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getQuality()
	 */
	public OCRQuality getQuality () {
		return quality;
	}

	

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setQuality(de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRQuality)
	 */
	public void setQuality (OCRQuality quality) {
		this.quality = quality;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getTextTyp()
	 */
	@Override
	public OCRTextType getTextType () {
		return this.textType;
	}
	

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setTextTyp(de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp)
	 */
	@Override
	public void setTextType (OCRTextType t) {
		this.textType = t ;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getPriority()
	 */
	@Override
	public OCRPriority getPriority() {
		return this.priority;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setPriority(de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority)
	 */
	@Override
	public void setPriority(OCRPriority p) {
		this.priority = p;
	}

	@Override
	public void addOutput (OCROutput output) {
		ocrOutputs.add(output);
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
				return output.getUri();
			}
		}
		return null;
	}
	
}
