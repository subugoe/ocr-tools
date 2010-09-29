package de.uni_goettingen.sub.commons.ocr.abbyy.server;


import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilter implements FileFilter {
	private String extension;
	
	public FileExtensionFilter (String extension) {
		this.extension = extension;
	}
	
	public String getExtension(String file) {
		return file.substring(file.lastIndexOf(".") + 1).toLowerCase();
	}

	
	public boolean accept (File pathname) {
		if (getExtension(pathname.getAbsolutePath()).equalsIgnoreCase(extension)) {
			return true;
		} else {
			return false;
		}
	}
}