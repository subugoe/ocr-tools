package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;

// TODO: Auto-generated Javadoc
/**
 * The Class ApacheVFSHotfolderImpl is used to control the hotfolders used by
 * the Abbyy Recognition Server.
 */
public class ApacheVFSHotfolderImpl extends AbstractHotfolder implements Hotfolder {
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
	
	private String serverUrl = null;

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
			throw new RuntimeException(e);
		}
	}

	private ApacheVFSHotfolderImpl(ConfigParser config) {
		this();
		this.config = config;
		//Construct the login part.
		if (config.getUsername() != null && config.getPassword() != null && config.getServerURL().startsWith("https")) {
			serverUrl = config.getServerURL().replace("https://", "webdavs://" + config.getUsername() + ":" + config.getPassword() + "@");
		} else if (config.getUsername() != null && config.getPassword() != null && config.getServerURL().startsWith("http")) {
			serverUrl = config.getServerURL().replace("http://", "webdav://" + config.getUsername() + ":" + config.getPassword() + "@");
		}
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyFile(java.net.URI, java.net.URI)
	 */
	//TODO: This is dangerous, check if the file exists!
	@Override
	public void copyFile (URI from, URI to) throws IOException {
		FileObject remoteFile = fsManager.resolveFile(from.toString());
		if (remoteFile.exists()) {
			//TODO: There is an error in here.
			throw new IOException("Remote file allready exists!");
		}
		FileObject localFile = fsManager.resolveFile(to.toString());
		localFile.copyFrom(remoteFile, new AllFileSelector());
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#delete(java.net.URI)
	 */
	@Override
	public void delete (URI uri) throws FileSystemException {
		fsManager.resolveFile(uri.toString()).delete();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#mkDir(java.net.URI)
	 */
	@Override
	public void mkDir (URI uri) throws FileSystemException {
		fsManager.resolveFile(uri.toString()).createFolder();
		logger.debug("Directory " + uri.toString() + " created");
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#exists(java.net.URI)
	 */
	@Override
	public Boolean exists (URI uri) throws FileSystemException {
		if (fsManager.resolveFile(uri.toString()).exists()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<URI> listURIs (URI uri) throws IOException {
		List<URI> uriList = new ArrayList<URI>();
		if (isDirectory(uri)) {
			FileObject directory = fsManager.resolveFile(uri.toString());
			FileObject[] children = directory.getChildren();
			for (int i = 0; i < children.length; i++) {
				try {
					uriList.add(new URI(children[i].getName().toString()));
				} catch (URISyntaxException e) {
					logger.error("Error while coverting URI.");
					throw new RuntimeException(e);
				}
			}
		}
		return uriList;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#isDirectory(java.net.URI)
	 */
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
	@Override
	public OutputStream createTmpFile (String name) throws FileSystemException {
		String tmpTicket = config.getTicketTmpStore() + name;
		try {
			return getOutputStream(new URI(tmpTicket));
		} catch (URISyntaxException e) {
			logger.error("Error while coverting URI.");
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyTmpFile(java.lang.String, java.net.URI)
	 */
	@Override
	public Boolean copyTmpFile (String tmpFile, URI to) throws IOException {
		if (!fsManager.resolveFile(config.getTicketTmpStore() + tmpFile).exists()) {
			logger.error(config.getTicketTmpStore() + tmpFile + "doesn't exist!");
		} else {
			return false;
		}
		try {
			copyFile(new URI(config.getTicketTmpStore() + tmpFile), to);
		} catch (URISyntaxException e) {
			logger.error("Couldn't creste URI for tempory file", e);
		}
		return true;
	}

	/**
	 * New instance.
	 * 
	 * @param config
	 *            the config
	 * @return the hotfolder
	 */
	public static Hotfolder getInstance (ConfigParser config) {
		if (_instance == null) {
			_instance = new ApacheVFSHotfolderImpl(config);
		}
		return _instance;
	}

	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the new config
	 */
	public void setConfig (ConfigParser config) {
		this.config = config;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#openInputStream(java.net.URI)
	 */
	@Override
	public InputStream openInputStream (URI uri) throws IOException {
		FileObject uriFile = fsManager.resolveFile(uri.toString());
		return uriFile.getContent().getInputStream();
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#getSize(java.net.URI)
	 */
	@Override
	public Long getSize (URI uri) throws IOException {
		FileObject uriFile = fsManager.resolveFile(uri.toString());
		if (uriFile.getType() != FileType.FOLDER) {
			return uriFile.getContent().getSize();
		}
		return 0l;
	}
	
	public String rewiteURL (String url) {
		if (serverUrl != null) {
		return url.replace(config.getServerURL(), serverUrl);
		}
		return url;
	}

}