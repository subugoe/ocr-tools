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

	protected void copyFilesToServer(List<AbbyyOCRFile> files) throws FileSystemException  {
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
				logger.trace("Copy from " + info.getUrl().toString() + " to " + info.getRemoteFileName());
				//TODO: Upload the file
				//put(AbbyyFileName, info.getLocalFile());

			}
			

		}
		
	}
	
	//Implement a method to download the files
	//abstract void protected getResults ();

	protected Long getSize(String path) {
		return null;
	}
	

}
