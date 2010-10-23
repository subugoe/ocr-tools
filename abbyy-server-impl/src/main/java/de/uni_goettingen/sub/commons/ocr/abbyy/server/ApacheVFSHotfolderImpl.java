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
import java.net.URI;
import java.net.URISyntaxException;
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
 * The Class ApacheVFSHotfolderImpl is used to control the hotfolders used by
 * the Abbyy Recognition Server.
 */
public class ApacheVFSHotfolderImpl extends Thread implements Hotfolder {
	// The Constant logger.
	final static Logger logger = LoggerFactory.getLogger(ApacheVFSHotfolderImpl.class);

	protected ConfigParser config;

	private static Hotfolder _instance;

	// The fsmanager.
	protected FileSystemManager fsManager = null;

	// State variables
	// The total file count.
	protected static Long totalFileCount = 0l;

	// The total file size.
	protected static Long totalFileSize = 0l;

	/**
	 * Instantiates a new apacheVFSHotfolderImpl.
	 * 
	 * @throws FileSystemException
	 *             the file system exception
	 */
	private ApacheVFSHotfolderImpl() {
		try {
			VFS.setUriStyle(true);
			fsManager = VFS.getManager();
		} catch (FileSystemException e) {
			logger.error("Can't get file system manager", e);
			throw new OCRException(e);
		}
	}

	private ApacheVFSHotfolderImpl(ConfigParser config) {
		this();
		this.config = config;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyFile(java.lang.String, java.lang.String)
	 */
	//TODO: This is dangerous, check if the file exists!
	public void copyFile (URI from, URI to) throws IOException {
		FileObject remoteFile = fsManager.resolveFile(from.toString());
		if (remoteFile.exists()) {
			throw new IOException("Remote file allready exists!");
		}
		FileObject localFile = fsManager.resolveFile(to.toString());
		localFile.copyFrom(remoteFile, new AllFileSelector());
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#delete(java.net.URI)
	 */
	public void delete (URI uri) throws FileSystemException {
		fsManager.resolveFile(uri.toString()).delete();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#deleteIfExists(java.net.URI)
	 */
	public void deleteIfExists (URI uri) throws FileSystemException {
		if (fsManager.resolveFile(uri.toString()).delete()) {
			logger.trace(uri.toString() + " exists already, but now deleted");
		}
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#mkDir(java.net.URI)
	 */
	public void mkDir (URI uri) throws FileSystemException {
		fsManager.resolveFile(uri.toString()).createFolder();
		logger.debug("Directory " + uri.toString() + " created");
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#exists(java.net.URI)
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
	public Long getTotalSize (URI testImageUri) throws IOException, URISyntaxException {
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

	public Long getTotalCount (URI uri) throws IOException, URISyntaxException {
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
	 * @throws URISyntaxException
	 * @throws IOException
	 *             the malformed url exception
	 */
	@Override
	public List<URI> listURIs (URI uri) throws IOException, URISyntaxException {
		List<URI> uriList = new ArrayList<URI>();
		if (isDirectory(uri)) {
			FileObject directory = fsManager.resolveFile(uri.toString());
			FileObject[] children = directory.getChildren();
			for (int i = 0; i < children.length; i++) {
				uriList.add(new URI(children[i].getName().toString()));
			}
		}
		return uriList;
	}

	@Override
	public Boolean isDirectory (URI uri) throws IOException {
		if (fsManager.resolveFile(uri.toString()).getType() == FileType.FOLDER) {
			return true;
		}
		return false;
	}

	private OutputStream getOutputStream (URI uri) throws FileSystemException {
		FileObject out = fsManager.resolveFile(uri.toString());
		return out.getContent().getOutputStream();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#createTmpFile(java.lang.String)
	 */
	public OutputStream createTmpFile (String name) throws FileSystemException, URISyntaxException {
		String tmpTicket = config.ticketTmpStore + name;
		return getOutputStream(new URI(tmpTicket));
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyTmpFile(java.lang.String, java.net.URI)
	 */
	public void copyTmpFile (String tmpFile, URI to) throws IOException {
		if (!fsManager.resolveFile(config.ticketTmpStore + tmpFile).exists()) {
			logger.error(config.ticketTmpStore + tmpFile + "doesn't exist!");
		}
		try {
			copyFile(new URI(config.ticketTmpStore + tmpFile), to);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Hotfolder newInstance (ConfigParser config) {
		if (_instance == null) {
			_instance = new ApacheVFSHotfolderImpl(config);
		}
		return _instance;
	}

	public void setConfig (ConfigParser config) {
		this.config = config;
	}

}
