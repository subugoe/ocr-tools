package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOutput;

/**
 * Representation of a result output.
 * 
 * @author dennis
 *
 */
public class OcrsdkOutput extends AbstractOutput {
	
	@Override
	public void setLocalUri(URI outputUri) {
		if (!"file".equals(outputUri.getScheme())) {
			throw new IllegalArgumentException("Only local outputs are supported. URI is: " + outputUri);
		}
		this.localUri = outputUri;
	}
	
	/**
	 * Saves the input to the internal URI.
	 * 
	 * @param streamToSave Input that will be saved to the URI
	 */
	public void save(InputStream streamToSave) {
		File outFile = new File(localUri);
		try {
			OutputStream fos = new FileOutputStream(outFile);
			IOUtils.copy(streamToSave, fos);
		} catch (IOException e) {
			throw new IllegalStateException("Could not save to " + localUri);
		}
	}

}
