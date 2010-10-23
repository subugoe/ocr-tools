package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URI;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface OCROutput.
 */
public interface OCROutput {

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public URI getUri ();

	/**
	 * Sets the url.
	 *
	 * @param uri the new uri
	 */
	public void setUri (URI uri);

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
	//public void setFormat (OCRFormat format);

	/**
	 * Gets the format.
	 * 
	 * @return the format
	 */
	//public OCRFormat getFormat ();
	
}
