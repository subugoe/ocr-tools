package de.unigoettingen.sub.commons.ocr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

	public Properties getFileProperties(String file) {
		Properties props = new Properties();
		try {
			File f = new File(getClass().getResource("/" + file).getFile());
			props.load(inputStreamFromFile(f));
		} catch (IOException e) {
			// TODO: logger
			System.err.println("Could not load file:" + file);
		}
		return props;
	}
	
	InputStream inputStreamFromFile(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO: logger
			System.err.println("Could not load file:" + file);
		}
		return is;
	}

}
