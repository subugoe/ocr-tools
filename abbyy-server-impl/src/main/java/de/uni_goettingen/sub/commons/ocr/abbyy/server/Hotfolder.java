package de.uni_goettingen.sub.commons.ocr.abbyy.server;

/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://www.sub.uni-goettingen.de 
 * 
 * Copyright 2009, 2010, SUB Goettingen.
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

//TODO: Add a method to open an OutputStream
/**
 * The Interface Hotfolder is used to access any file system like backend. This
 * can be used to integrate external systems like Grid storage or WebDAV based
 * hotfolders.
 * 
 * @version 0.2
 * @author abergna
 * @author cmahnke
 * @since 0.2
 */
public interface Hotfolder {

	/**
	 * Copy a file from one location to another. Implementors should raises an
	 * IOException if the file already exists.
	 * 
	 * @param from
	 *            the uri of the file name as used on the remote system, usally
	 *            a relative file name and thus represented as a String
	 * @param to
	 *            an URL representing the local file, it should be resolveable
	 *            from the local Server.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void copyFile (URI from, URI to) throws IOException;

	/**
	 * Delete a resource at the specified url.
	 * 
	 * @param uri
	 *            the uri
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void delete (URI uri) throws IOException;

	/**
	 * Delete a resource at the specified URI if it exists.
	 * 
	 * @param uri
	 *            the uri
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void deleteIfExists (URI uri) throws IOException;

	/**
	 * Create a directory or collection at the specified url.
	 * 
	 * @param uri
	 *            the URI of the directory to be created
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void mkDir (URI uri) throws IOException;

	/**
	 * Check if a resource at the specified url exists
	 * 
	 * @param uri
	 *            the uri
	 * @return true, if successful, false otherwise.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract Boolean exists (URI uri) throws IOException;

	/**
	 * Creates the temporary file and returns the {@link java.io.OutputStream}
	 * to write to this file.
	 * 
	 * @param name
	 *            the name of the temp file. Note that this might be just used
	 *            by implementations of this interface, te file isn't guaranteed
	 *            to be named like specified here.
	 * @return the output stream to write to
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract OutputStream createTmpFile (String name) throws IOException;

	/**
	 * Copy a temporary file with the given name. Make sure it was created using
	 * {@link #createTmpFile(String)} before.
	 * 
	 * @param tmpFile
	 *            the tmp file
	 * @param to
	 *            the URI to copy the file to
	 * @return true if the file was copied, false if the file wasn't created
	 *         before hand or it can't be copied
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract Boolean copyTmpFile (String tmpFile, URI to) throws IOException;

	/**
	 * Gets the total size for a URI returns just the size of this URI if it
	 * represents a single file. This method also looks for the size of children
	 * if they exists.
	 * 
	 * @param uri
	 *            the URI to check
	 * @return the total size
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract Long getTotalSize (URI uri) throws IOException;

	/**
	 * Gets the total count of files under a given URI, returns just 1 if the
	 * URI represents a single file. This method also looks for the number of
	 * children if they exists.
	 * 
	 * @param uri
	 *            the URI to check
	 * @return the total count
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract Long getTotalCount (URI uri) throws IOException;

	/**
	 * Gets the size of a given URI. If the URI represents a collection or
	 * directory the size should be returned as 0.
	 * 
	 * @param uri
	 *            the URI
	 * @return the size
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract Long getSize (URI uri) throws IOException;

	/**
	 * Checks if the URI represents directory or resource collection.
	 * 
	 * @param uri
	 *            the URI to check
	 * @return true if this is a directory
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract Boolean isDirectory (URI uri) throws IOException;

	/**
	 * List the URIs that are children of the given URI. In other terms, this
	 * can be used to generate a directory listing. This should return a empty
	 * List (nut null) if the URI doesn't represent a directory.
	 * 
	 * @param uri
	 *            the URI to lists it's contents
	 * @return a List of URI's
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract List<URI> listURIs (URI uri) throws IOException;

	/**
	 * Open an {@link java.io.InputStream} for the given URI to read files based
	 * on stream.
	 * 
	 * @param uri
	 *            the URI
	 * @return the input stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract InputStream openInputStream (URI uri) throws IOException;
	
	//public abstract void lock ();
	
}