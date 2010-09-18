package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.net.URL;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Hotfolder {

	final Logger logger = LoggerFactory.getLogger(Hotfolder.class);
	FileSystemManager fsManager = null;
	
	public Hotfolder () throws FileSystemException {
		 fsManager = VFS.getManager();
	}

	protected void copyFilesToServer(List<WebDAVOCRFile> files)  {
		// iterate over all Files and put them to webdav-server inputFolder: 
		for (WebDAVOCRFile info : files) {
			
			URL webDavFileName = info.getRemoteURL();
			FileObject remoteFile = fsManager.resolveFile(webDavFileName.toString());
			remoteFile.delete();
			if (webDavFileName.endsWith("/")) {
				logger.trace("Creating new directory " + webDavFileName + "!");
				mkCol(webDavFileName);
			} else {
				logger.trace("Copy from " + info.getLocalFile().getAbsolutePath() + " to " + webDavFileName);
				put(webDavFileName, info.getLocalFile());

			}
			

		}
		
	}
	
	//Implement a method to download the files
	//abstract void protected getResults ();

	protected Long getSize(String path) {
		return null;
	}
	

}
