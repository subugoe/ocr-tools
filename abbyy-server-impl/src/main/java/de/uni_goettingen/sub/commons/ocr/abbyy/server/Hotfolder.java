package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs.FileSystemException;

public interface Hotfolder {

	/**
	 * Copy a files from remotefile to localfile. Assumes overwrite.
	 * 
	 * @param from
	 *            , the url of the file name as used on the remote system,
	 *            usally a relative file name and thus represented as a String
	 * @param to
	 *            , an URL representing the local file, it should be resolveable
	 *            from the local Server.
	 * @throws IOException
	 *             the file system exception
	 */
	//TODO: Use URLs
	//TODO: This is dangerous, check if the file exists!
	public abstract void copyFile (String from, String to) throws IOException;

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
	 * @param url
	 *            the url
	 * @return true, if successful
	 * @throws FileSystemException
	 *             the file system exception
	 */
	public abstract Boolean exists (URI uri) throws IOException;

	public abstract OutputStream createTmpFile (String name) throws IOException, URISyntaxException;

	public abstract void copyTmpFile (String tmpFile, URI to) throws IOException;

	public abstract void checkServerState () throws IOException, URISyntaxException;

}