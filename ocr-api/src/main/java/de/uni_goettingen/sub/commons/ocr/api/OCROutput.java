package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URL;
import java.util.Map;

/**
 * The Interface OCROutput.
 */
public interface OCROutput {

	/**
	 * Gets the url.
	 * 
	 */
	public URL getUrl ();

	/**
	 * Sets the url.
	 * 
	 */
	public void setUrl (URL url);

	/**
	 * Sets the params.
	 * 
	 * @param params
	 *            the params
	 */
	public void setParams (Map<String, String> params);

	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public Map<String, String> getParams ();

	/**
	 * Sets the format.
	 * 
	 * @param format
	 *            the new format
	 */
	public void setFormat (OCRFormat format);

	/**
	 * Gets the format.
	 * 
	 * @return the format
	 */
	public OCRFormat getFormat ();

}
