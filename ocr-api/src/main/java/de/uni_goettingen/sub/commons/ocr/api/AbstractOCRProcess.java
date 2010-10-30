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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

/**
 * The Class AbstractOCRProcess is a abstract super class for {@link OCRProcess}
 * implementations. It also adds a few static utility methods for easier
 * creation of processes.
 * 
 * @version 0.9
 * @author cmahnke
 */
public abstract class AbstractOCRProcess extends Observable implements OCRProcess {

	/** The name of this process, this is needed for serialization of a process */
	protected String name;

	/** The Images which should be converted */
	protected List<OCRImage> ocrImages = new ArrayList<OCRImage>();

	/** The languages which should be recognized */
	protected Set<Locale> langs = new HashSet<Locale>();

	/** The quality that the implementing process should create, default is FAST */
	protected OCRQuality quality = OCRQuality.FAST;

	/**
	 * The images that should be converted, are stored in the given format at
	 * the given location
	 */
	protected Map<OCRFormat, OCROutput> ocrOutputs;

	/** The params that should be used adjust the recognition process. */
	protected Map<String, String> params = new HashMap<String, String>();

	//State variables
	/** This indicates that process has failed. */
	protected Boolean isFinished = false;

	/**
	 * Instantiates a new abstract OCR process.
	 */
	protected AbstractOCRProcess() {
	}

	/**
	 * Instantiates a new abstract OCR process using a copy constructor.
	 * 
	 * @param process
	 *            the process
	 */
	public AbstractOCRProcess(OCRProcess process) {
		//Copy Constructor
		this(process.getOcrImages(), new HashSet<Locale>(process.getLanguages()), new HashMap<OCRFormat, OCROutput> (process.getOcrOutputs()));
	}

	/**
	 * Instantiates a new abstract OCR process. This constructor will be used by
	 * the copy constructor.
	 * 
	 * @param ocrImages
	 *            a {@link List} of {@link OCRImage}
	 * @param langs
	 *            a {@link Set} of {@link Locale} repreenting the languages that
	 *            should be recognized
	 * @param output
	 *            the output
	 */
	public AbstractOCRProcess(List<OCRImage> ocrImages, Set<Locale> langs, Map<OCRFormat, OCROutput> output) {
		this.ocrImages = ocrImages;
		this.langs = langs;
		this.ocrOutputs = output;
	}

	/**
	 * Add a new language.
	 * 
	 * @param locale
	 *            the {@link Locale} representing the language to be added
	 */
	public void addLanguage (Locale locale) {
		langs.add(locale);
	}

	/**
	 * remove language from the list.
	 * 
	 * @param locale
	 *            the {@link Locale} representing the language to be removed
	 */
	public void removeLanguage (Locale locale) {
		langs.remove(locale);
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getLanguages()
	 */
	public Set<Locale> getLanguages () {
		return langs;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setLanguages(java.util.Set)
	 */
	public void setLanguages (Set<Locale> langs) {
		this.langs = langs;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getOcrImages()
	 */
	public List<OCRImage> getOcrImages () {
		return ocrImages;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setOcrImages(java.util.List)
	 */
	public void setOcrImages (List<OCRImage> ocrImages) {
		this.ocrImages = ocrImages;
	}

	/**
	 * Adds a {@link OCRImage} to the internal List
	 * 
	 * @param ocrImage
	 *            the ocr image
	 */
	public void addImage (OCRImage ocrImage) {
		this.ocrImages.add(ocrImage);
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
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setOcrOutput(java.util.Map)
	 */
	public void setOcrOutputs (Map<OCRFormat, OCROutput> ocrOutputs) {
		this.ocrOutputs = ocrOutputs;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getOcrOutput()
	 */
	public Map<OCRFormat, OCROutput> getOcrOutputs () {
		return this.ocrOutputs;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getParams()
	 */
	public Map<String, String> getParams () {
		return this.params;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#setParams(java.util.Map)
	 */
	public void setParams (Map<String, String> params) {
		this.params = params;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#isFinished()
	 */
	public Boolean isFinished () {
		return isFinished();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcess#getOcrProcessMetadata()
	 */
	public OCRProcessMetadata getOcrProcessMetadata () {
		throw new UnsupportedOperationException();
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
	 * @see setOcrOutputs(Map);
	 */
	public void addOutput (OCRFormat format, OCROutput output) {
		ocrOutputs.put(format, output);
	}
}
