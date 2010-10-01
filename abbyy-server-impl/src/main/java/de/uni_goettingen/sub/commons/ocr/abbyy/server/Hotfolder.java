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






import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



// TODO: Auto-generated Javadoc
/**
 * The Class Hotfolder is used to control the hotfolders used by the Abbyy
 * Recognition Server.
 */
public class Hotfolder extends Thread{

	/** The errror, input, output folder. */
	protected URL inFolder, outFolder, errrorFolder;
	
	/** The url file. */
	FileObject urlFile = null;
	
	/** The url file string to url. */
	FileObject urlFileStringToUrl = null;
	
	/** The get url image. */
	FileObject getUrlImage = null;
	
	/** The total size. */
	Long totalSize = 0l;
	
	/** The image directory. */
	protected String imageDirectory;
	
	/** The identifier. */
	protected String identifier;
	//protected HierarchicalConfiguration configu;
	/** The language. */
	protected static List<Locale> langs;
	
	/** The local output dir. */
	protected static String localOutputDir = null;
	
	/** The fileinfos. */
	protected List<AbbyyOCRFile> fileInfos = null;
	
	
	//protected static Boolean writeRemotePrefix = true;

	/** The Constant logger. */
	final static Logger logger = LoggerFactory.getLogger(Hotfolder.class);
	
	/** The fsmanager. */
	FileSystemManager fsManager = null;

	/**
	 * Instantiates a new hotfolder.
	 *
	 * @throws FileSystemException the file system exception
	 */
	public Hotfolder() throws FileSystemException {
		fsManager = VFS.getManager();
	}
	
	
	
