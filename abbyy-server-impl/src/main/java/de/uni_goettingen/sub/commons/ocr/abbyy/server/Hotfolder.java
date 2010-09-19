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

	protected void copyFilesToServer(List<AbbyyOCRFile> files)  {
		// iterate over all Files and put them to Abbyy-server inputFolder: 
		for (AbbyyOCRFile info : files) {
			
			URL AbbyyFileName = info.getRemoteURL();
			FileObject remoteFile = fsManager.resolveFile(AbbyyFileName.toString());
			remoteFile.delete();
			if (AbbyyFileName.toString().endsWith("/")) {
				logger.trace("Creating new directory " + AbbyyFileName + "!");
				//TODO: Create the directory
				//mkCol(AbbyyFileName);
			} else {
				logger.trace("Copy from " + info.getLocalFile().getAbsolutePath() + " to " + AbbyyFileName);
				put(AbbyyFileName, info.getLocalFile());

			}
			

		}
		
	}
	
	//Implement a method to download the files
	//abstract void protected getResults ();

	protected Long getSize(String path) {
		return null;
	}
	

}
