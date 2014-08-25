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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The Class AbstractOCRProcess is a abstract super class for {@link OCRProcess}
 * implementations. It also adds a few static utility methods for easier
 * creation of processes.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public abstract class AbstractOCRProcess extends Observable implements OCRProcess,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3302775196071887966L;

	final static Logger logger = LoggerFactory.getLogger(AbstractOCRProcess.class);
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
	transient protected Map<OCRFormat, OCROutput> ocrOutputs = new LinkedHashMap<OCRFormat, OCROutput>();

	protected Boolean isFinished = false;
	/** for Subdivision the process*/
    protected Boolean segmentation = false;
    	
	protected Long time;

	/**
	 * Add a new language.
	 * 
	 * @param locale
	 *            the {@link Locale} representing the language to be added
	 */
	public void addLanguage (Locale locale) {
		langs.add(locale);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getLanguages()
	 */
	public Set<Locale> getLanguages () {
		return langs;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getOcrImages()
	 */
	public List<OCRImage> getImages () {
		return ocrImages;
	}

	@Override
	public void addImage(OCRImage image) {
		ocrImages.add(image);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setOcrImages(java.util.List)
	 */
	public void setOcrImages (List<OCRImage> ocrImages) {
		this.ocrImages = ocrImages;
	}

	@Override
	public int getNumberOfImages() {
		return ocrImages.size();
	}
	
	public boolean canBeStarted() {
		if (ocrOutputs == null || ocrOutputs.isEmpty()) {
			logger.warn("The OCR process has no outputs: " + getName());
			return false;
		}

		if (ocrImages == null || ocrImages.isEmpty()) {
			logger.warn("The OCR process has no input images: " + getName());
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getName()
	 */
	public String getName () {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setName(java.lang.String)
	 */
	public void setName (String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getOcrOutput()
	 */
	public Map<OCRFormat, OCROutput> getOcrOutputs () {
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
	public OCRTextType getTextType () {
		return this.textType;
	}
	

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setTextTyp(de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp)
	 */
	public void setTextType (OCRTextType t) {
		this.textType = t ;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getPriority()
	 */
	public OCRPriority getPriority() {
		return this.priority;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setPriority(de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority)
	 */
	public void setPriority(OCRPriority p) {
		this.priority = p;
	}
	/**
	 * Adds the output for the given format, this might be a helpful utility
	 * method when working with Lists
	 * 
	 * @param format
	 *            the format to add
	 * @param output
	 *            the output, the output settings for the given format
	 * @see OCRFormat
	 * @see OCROutput
	 * @see #setOcrOutputs(Map);
	 */
	public void addOutput (OCRFormat format, OCROutput output) {
		if (ocrOutputs == null) {
			// We use a LinkedHashMap to get the order of the elements
			// predictable
			ocrOutputs = new LinkedHashMap<OCRFormat, OCROutput>();
		}
		ocrOutputs.put(format, output);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getTime()
	 */
	public Long getTime() {
		return time;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setTime(java.lang.Long)
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getSegmentation()
	 */
	public Boolean getSegmentation() {
		return segmentation;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setSegmentation(java.lang.Boolean)
	 */
	public void setSegmentation(Boolean segmentaion) {
		this.segmentation = segmentaion;
	}
	
}