	/**
	 * Copy a url from source to destination. Assumes overwrite.
	 *
	 * @param files is a List of The Class AbbyyOCRFile. Is a representation of an 
	 * OCRImage suitable for holding references to remote files as used by the Abbyy 
	 * Recognition Server
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public void copyFilesToServer(List<AbbyyOCRFile> files) throws IOException,
			InterruptedException {
		// iterate over all Files and put them to Abbyy-server inputFolder:
		for (AbbyyOCRFile info : files) {
			/* File f = urlToFile(AbbyyFileName); */
			FileObject remoteFile = fsManager.resolveFile(info.getRemoteURL()
					.toString());
			FileObject imageUrlfile = fsManager.resolveFile(info.getUrl()
					.toString());
			// Delete if exists
			deleteIfExists(info.getRemoteURL());
			if (info.toString().endsWith("/")) {
				logger.trace("Creating new directory "
						+ info.getRemoteURL().toString() + "!");
				// Create the directory
				mkCol(info.getRemoteURL());

			} else {

				logger.trace("Copy from " + info.getUrl().toString() + " to "
						+ info.getRemoteURL());
				remoteFile.copyFrom(imageUrlfile, new AllFileSelector());
			}
		}
	}
	
	/**
	 * Copy a files from remotefile to localfile. Assumes overwrite.
	 * 
	 * @param remotefile, the url of the file name as used on the remote system, usally a
	 * relative file name and thus represented as a String
	 * @param localfile, an URL representing the local file, it should be resolveable
	 * from the local Server.
	 * @throws FileSystemException the file system exception
	 */
	public void copyAllFiles(String remotefile, String localfile) throws FileSystemException{
		FileObject remoteFile = fsManager.resolveFile(remotefile);
		FileObject localFile = fsManager.resolveFile(localfile);
		localFile.copyFrom(remoteFile, new AllFileSelector());
	}

	/**
	 * Delete a resource at the specified url
	 *
	 * @param url the url
	 * @throws FileSystemException the file system exception
	 */
	public void delete(URL url) throws FileSystemException {
		fsManager.resolveFile(url.toString()).delete();
	}

	/**
	 * Delete a resource at the specified url if exists.
	 *
	 * @param url the url
	 * @throws FileSystemException the file system exception
	 */
	public void deleteIfExists(URL url) throws FileSystemException {
		if (fsManager.resolveFile(url.toString()).delete())
			logger.debug(url.toString() + " Exists already but now deleted");
	}

	/**
	 * Delete a resource at the specified url(String) if exists.
	 *
	 * @param url the url
	 * @throws FileSystemException the file system exception
	 */
	public void deleteIfExists(String url) throws FileSystemException {
		if (fsManager.resolveFile(url).delete())
			logger.debug(url + " Exists already but now deleted");
	}
	
	/**
	 * to create a directory at the specified url
	 *
	 * @param url the url
	 * @throws FileSystemException the file system exception
	 */
	public void mkCol(URL url) throws FileSystemException {
		fsManager.resolveFile(url.toString()).createFolder();
		logger.debug(url.toString() + " created");
	}

	/**
	 * a resource at the specified url if exists.
	 *
	 * @param url the url
	 * @return true, if successful
	 * @throws FileSystemException the file system exception
	 */
	public boolean fileIfexists(String url) throws FileSystemException {
		if (fsManager.resolveFile(url).exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the total size for a url.
	 *
	 * @param url the url
	 * @return the total size
	 * @throws FileSystemException the file system exception
	 */
	public Long getTotalSize(URL url) throws FileSystemException {
		totalSize++;
		urlFile = fsManager.resolveFile(url.toString());

		long foldersize = 0;
		if (urlFile.getType().toString().equals("folder")) {
			FileObject[] children = urlFile.getChildren();
			for (int j = 0; j < children.length; j++) {
				if (children[j].getType().toString().equals("folder")) {
					foldersize += getTotalSize(children[j].getURL());
				} else {
					FileContent contentFile = children[j].getContent();
					foldersize += contentFile.getSize();
				}
			}
			return foldersize;
		} else {
			FileContent content = urlFile.getContent();
			return content.getSize();
		}
	}

	/**
	 * String to url.
	 *
	 * @param uri the uri
	 * @return the uRL
	 * @throws FileSystemException the file system exception
	 */
	public URL stringToUrl (String uri) throws FileSystemException{
		urlFileStringToUrl = fsManager.resolveFile(uri);
		return urlFileStringToUrl.getURL();
		
	}
	
	/**
	 * Gets the url list.
	 *
	 * @param imageDirectory the image directory
	 * @return the url list
	 * @throws FileSystemException the file system exception
	 * @throws MalformedURLException the malformed url exception
	 */
	List<AbbyyOCRFile> getUrlList(String imageDirectory) throws FileSystemException, MalformedURLException {
		List<AbbyyOCRFile> imageList = new ArrayList<AbbyyOCRFile>();
		getUrlImage = fsManager.resolveFile(imageDirectory);
		FileObject[] children = getUrlImage.getChildren();
		 for ( int i = 0; i < children.length; i++ )
	        {
			 imageList.add(new AbbyyOCRFile(new URL(children[ i ].getName().toString())));
	        }
		return imageList;
	}
	
	/**
	 * File to url.
	 *
	 * @param file the file
	 * @return the uRL
	 */
	public URL fileToURL(File file){
        URL url = null;
        try {
            url = new URL("file://" + file.getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    } 
	
	/**
	 * Url to file.
	 *
	 * @param url the url
	 * @return the file
	 */
	public File urlToFile(URL url) {
        File file = null;
        try {
          file = new File(url.toURI());
        } catch(URISyntaxException e) {
          file = new File(url.getPath());
        }
        return file;
    } 
	
	
	
	
	/**
	 * Gets the in folder.
	 *
	 * @return the in folder
	 */
	public URL getInFolder() {
		return inFolder;
	}

	/**
	 * Sets the in folder.
	 *
	 * @param inFolder the new in folder
	 */
	public void setInFolder(URL inFolder) {
		this.inFolder = inFolder;
	}

	/**
	 * Gets the out folder.
	 *
	 * @return the out folder
	 */
	public URL getOutFolder() {
		return outFolder;
	}

	/**
	 * Sets the out folder.
	 *
	 * @param outFolder the new out folder
	 */
	public void setOutFolder(URL outFolder) {
		this.outFolder = outFolder;
	}

	/**
	 * Gets the errror folder.
	 *
	 * @return the errror folder
	 */
	public URL getErrrorFolder() {
		return errrorFolder;
	}

	/**
	 * Sets the errror folder.
	 *
	 * @param errrorFolder the new errror folder
	 */
	public void setErrrorFolder(URL errrorFolder) {
		this.errrorFolder = errrorFolder;
	}

}
