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



/**
 * The Class Hotfolder is used to control the hotfolders used by the Abbyy
 * Recognition Server.
 */
public class Hotfolder extends Thread{

	protected URL inFolder, outFolder, errrorFolder;
	FileObject urlFile = null;
	FileObject urlFileStringToUrl = null;
	FileObject getUrlImage = null;
	
	Long totalSize = 0l;
	protected String imageDirectory;
	protected String identifier;
	//protected HierarchicalConfiguration configu;
	protected static List<Locale> langs;
	
	protected static String localOutputDir = null;
	protected List<AbbyyOCRFile> fileInfos = null;
	
	
	protected static Boolean writeRemotePrefix = true;
	//TODO mal sehen ob das brauche

	
	
	

	final static Logger logger = LoggerFactory.getLogger(Hotfolder.class);
	FileSystemManager fsManager = null;

	public Hotfolder() throws FileSystemException {
		fsManager = VFS.getManager();
	}
	
	
	
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
	
	public void copyAllFiles(String remotefile, String localfile) throws FileSystemException{
		FileObject remoteFile = fsManager.resolveFile(remotefile);
		FileObject localFile = fsManager.resolveFile(localfile);
		localFile.copyFrom(remoteFile, new AllFileSelector());
	}

	public void delete(URL url) throws FileSystemException {
		fsManager.resolveFile(url.toString()).delete();
	}

	public void deleteIfExists(URL url) throws FileSystemException {
		if (fsManager.resolveFile(url.toString()).delete())
			logger.debug(url.toString() + " Exists already but now deleted");
	}

	public void deleteIfExists(String url) throws FileSystemException {
		if (fsManager.resolveFile(url).delete())
			logger.debug(url + " Exists already but now deleted");
	}
	
	public void mkCol(URL url) throws FileSystemException {
		fsManager.resolveFile(url.toString()).createFolder();
		logger.debug(url.toString() + " created");
	}

	public boolean fileIfexists(String url) throws FileSystemException {
		if (fsManager.resolveFile(url).exists()) {
			return true;
		} else {
			return false;
		}
	}

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

	public URL stringToUrl (String uri) throws FileSystemException{
		urlFileStringToUrl = fsManager.resolveFile(uri);
		return urlFileStringToUrl.getURL();
		
	}
	
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
	
	public URL fileToURL(File file){
        URL url = null;
        try {
            url = new URL("file://" + file.getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    } 
	
	public File urlToFile(URL url) {
        File file = null;
        try {
          file = new File(url.toURI());
        } catch(URISyntaxException e) {
          file = new File(url.getPath());
        }
        return file;
    } 
	
	
	
	
	public URL getInFolder() {
		return inFolder;
	}

	public void setInFolder(URL inFolder) {
		this.inFolder = inFolder;
	}

	public URL getOutFolder() {
		return outFolder;
	}

	public void setOutFolder(URL outFolder) {
		this.outFolder = outFolder;
	}

	public URL getErrrorFolder() {
		return errrorFolder;
	}

	public void setErrrorFolder(URL errrorFolder) {
		this.errrorFolder = errrorFolder;
	}

}
