package de.unigoettingen.sub.ocr.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

	public boolean isReadableFolder(String inputFolder) {
		File folder = new File(inputFolder);
		return folder.exists() && folder.isDirectory() && folder.canRead();
	}

	public boolean isWritableFolder(String outputFolder) {
		File folder = new File(outputFolder);
		return folder.exists() && folder.isDirectory() && folder.canWrite();
	}

	public File[] getAllFolders(String parentFolder, String[] imageTypes) {
		File parent = new File(parentFolder);
		List<File> imagesFromFolder = getImagesFromFolder(parent, imageTypes);
		if (!imagesFromFolder.isEmpty()) {
			return new File[]{parent};
		} else {
			List<File> folders = new ArrayList<File>();
			File[] files = parent.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					folders.add(file);
				}
			}
			return folders.toArray(new File[]{});
		}

	}

	private List<File> getImagesFromFolder(File folder, String[] imageTypes) {
		List<File> images = new ArrayList<File>();
		File[] files = folder.listFiles();
		for (File file : files) {
			for (String format : imageTypes) {
				if (hasFormat(file, format)) {
					images.add(file);
				}
			}
		}
		return images;
	}

	private boolean hasFormat(File file, String format) {
		return file.toString().toLowerCase().endsWith("." + format);
	}
	
	public File[] getAllImagesFromFolder(File folder, String[] imageTypes) {
		List<File> images = getImagesFromFolder(folder, imageTypes);
		return images.toArray(new File[]{});
	}

}
