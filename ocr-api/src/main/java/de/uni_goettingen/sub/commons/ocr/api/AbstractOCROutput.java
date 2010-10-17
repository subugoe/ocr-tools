package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractOCROutput implements OCROutput {
	protected Map<String, String> params = new HashMap<String, String>();
	
	protected URL outputUrl;
	
	public AbstractOCROutput () {
		
	}
	
	public AbstractOCROutput (OCROutput ocrOutput) {
		this.outputUrl = ocrOutput.getUrl();
		this.params = ocrOutput.getParams();
	}

	public Map<String, String> getParams () {
		return this.params;
	}

	public URL getUrl () {
		return this.outputUrl;
	}

	public void setParams (Map<String, String> params) {
		this.params = params;
		
	}

	public void setUrl (URL url) {
		this.outputUrl = url;
		
	}

}
