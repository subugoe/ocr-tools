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
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.HeadMethod;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class Hotfolder is used to control the hotfolders used by the Abbyy
 * Recognition Server.
 */
public class Hotfolder {
	protected URL inFolder, outFolder, errrorFolder;
	
	
	protected HttpClient client = null;
	
	
	final static Logger logger = LoggerFactory.getLogger(Hotfolder.class);
	FileSystemManager fsManager = null;
	
	public Hotfolder () throws FileSystemException {
		 fsManager = VFS.getManager();
	}

	protected void copyFilesToServer(List<AbbyyOCRFile> files) throws HttpException, IOException, InterruptedException  {
		// iterate over all Files and put them to Abbyy-server inputFolder: 
		for (AbbyyOCRFile info : files) {
			

			URL AbbyyFileName = info.getRemoteURL();
			FileObject remoteFile = fsManager.resolveFile(AbbyyFileName.toString());
			//Delete if exists
			if (remoteFile.delete()){
				logger.debug("Deleted " + remoteFile.toString());
			}
		
			if (AbbyyFileName.toString().endsWith("/")) {
				logger.trace("Creating new directory " + AbbyyFileName + "!");
				//Create the directory
				mkCol(AbbyyFileName.toString());

			} else {
				logger.trace("Copy from " + info.getUrl().toString() + " to " + info.getRemoteFileName()); 
				put(AbbyyFileName.toString(), info.getUrl());

			}			
		}
	}
	
	private void mkCol(String uri) throws HttpException, IOException, InterruptedException {
		DavMethod mkCol = new MkColMethod(uri);
		executeMethod(this.client, mkCol);

		//Since we use the multithreaded Connection manager we have to wait until the directory is created
		//The problem doesn't accoure in debug mode since the main thread is slower there
		//You get a 403 if you try to PUT something in an non existing COLection
		while (true) {
			Integer status = head(uri);
			if (status == 200) {
				break;
			}
			if (status == 403) {
				throw new IllegalStateException("Got HTTP Code " + status);
			}
		}
	}
	
	protected void delete(String uri) throws HttpException, IOException {
		DavMethod delete = new DeleteMethod(uri);
		executeMethod(this.client, delete);
	}
	
	
	protected void put(String uri, URL url) throws HttpException, IOException {
		File file = new File(url.toString());
		if (!file.exists()) {
			throw new IllegalArgumentException("URL " + file + " doesn't exist.");
		}
		PutMethod put = new PutMethod(uri);
		String fileName = file.getPath();
		String mimeType = URLConnection.guessContentTypeFromName(fileName);
		put.setRequestEntity(new FileRequestEntity(file, mimeType));
		executeMethod(this.client, put);
	}
	
		
	public static void executeMethod(HttpClient client, DavMethod method) throws HttpException, IOException {
		Integer responseCode = client.executeMethod(method);
		method.releaseConnection();
		logger.trace("Response code: " + responseCode);
		if (responseCode >= 400) {
			throw new IllegalStateException("Got HTTP Code " + responseCode + " for " + method.getURI());
		}
	}

	
	protected int head(String uri) throws HttpException, IOException {
		HeadMethod head = new HeadMethod(uri);
		Integer status = this.client.executeMethod(head);
		head.releaseConnection();
		return status;
	}
	//Implement a method to download the files
	//abstract void protected getResults ();


	protected Long getSize(String path) {
		return null;
	}
	

}
