package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class AbstractOCROutput is a abstract super class for {@link OCRImage}
 * implementations. To support different unterlying {@link OCREngine}
 * implementations parameters can set as a simple {@link Map}.
 * 
 * @version 0.9
 * @author cmahnke
 * 
 */
public abstract class AbstractOCROutput implements OCROutput {

	/** The URI where the output file should be stored. */
	protected URI outputUri;

	/** The params that should be used to generate this output representation. */
	protected Map<String, String> params = new HashMap<String, String>();

	//State variables
	/**
	 * This indicates that the results <u>should</u> exist, the API doesn't
	 * guarantee this to be true.
	 */
	protected Boolean isResult = false;

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
		this(ocrOutput.getUri(), ocrOutput.getParams());
	}

	/**
	 * Instantiates a new abstract ocr output.
	 * 
	 * @param uri
	 *            the uri where the results should be stored.
	 * @param params
	 *            the params, set variants of the output like different versions of PDF.
	 */
	public AbstractOCROutput(URI uri, Map<String, String> params) {
		this.outputUri = uri;
		this.params = params;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#getParams()
	 */
	public Map<String, String> getParams () {
		return this.params;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#getUri()
	 */
	public URI getUri () {
		return this.outputUri;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#setParams(java.util.Map)
	 */
	public void setParams (Map<String, String> params) {
		this.params = params;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#setUri(java.net.URI)
	 */
	public void setUri (URI uri) {
		this.outputUri = uri;

	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCROutput#isResult()
	 */
	public Boolean isResult () {
		return isResult;
	}

}
