package de.unigoettingen.sub.commons.ocr.util;

/*

Copyright 2010 SUB Goettingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.unigoettingen.sub.commons.util.file.FileExtensionsFilter;

/**
 * The Class OCRUtil is a container for static methods that can ease OCR related development.
 *
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public class OCRUtil {
	/**
	 * Parses a String for languages. Languages are represented by {@link java.util.Locale}
	 * 
	 * @param str
	 *            the str
	 * @return the list of language
	 */
	public static List<Locale> parseLangs (String str) {
		List<Locale> langs = new ArrayList<Locale>();
		//TODO: Test this, remove the if
		if (str.contains(",")) {
			for (String lang : Arrays.asList(str.split(","))) {
				langs.add(new Locale(lang));
			}
		} else {
			langs.add(new Locale(str));
		}
		return langs;
	}
	

	/**
	 * Gets the target directories for a given directory. It checks recursively
	 * if any subfolder contains directories containing one of the given extensions. To use
	 * this method for OCR purposes you consider multiple extensions for one file type.
	 * For example consider a list of "tif", "tiff" and "TIF".
	 * 
	 * @param dir the directory to search in
	 * @param extension the file extensions
	 * @return the target directories
	 */
	
	public static List<File> getTargetDirectories (File dir, List<String> extensions) {
		List<File> dirs = new ArrayList<File>();
		if (makeFileList(dir, extensions).size() > 0) {
			dirs.add(dir);
		}
		List<File> fileList;
		if (dir.isDirectory()) {
			fileList = Arrays.asList(dir.listFiles());
			for (File file : fileList) {
				if (file.isDirectory()) {
					List<File> files = makeFileList(dir, extensions);
					if (files.size() > 0) {
						dirs.addAll(files);
					} else {
						dirs.addAll(getTargetDirectories(file, extensions));
					}
				}
			}
		} else {
			throw new IllegalStateException(dir.getAbsolutePath() + " is not a directory");
		}
		return dirs;
	}
	
	/**
	 * Gets the target directories for a given directory. It checks recursively
	 * if any subfolder contains directories containing the given extension. To use
	 * this method for OCR purposes you should set an extension like "tif" or "jpg"
	 * 
	 * @param dir the directory to search in
	 * @param extension the file extension
	 * @return the target directories containing files ending with the given extension
	 */
	public static List<File> getTargetDirectories (File dir, String extension) {
		return getTargetDirectories(dir, Arrays.asList(new String[] {extension}));
	}

	/**
	 * makeFileList is a simple static method to create a list of files ending
	 * with the given extension.
	 * 
	 * @param inputFile
	 *            the directory that is searched for files
	 * @param filter the file extension to look for
	 * @return the list of all files have "filter" as ending
	 */
	public static List<File> makeFileList (File dir, String extension) {
		return makeFileList (dir, Arrays.asList(new String[] {extension}));
	}
	
	/**
	 * makeFileList is a simple static method to create a list of files ending
	 * with the given extensions.
	 * 
	 * @param dir
	 *            the directory that is searched for files
	 * @param extensions the extensions as list
	 * @return the list of all files have one of the given filers as extension
	 */
	public static List<File> makeFileList (File dir, List<String> extensions) {
		List<File> fileList;
		if (dir.isDirectory()) {
			fileList = Arrays.asList(dir.listFiles(new FileExtensionsFilter(extensions)));
			Collections.sort(fileList);
		} else {
			fileList = new ArrayList<File>();
			fileList.add(dir);
		}
		return fileList;
	}
	
}
