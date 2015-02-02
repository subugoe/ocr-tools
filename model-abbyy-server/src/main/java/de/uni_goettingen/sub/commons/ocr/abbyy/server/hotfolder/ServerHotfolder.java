package de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder;

/*

 Copyright 2010 SUB Goettingen. All rights reserved.
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
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import de.unigoettingen.sub.commons.ocr.util.FileAccess;

/**
 * The class is a base class for other implementations of
 * Hotfolder. It provides some of the methods that can be implemented completely
 * on top of others. Note that this approach might not be the best from a
 * performance point of view, since it's using just another abstraction.
 * 
 */
public abstract class ServerHotfolder implements Hotfolder {

	protected Map<String, File> tmpfiles = new HashMap<String, File>();
	protected FileAccess fileAccess = new FileAccess();

	// for unit tests
	protected void setFileAccess(FileAccess newAccess) {
		fileAccess = newAccess;
	}

	abstract public void configureConnection(String serverUrl, String username, String password);

	@Override
	public OutputStream createTmpFile(String name) throws IOException {
		File tmpFile = fileAccess.createTempFile(name);
		tmpfiles.put(name, tmpFile);
		return fileAccess.outputStreamForFile(tmpFile);
	}

	@Override
	public void copyTmpFile(String tmpFile, URI to) throws IOException {
		if (tmpfiles.containsKey(tmpFile)) {
			upload(tmpfiles.get(tmpFile).toURI(), to);
		} else {
			throw new IOException("Temp file does not exist.");
		}
	}

	@Override
	public void deleteTmpFile(String name) throws IOException {
		if (tmpfiles.containsKey(name)) {
			fileAccess.deleteFile(tmpfiles.get(name));
			tmpfiles.remove(name);
		}
	}

	@Override
	public void deleteIfExists(URI uri) throws IOException {
		if (exists(uri)) {
			delete(uri);
		}
	}

	@Override
	public Long getTotalSize(URI uri) throws IOException {
		if (!isDirectory(uri)) {
			return getSize(uri);
		}
		Long size = 0l;
		for (URI u : listURIs(uri)) {
			if (isDirectory(uri)) {
				size += getTotalSize(u);
			} else {
				size += getSize(uri);
			}
		}
		return size;
	}
	
}
