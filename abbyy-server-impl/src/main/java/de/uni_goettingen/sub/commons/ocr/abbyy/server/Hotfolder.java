package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.vfs.FileSystemException;

public interface Hotfolder {

	/**
	 * Copy a files from remotefile to localfile. This method should raises an
	 * IOException if the file already exists.
	 * 
	 * @param from
	 *            , the uri of the file name as used on the remote system,
	 *            usally a relative file name and thus represented as a String
	 * @param to
	 *            , an URL representing the local file, it should be resolveable
	 *            from the local Server.
	 * @throws IOException
	 *             the file system exception
	 */
	public abstract void copyFile (URI from, URI to) throws IOException;

	/**
	 * Delete a resource at the specified url
	 * 
	 * @param url
	 *            the url
	 * @throws IOException
	 *             the file system exception
	 */
	public abstract void delete (URI uri) throws IOException;

	/**
	 * Delete a resource at the specified i if exists.
	 * 
	 * @param url
	 *            the url
	 * @throws IOException
	 *             the file system exception
	 */
	public abstract void deleteIfExists (URI uri) throws IOException;

	/**
	 * to create a directory at the specified url
	 * 
	 * @param url
	 *            the url
	 * @throws IOException
	 *             the file system exception
	 */
	public abstract void mkDir (URI uri) throws IOException;

	/**
	 * check if a resource at the specified url.
	 * 
	 * @param uri
	 *            the uri
	 * @return true, if successful
	 */
	public abstract Boolean exists (URI uri) throws IOException;

	public abstract OutputStream createTmpFile (String name) throws IOException;

	public abstract void copyTmpFile (String tmpFile, URI to) throws IOException;

	/**
	 * Gets the total size for a uri.
	 * 
	 * @param testImageUrl
	 *            the uri
	 * @return the total size
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws URISyntaxException
	 */
	public abstract Long getTotalSize (URI testImageUri) throws IOException;

	public abstract Long getTotalCount (URI uri) throws IOException;
	
	public abstract Long getSize (URI uri) throws IOException; 

	public abstract Boolean isDirectory (URI uri) throws IOException;

	public abstract List<URI> listURIs (URI directory) throws IOException;
	
	public abstract InputStream openInputStream (URI uri) throws IOException;

}