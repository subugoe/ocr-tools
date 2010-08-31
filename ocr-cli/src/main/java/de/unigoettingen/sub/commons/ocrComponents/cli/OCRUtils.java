package de.unigoettingen.sub.commons.ocrComponents.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;

public class OCRUtils {

	public static List<File> makeFileList(File inputFile, String filter) {
		List<File> fileList;
		if (inputFile.isDirectory()) {
			
			
			File files[] = inputFile.listFiles(new FileExtensionsFilter(filter));
			fileList = Arrays.asList(files);
			Collections.sort(fileList);
			
		} else {
			fileList = new ArrayList<File>();
			fileList.add(inputFile);
		}
		return fileList;
	}

}
