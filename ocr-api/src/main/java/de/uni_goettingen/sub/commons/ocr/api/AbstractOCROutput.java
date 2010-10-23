package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractOCROutput implements OCROutput {	
	protected URI outputUri;
	
	protected Map<String, String> params = new HashMap<String, String>();
	
	protected AbstractOCROutput () {
		
	}
	
	public AbstractOCROutput (OCROutput ocrOutput) {
		this(ocrOutput.getUri(), ocrOutput.getParams());
	}
	
	public AbstractOCROutput(URI uri, Map<String, String> params) {
		this.outputUri = uri;
		this.params = params;
	}

	public Map<String, String> getParams () {
		return this.params;
	}

	public URI getUri () {
		return this.outputUri;
	}

	public void setParams (Map<String, String> params) {
		this.params = params;
		
	}

	public void setUri (URI uri) {
		this.outputUri = uri;
		
	}

}
