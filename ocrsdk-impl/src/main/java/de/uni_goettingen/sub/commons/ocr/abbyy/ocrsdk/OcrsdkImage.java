package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;

/**
 * Representation of an image that is to be recognized by Abbyy OCRSDK service.
 * 
 * @author dennis
 *
 */
public class OcrsdkImage extends AbstractOCRImage {

	public OcrsdkImage(URI imageUri) {
		super(imageUri);
		if (!"file".equals(imageUri.getScheme())) {
			throw new IllegalArgumentException("Only local files are supported");
		}
	}
	
	/**
	 * Reads the image from the internal URI.
	 * 
	 * @return Binary representation of the image
	 */
	public byte[] getAsBytes() {
		byte[] imageBytes = null;
		try {
			imageBytes = IOUtils.toByteArray(imageUri);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read from " + imageUri, e);
		}
		return imageBytes;
	}
}
