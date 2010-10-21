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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
//TODO: Make this a real singleton
public class Hotfolder extends Thread {
	// The Constant logger.
	final static Logger logger = LoggerFactory.getLogger(Hotfolder.class);

	// The errror, input, output folder.
	protected URI inFolder, outFolder, errrorFolder;

	protected String serverURL, inputFolder, outputFolder, errorFolder;

	protected ConfigParser config;

	private static Hotfolder _instance;

	// internal tweaking variables
	// Variables used for process management
	// The max size, default is defined in ConfigParser
	protected static Long maxSize;

	// The max files,  default is defined in ConfigParser
	protected static Long maxFiles;
	
	// The fsmanager.
	protected FileSystemManager fsManager = null;

	// State variables
	// The total file count.
	protected static Long totalFileCount = 0l;

	// The total file size.
	protected static Long totalFileSize = 0l;

	
	/**
	 * Instantiates a new hotfolder.
	 * 
	 * @throws FileSystemException
	 *             the file system exception
	 */
	private Hotfolder() {
		try {
			VFS.setUriStyle(true);
			fsManager = VFS.getManager();
		} catch (FileSystemException e) {
			logger.error("Can't get file system manager", e);
			throw new OCRException(e);
		}
	}

	public Hotfolder(ConfigParser config) {
		this();
		this.config = config;
	}

	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 * 
	 * @param fileInfos
	 *            is a List of The Class AbbyyOCRImage. Is a representation of
	 *            an OCRImage suitable for holding references to remote files as
	 *            used by the Abbyy Recognition Server
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws FileSystemException
	 */
	public void copyFilesToServer (List<AbbyyOCRImage> fileInfos) throws InterruptedException, FileSystemException {
		// iterate over all Files and put them to Abbyy-server inputFolder:
		for (AbbyyOCRImage info : fileInfos) {
			if (info.toString().endsWith("/")) {
				logger.trace("Creating new directory " + info.getRemoteURI().toString() + "!");
				// Create the directory
				mkDir(info.getRemoteURI());
			} else {
				String to = info.getRemoteURI().toString().replace(config.password, "***");
				logger.trace("Copy from " + info.getUrl().toString() + " to " + to);
				copyFile(info.getUrl().toString(), info.getRemoteURI().toString());
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
	public void delete (URI uri) throws FileSystemException {
		fsManager.resolveFile(uri.toString()).delete();
	}

	/**
	 * Delete a resource at the specified i if exists.
	 * 
	 * @param url
	 *            the url
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public void deleteIfExists (URI uri) throws FileSystemException {
		if (fsManager.resolveFile(uri.toString()).delete()) {
			logger.trace(uri.toString() + " exists already, but now deleted");
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
	public void mkDir (URI uri) throws FileSystemException {
		fsManager.resolveFile(uri.toString()).createFolder();
		logger.debug("Directory " + uri.toString() + " created");
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
	public Boolean exists (URI uri) throws FileSystemException {
		if (fsManager.resolveFile(uri.toString()).exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the total size for a url.
	 * 
	 * @param testImageUrl
	 *            the url
	 * @return the total size
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws URISyntaxException 
	 */
	public Long getTotalSize (URI testImageUri) throws FileSystemException, URISyntaxException {
		FileObject urlFile = fsManager.resolveFile(testImageUri.toString());

		Long size = 0l;
		if (urlFile.getType() == FileType.FOLDER) {
			FileObject[] children = urlFile.getChildren();
			for (int j = 0; j < children.length; j++) {
				if (children[j].getType() == FileType.FOLDER) {
					size += getTotalSize(children[j].getURL().toURI());
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

	public Long getTotalCount (URI uri) throws FileSystemException, URISyntaxException {
		FileObject uriFile = fsManager.resolveFile(uri.toString());
		Long count = 0l;
		if (uriFile.getType() == FileType.FOLDER) {
			FileObject[] children = uriFile.getChildren();
			for (int j = 0; j < children.length; j++) {
				if (children[j].getType() == FileType.FOLDER) {
					count += getTotalCount(children[j].getURL().toURI());
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
	//TODO: Check if this is still needed
	protected List<AbbyyOCRImage> getUrlList (URI imageDirectory) throws FileSystemException, MalformedURLException {
		List<AbbyyOCRImage> imageList = new ArrayList<AbbyyOCRImage>();
		FileObject getUrlImage = fsManager.resolveFile(imageDirectory.toString());
		FileObject[] children = getUrlImage.getChildren();
		for (int i = 0; i < children.length; i++) {
			imageList.add(new AbbyyOCRImage(new URL(children[i].getName().toString())));
		}
		return imageList;
	}

	protected OutputStream getOutputStream (URI uri) throws FileSystemException {
		FileObject out = fsManager.resolveFile(uri.toString());
		return out.getContent().getOutputStream();
	}

	public OutputStream createTmpFile (String name) throws FileSystemException, URISyntaxException {
		String tmpTicket = config.ticketTmpStore + name;
		return getOutputStream(new URI(tmpTicket));
	}

	public void copyTmpFile (String tmpFile, URI to) throws FileSystemException {
		if (!fsManager.resolveFile(config.ticketTmpStore + tmpFile).exists()) {
			logger.error(config.ticketTmpStore + tmpFile + "doesn't exist!");
		}

		copyFile(config.ticketTmpStore + tmpFile, to.toString());
	}

	/**
	 * Gets the in folder.
	 * 
	 * @return the in folder
	 */
	public URI getInFolder () {
		return inFolder;
	}

	/**
	 * Sets the in folder.
	 * 
	 * @param inFolder
	 *            the new in folder
	 */
	public void setInFolder (URI inFolder) {
		this.inFolder = inFolder;
	}

	/**
	 * Gets the out folder.
	 * 
	 * @return the out folder
	 */
	public URI getOutFolder () {
		return outFolder;
	}

	/**
	 * Sets the out folder.
	 * 
	 * @param outFolder
	 *            the new out folder
	 */
	public void setOutFolder (URI outFolder) {
		this.outFolder = outFolder;
	}

	/**
	 * Gets the errror folder.
	 * 
	 * @return the errror folder
	 */
	public URI getErrrorFolder () {
		return errrorFolder;
	}

	/**
	 * Sets the errror folder.
	 * 
	 * @param errrorFolder
	 *            the new errror folder
	 */
	public void setErrrorFolder (URI errrorFolder) {
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

	public static Hotfolder newInstace (ConfigParser config) {
		if (_instance == null) {
			_instance = new Hotfolder(config);
		}
		return _instance;
	}

	public void setConfig (ConfigParser config) {
		this.config = config;
		setErrorFolder(config.error);
		setInputFolder(config.input);
		setOutputFolder(config.output);
		setServerURL(config.getServerURL());
		maxSize = config.getMaxSize();
		maxFiles = config.getMaxFiles();

	}
	
	@SuppressWarnings("serial")
	public void checkServerState () throws IOException, URISyntaxException {
		if (maxSize != 0 && maxFiles != 0) {

			// check if a slash is already appended
			final URI serverUri = new URI(config.getServerURL());
			Map<URI, Long> sizeMap = new LinkedHashMap<URI, Long>() {
				{
					//TODO: There is an error in here
					put(new URI(serverUri.toString() + config.getInput() + "/"), 0l);
					put(new URI(serverUri.toString() + config.getOutput() + "/"), 0l);
					put(new URI(serverUri.toString() + config.getError() + "/"), 0l);
				}
			};

			for (URI uri : sizeMap.keySet()) {
				sizeMap.put(uri, getTotalSize(uri));
			}
			totalFileCount = Integer.valueOf(sizeMap.size()).longValue();
			for (Long size : sizeMap.values()) {
				if (size != null) {
					totalFileSize += size;
				}
			}
			logger.debug("TotalFileSize = " + totalFileSize);

			if (maxFiles != 0 && totalFileCount > maxFiles) {
				logger.error("Too much files. Max number of files is " + maxFiles + ". Number of files on server: " + totalFileCount + ".\nExit program.");
				throw new IllegalStateException("Max number of files exeded");
			}
			if (maxSize != 0 && totalFileSize > maxSize) {
				logger.error("Size of files is too much files. Max size of all files is " + maxSize
						+ ". Size of files on server: "
						+ totalFileSize
						+ ".\nExit program.");
				throw new IllegalStateException("Max size of files exeded");
			}
		} else {
			logger.warn("Server state checking is disabled.");
		}
	}

}
