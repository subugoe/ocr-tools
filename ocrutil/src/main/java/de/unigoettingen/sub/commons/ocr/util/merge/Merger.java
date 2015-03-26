package de.unigoettingen.sub.commons.ocr.util.merge;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public abstract class Merger {

	public void merge(List<InputStream> inputs, OutputStream output) throws IOException {
		BufferedOutputStream bufferedOutput = new BufferedOutputStream(output, 8*1024);
		try {
			mergeBuffered(inputs, bufferedOutput);
		} finally {
			bufferedOutput.flush();
			bufferedOutput.close();
		}
	}
	
	protected abstract void mergeBuffered(List<InputStream> inputs, OutputStream output);
	
}
