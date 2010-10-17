package de.uni_goettingen.sub.commons.ocr.abbyy.server;
/*

© 2010, SUB Goettingen. All rights reserved.
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

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;

/**
 * The Class AbbyyOCRFile. Is a representation of an OCRImage suitable
 * for holding references to remote files as used by the Abbyy Recognition
 * Server. It adds two fields:
 * - remoteFileName, the file name as used on the remote system, usally a
 * relative file name and thus represented as a String.
 * - remoteURL, an URL representing the remote file, it should be resolveable
 * from the local Server. 
 */
public class AbbyyOCRFile extends AbstractOCRImage implements OCRImage {
	//This represents the filename that should be written to the ticket.
	protected String remoteFileName;

	//This represents the URL to the remote system
	protected URL remoteURL = null;

//	protected URL url = null;
	
	/**
	 * Instantiates a new abbyy ocr file.
	 * 
	 * @param imageUrl the image url
	 */
	public AbbyyOCRFile(URL imageUrl) {
		super(imageUrl);
	}
	
	public AbbyyOCRFile (OCRImage i) {
		super(i);
	}
	
	protected AbbyyOCRFile () {
		
	}
	
	/**
	 * Instantiates a new abbyy ocr file.
	 *
	 * @param imageUrl the image url
	 * @param remoteURL the remote url
	 * @param remoteFileName the remote file name
	 */
	public AbbyyOCRFile(URL imageUrl, URL remoteURL, String remoteFileName) {
		super(imageUrl);
		this.url = imageUrl;
		this.remoteURL = remoteURL;
		this.remoteFileName = remoteFileName;
	}

	//TODO: Add a copy contructor for OCRFile.
	
	/**
	 * Gets the remote file name.
	 *
	 * @return the remote file name
	 */
	public String getRemoteFileName() {
		return remoteFileName;
	}

	/**
	 * Sets the remote file name.
	 *
	 * @param remoteFileName the new remote file name
	 */
	public void setRemoteFileName(String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}

	/**
	 * Gets the remote url.
	 *
	 * @return the remote url
	 */
	public URL getRemoteURL() {
		return this.remoteURL;
	}

	/**
	 * Sets the remote url.
	 *
	 * @param remoteURL the new remote url
	 */
	public void setRemoteURL(URL remoteURL) {
		this.remoteURL = remoteURL;
	}
	
	/*public URL getUrl() {
		return this.url;
	}*/
	
}
