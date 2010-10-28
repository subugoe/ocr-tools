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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;

/**
 * The Class AbstractHotfolder is a base class for other implementations of
 * Hotfolder. It provides some of the methods that can be implemented completely
 * on top of others. Note that this approach might not be the best from a
 * performance point of view, since it's using just another abstraction.
 * 
 * @version 0.2
 * @author cmahnke
 * @since 0.2
 */
public abstract class AbstractHotfolder implements Hotfolder {
	
	//Simple Implementation of tempfile based on a local file.
	protected Map<String, File> tmpfiles = new HashMap<String, File>();

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#deleteIfExists(java.net.URI)
	 */
	@Override
	public void deleteIfExists (URI uri) throws IOException {
		if (exists(uri)) {
			delete(uri);
		}
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#getTotalSize(java.net.URI)
	 */
	@Override
	public Long getTotalSize (URI uri) throws IOException {
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

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#getTotalCount(java.net.URI)
	 */
	@Override
	public Long getTotalCount (URI uri) throws IOException {
		if (!isDirectory(uri)) {
			return 1l;
		}
		Long count = 0l;
		for (URI u : listURIs(uri)) {
			if (isDirectory(uri)) {
				count += getTotalCount(u);
			} else {
				count += 1l;
			}
		}
		return count;
	}
	
	protected Boolean isLocal (URI uri) {
		return uri.getScheme().equals("file");
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#copyTmpFile(java.lang.String, java.net.URI)
	 */
	@Override
	public Boolean copyTmpFile (String tmpFile, URI to) throws IOException {
		if (tmpfiles.containsKey(tmpFile)) {
			copyFile(tmpfiles.get(tmpFile).toURI(), to);
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder#createTmpFile(java.lang.String)
	 */
	@Override
	public OutputStream createTmpFile (String name) throws IOException {
		File tmpFile = File.createTempFile(name, null);
		tmpfiles.put(name, tmpFile);
		return new FileOutputStream(tmpFile);
	}
	
	public static Hotfolder getHotfolder (String cls, ConfigParser config) {
		Class<?> c;
		try {
			c = Class.forName(cls);
			return (Hotfolder) c.getMethod("getInstance", new Class[] {ConfigParser.class}).invoke(null, new Object[] {config});
 		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
