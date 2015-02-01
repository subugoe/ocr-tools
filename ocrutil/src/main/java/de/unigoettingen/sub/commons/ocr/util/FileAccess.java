package de.unigoettingen.sub.commons.ocr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class FileAccess {

	public boolean isReadableFolder(String inputFolder) {
		if (isEmpty(inputFolder)) {
			return false;
		}
		File folder = new File(inputFolder);
		return folder.exists() && folder.isDirectory() && folder.canRead();
	}

	public boolean isWritableFolder(String outputFolder) {
		if (isEmpty(outputFolder)) {
			return false;
		}
		File folder = new File(outputFolder);
		return folder.exists() && folder.isDirectory() && folder.canWrite();
	}

	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
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
		Arrays.sort(files);
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

	public Properties getPropertiesFromFile(String file) {
		Properties props = new Properties();
		try {
			File f = new File(getClass().getResource("/" + file).getFile());
			props.load(inputStreamForFile(f));
		} catch (IOException e) {
			// TODO: logger or throw
			System.err.println("Could not load file:" + file);
		}
		return props;
	}
	
	InputStream inputStreamForFile(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}
	
	public OutputStream outputStreamForFile(File file) throws FileNotFoundException {
		return new FileOutputStream(file);
	}

	public void copyUrlToFile(String urlString, File file) throws IOException {
		URL inputUrl = new URL(urlString);
		FileUtils.copyURLToFile(inputUrl, file);
	}
	
	public void copyStreamToFile(InputStream sourceStream, File targetFile) throws IOException {
		FileUtils.copyInputStreamToFile(sourceStream, targetFile);
	}

	public void deleteFile(File file) throws IOException {
		boolean success = file.delete();
		if (!success) {
			throw new IOException("Could not delete file: " + file.getAbsolutePath());
		}
	}

	public void deleteDir(File dir) throws IOException {
		FileUtils.deleteDirectory(dir);
	}

	public boolean fileExists(File file) {
		return file.exists();
	}

	public void copyFile(File source, File target) throws IOException {
		FileUtils.copyFile(source, target);
	}

	public String readFileToString(File file) throws IOException {
		return FileUtils.readFileToString(file);
	}

	public File createTempFile(String name) throws IOException {
		return File.createTempFile(name, null);
	}

}
