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


import java.net.URL;

import java.util.List;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;


import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * The Class Hotfolder is used to control the hotfolders used by the Abbyy
 * Recognition Server.
 */
public class Hotfolder {
	protected URL inFolder, outFolder, errrorFolder;
	
	
	
	
	
	final static Logger logger = LoggerFactory.getLogger(Hotfolder.class);
	FileSystemManager fsManager = null;
	
	public Hotfolder () throws FileSystemException {
		 fsManager = VFS.getManager();
	}

	
	/*static File urlToFile(URL url) {
        File file = null;
        try {
          file = new File(url.toURI());
        } catch(URISyntaxException e) {
          file = new File(url.getPath());
        }
        System.out.println(file);
        return file;
    } */
	
	
	public void copyFilesToServer(List<AbbyyOCRFile> files) throws IOException, InterruptedException  {
		// iterate over all Files and put them to Abbyy-server inputFolder: 
		for (AbbyyOCRFile info : files) {
			/*File f = urlToFile(AbbyyFileName);*/
			
			FileObject remoteFile = fsManager.resolveFile(info.getRemoteURL().toString());
			
			FileObject infile = fsManager.resolveFile(info.getUrl().toString());
			//Delete if exists
			if (remoteFile.delete()){
				logger.debug("Deleted " + remoteFile.toString());
			}
			
			if (info.toString().endsWith("/")) {
				logger.trace("Creating new directory " + info.getRemoteURL().toString() + "!");
				//Create the directory
				remoteFile.createFolder();
				//mkCol(AbbyyFileName.toString());

			} else {

				logger.trace("Copy from " + info.getUrl().toString() + " to " + info.getRemoteURL()); 
				remoteFile.copyFrom(infile, new AllFileSelector()) ;
					
					
				
				//put(AbbyyFileName, new File(info.getRemoteFileName()));

			}			
		}
	}
	
	
	
	public void deleteIfExists(String uri) throws IOException {
	
	}
	
	

	
	


	protected Long getSize(String path) {
		return null;
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
