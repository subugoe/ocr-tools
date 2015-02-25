package de.unigoettingen.sub.commons.ocr.util.merge;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface Merger {

	public void merge(List<InputStream> inputs, OutputStream output);
	
}
