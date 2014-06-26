package de.unigoettingen.sub.commons.ocrComponents.cli.testutil;

import de.unigoettingen.sub.commons.ocr.util.FileManager;

public class FileManagerMockProvider extends FileManager {

	public static FileManager mock;
	
	public static FileManager getMock() {
		return mock;
	}
}
