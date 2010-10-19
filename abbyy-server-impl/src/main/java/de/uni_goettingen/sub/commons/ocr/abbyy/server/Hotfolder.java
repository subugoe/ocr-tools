package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*

 © 2010, SUB Göttingen. All rights reserved.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

/**
 * The Class Hotfolder is used to control the hotfolders used by the Abbyy
 * Recognition Server.
 */
public class Hotfolder extends Thread {
	// The Constant logger.
	final static Logger logger = LoggerFactory.getLogger(Hotfolder.class);

	// The errror, input, output folder.
	protected URL inFolder, outFolder, errrorFolder;

	protected String serverURL, inputFolder, outputFolder, errorFolder;
	
	protected ConfigParser config;
	
	private static Hotfolder _instance; 

	// The fsmanager.
	protected FileSystemManager fsManager = null;

	/**
	 * Instantiates a new hotfolder.
	 * 
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public Hotfolder() {
		try {
			fsManager = VFS.getManager();
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OCRException(e);
		}
	}

	public Hotfolder(ConfigParser config) throws FileSystemException {
		this();
		this.config = config;
	}

	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 * 
	 * @param files
	 *            is a List of The Class AbbyyOCRImage. Is a representation of
	 *            an OCRImage suitable for holding references to remote files as
	 *            used by the Abbyy Recognition Server
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws FileSystemException
	 */
	public void copyFilesToServer (List<AbbyyOCRImage> files) throws InterruptedException, FileSystemException {
		// iterate over all Files and put them to Abbyy-server inputFolder:
		for (AbbyyOCRImage info : files) {
			FileObject remoteFile = fsManager.resolveFile(info.getRemoteURL().toString());
			FileObject imageUrlfile = fsManager.resolveFile(info.getUrl().toString());
			// Delete if exists
			deleteIfExists(info.getRemoteURL());
			if (info.toString().endsWith("/")) {
				logger.trace("Creating new directory " + info.getRemoteURL().toString() + "!");
				// Create the directory
				mkDir(info.getRemoteURL());

			} else {

				logger.trace("Copy from " + info.getUrl().toString() + " to " + info.getRemoteURL());
				remoteFile.copyFrom(imageUrlfile, new AllFileSelector());
			}
		}
	}

	/**
	 * Copy a files from remotefile to localfile. Assumes overwrite.
	 * 
	 * @param from
	 *            , the url of the file name as used on the remote system,
	 *            usally a relative file name and thus represented as a String
	 * @param to
	 *            , an URL representing the local file, it should be resolveable
	 *            from the local Server.
	 * @throws FileSystemException
	 *             the file system exception
	 */
	//TODO: Use URLs
	//TODO: This is dangerous, check if the file exists!
	public void copyFile (String from, String to) throws FileSystemException {
		FileObject remoteFile = fsManager.resolveFile(from);
		FileObject localFile = fsManager.resolveFile(to);
		localFile.copyFrom(remoteFile, new AllFileSelector());
	}

