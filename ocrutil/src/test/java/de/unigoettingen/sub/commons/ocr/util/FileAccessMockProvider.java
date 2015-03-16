package de.unigoettingen.sub.commons.ocr.util;

import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class FileAccessMockProvider {

	public static FileAccess mock;
	
	public static FileAccess getMock() {
		return mock;
	}
}
