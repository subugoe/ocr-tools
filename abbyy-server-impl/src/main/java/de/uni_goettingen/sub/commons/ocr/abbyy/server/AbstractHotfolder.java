package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.IOException;
import java.net.URI;

public abstract class AbstractHotfolder implements Hotfolder {

	@Override
	public void deleteIfExists (URI uri) throws IOException {
		if (exists(uri)) {
			delete(uri);
		}
	}
	
	@Override
	public Long getTotalSize (URI uri) throws IOException {
		if (!isDirectory(uri)) {
			return getSize(uri);
		}
		Long size = 0l;
		for (URI u : listURIs(uri)) {
			if (isDirectory(uri)) {
				size += getTotalSize(u);
			} else {
				size += getSize(uri);
			}
		}
		return size;
	}

	@Override
	public Long getTotalCount (URI uri) throws IOException {
		if (!isDirectory(uri)) {
			return 1l;
		}
		Long count = 0l;
		for (URI u : listURIs(uri)) {
			if (isDirectory(uri)) {
				count += getTotalCount(u);
			} else {
				count += 1l;
			}
		}
		return count;
	}
	
}
