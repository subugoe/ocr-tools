package de.unigoettingen.sub.commons.ocrComponents.cli.testutil;

import de.unigoettingen.sub.ocr.controller.FileManager;

public class FileManagerMockProvider extends FileManager {

	public static FileManager mock;
	
	public static FileManager getMock() {
		return mock;
	}
}