	/**
	 * Delete a resource at the specified url
	 * 
	 * @param url
	 *            the url
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public void delete (URL url) throws FileSystemException {
		fsManager.resolveFile(url.toString()).delete();
	}

	/**
	 * Delete a resource at the specified url if exists.
	 * 
	 * @param url
	 *            the url
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public void deleteIfExists (URL url) throws FileSystemException {
		if (fsManager.resolveFile(url.toString()).delete()) {
			logger.trace(url.toString() + " exists already, but now deleted");
		}
	}

	/**
	 * to create a directory at the specified url
	 * 
	 * @param url
	 *            the url
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public void mkDir (URL url) throws FileSystemException {
		fsManager.resolveFile(url.toString()).createFolder();
		logger.debug("Directory " + url.toString() + " created");
	}

	/**
	 * check if a resource at the specified url.
	 * 
	 * @param url
	 *            the url
	 * @return true, if successful
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public Boolean exists (URL url) throws FileSystemException {
		if (fsManager.resolveFile(url.toString()).exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the total size for a url.
	 * 
	 * @param url
	 *            the url
	 * @return the total size
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public Long getTotalSize (URL url) throws FileSystemException {
		FileObject urlFile = fsManager.resolveFile(url.toString());

		Long size = 0l;
		if (urlFile.getType() == FileType.FOLDER) {
			FileObject[] children = urlFile.getChildren();
			for (int j = 0; j < children.length; j++) {
				if (children[j].getType() == FileType.FOLDER) {
					size += getTotalSize(children[j].getURL());
				} else {
					FileContent contentFile = children[j].getContent();
					size += contentFile.getSize();
				}
			}
			return size;
		} else {
			FileContent content = urlFile.getContent();
			return content.getSize();
		}
	}

	public Long getTotalCount (URL url) throws FileSystemException {
		FileObject urlFile = fsManager.resolveFile(url.toString());
		Long count = 0l;
		if (urlFile.getType() == FileType.FOLDER) {
			FileObject[] children = urlFile.getChildren();
			for (int j = 0; j < children.length; j++) {
				if (children[j].getType() == FileType.FOLDER) {
					count += getTotalCount(children[j].getURL());
				} else {
					count += 1l;
				}
			}
			return count;
		} else {
			return 1l;
		}
	}

	/**
	 * Gets the url list.
	 * 
	 * @param imageDirectory
	 *            the image directory
	 * @return the url list
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws MalformedURLException
	 *             the malformed url exception
	 */
	List<AbbyyOCRImage> getUrlList (String imageDirectory) throws FileSystemException, MalformedURLException {
		List<AbbyyOCRImage> imageList = new ArrayList<AbbyyOCRImage>();
		FileObject getUrlImage = fsManager.resolveFile(imageDirectory);
		FileObject[] children = getUrlImage.getChildren();
		for (int i = 0; i < children.length; i++) {
			imageList.add(new AbbyyOCRImage(new URL(children[i].getName().toString())));
		}
		return imageList;
	}

	/**
	 * Gets the in folder.
	 * 
	 * @return the in folder
	 */
	public URL getInFolder () {
		return inFolder;
	}

	/**
	 * Sets the in folder.
	 * 
	 * @param inFolder
	 *            the new in folder
	 */
	public void setInFolder (URL inFolder) {
		this.inFolder = inFolder;
	}

	/**
	 * Gets the out folder.
	 * 
	 * @return the out folder
	 */
	public URL getOutFolder () {
		return outFolder;
	}

	/**
	 * Sets the out folder.
	 * 
	 * @param outFolder
	 *            the new out folder
	 */
	public void setOutFolder (URL outFolder) {
		this.outFolder = outFolder;
	}

	/**
	 * Gets the errror folder.
	 * 
	 * @return the errror folder
	 */
	public URL getErrrorFolder () {
		return errrorFolder;
	}

	/**
	 * Sets the errror folder.
	 * 
	 * @param errrorFolder
	 *            the new errror folder
	 */
	public void setErrrorFolder (URL errrorFolder) {
		this.errrorFolder = errrorFolder;
	}

	public String getServerURL () {
		return serverURL;
	}

	public void setServerURL (String serverURL) {
		this.serverURL = serverURL;
	}

	public String getInputFolder () {
		return inputFolder;
	}

	public void setInputFolder (String inputFolder) {
		this.inputFolder = inputFolder;
	}

	public String getOutputFolder () {
		return outputFolder;
	}

	public void setOutputFolder (String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getErrorFolder () {
		return errorFolder;
	}

	public void setErrorFolder (String errorFolder) {
		this.errorFolder = errorFolder;
	}

	public static Hotfolder newInstace () {
		if (_instance == null) {
			_instance = new Hotfolder();
		}
		return _instance;
	}

}
