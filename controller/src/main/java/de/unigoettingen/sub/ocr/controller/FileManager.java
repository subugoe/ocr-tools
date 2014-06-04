package de.unigoettingen.sub.ocr.controller;

import java.io.File;

public class FileManager {

	public boolean isReadableFolder(String inputFolder) {
		File folder = new File(inputFolder);
		return folder.exists() && folder.isDirectory() && folder.canRead();
	}

	public boolean isWritableFolder(String outputFolder) {
		File folder = new File(outputFolder);
		return folder.exists() && folder.isDirectory() && folder.canWrite();
	}

}
