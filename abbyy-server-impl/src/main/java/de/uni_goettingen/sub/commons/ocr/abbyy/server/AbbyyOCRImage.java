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

import java.net.URI;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;

/**
 * The Class AbbyyOCRImage. Is a representation of an OCRImage suitable for
 * holding references to remote files as used by the Abbyy Recognition Server.
 * It adds two fields: - remoteFileName, the file name as used on the remote
 * system, usally a relative file name and thus represented as a String. -
 * remoteURL, an URL representing the remote file, it should be resolveable from
 * the local Server.
 */
public class AbbyyOCRImage extends AbstractOCRImage implements OCRImage {
	/** This represents the filename that should be written to the ticket. */
	private String remoteFileName;

	/** This represents the URI to the remote system */
	private URI remoteUri;
	
	/** This represents the URI if the images is part of a failed process. */
	private URI errorUri;

	/** the size of the image, if known */
	private Long size = 0l;

	/**
	 * Instantiates a new abbyy ocr file.
	 * 
	 * @param imageUri
	 *            the image uri
	 */

	public AbbyyOCRImage(URI imageUri) {
		super(imageUri);
	}

	public AbbyyOCRImage(URI uri, Orientation orientation, URI remoteUri, String remoteFileName, Long size) {
		this.imageUri = uri;
		this.orientation = orientation;
		this.remoteUri = remoteUri;
		this.remoteFileName = remoteFileName;
		this.size = size;
	}

	//This calls a copy constructor
	public AbbyyOCRImage(OCRImage i) {
		super(i);
	}

	public AbbyyOCRImage(AbbyyOCRImage i) {
		this(i.imageUri, i.orientation, i.remoteUri, i.remoteFileName, i.size);
	}

	protected AbbyyOCRImage() {

	}

	/**
	 * Instantiates a new abbyy ocr file.
	 * 
	 * @param imageUrl
	 *            the image url
	 * @param remoteURL
	 *            the remote url
	 * @param remoteFileName
	 *            the remote file name
	 */
	public AbbyyOCRImage(URI imageUri, URI remoteUri, String remoteFileName) {
		super(imageUri);
		this.imageUri = imageUri;
		this.remoteUri = remoteUri;
		this.remoteFileName = remoteFileName;
	}

	/**
	 * Gets the remote file name.
	 * 
	 * @return the remote file name
	 */
	public String getRemoteFileName () {
		return remoteFileName;
	}

	/**
	 * Sets the remote file name.
	 * 
	 * @param remoteFileName
	 *            the new remote file name
	 */
	public void setRemoteFileName (String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}

	/**
	 * Gets the remote uri.
	 * 
	 * @return the remote uri
	 */
	public URI getRemoteUri () {
		return this.remoteUri;
	}

	/**
	 * Sets the remote uri.
	 * 
	 * @param remoteURL
	 *            the new remote uri
	 */
	public void setRemoteUri (URI remoteUri) {
		this.remoteUri = remoteUri;
	}

	public Long getSize () {
		return size;
	}

	public void setSize (Long size) {
		this.size = size;
	}

	public URI getErrorUri () {
		return errorUri;
	}

	public void setErrorUri (URI errorUri) {
		this.errorUri = errorUri;
	}
	
}
