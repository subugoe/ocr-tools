package de.uni_goettingen.sub.commons.ocr.api;

import java.net.URL;


/**
 * The Class OCRImage.
 */
public class OCRImage {
	protected URL imageUrl = null;
	
	public OCRImage (URL imageUrl) {
		this.imageUrl = imageUrl;
	}
	  
	/** directory of the Pictures. */
	public URL url;
	
	  /** rotation of the image. */
	public Integer rotation;
	
	  /**
  	 * get an Url for a Picture.
  	 *
  	 * @return the url
  	 */  
	public URL getUrl() {
		return url;
	}
	
	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(URL url) {
		this.url = url;
	}
	
	/**
	 * Gets the rotation.
	 *
	 * @return the rotation
	 */
	public Integer getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the rotation.
	 *
	 * @param rotation the new rotation
	 */
	public void setRotation(Integer rotation) {
		this.rotation = rotation;
	}
	  
	  
}
