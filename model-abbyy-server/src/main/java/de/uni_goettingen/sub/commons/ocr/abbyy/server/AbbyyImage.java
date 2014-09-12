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

import de.uni_goettingen.sub.commons.ocr.api.AbstractImage;

/**
 * The Class AbbyyImage. Is a representation of an OcrImage suitable for
 * holding references to remote files as used by the Abbyy Recognition Server.
 * It adds two fields: - remoteFileName, the file name as used on the remote
 * system, usally a relative file name and thus represented as a String. -
 * remoteURL, an URL representing the remote file, it should be resolveable from
 * the local Server.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public class AbbyyImage extends AbstractImage {
	/** This represents the filename that should be written to the ticket. */
	private String remoteFileName;

	/** This represents the URI to the remote system. */
	private URI remoteUri;

	/** This represents the URI if the images is part of a failed process. */
	private URI errorUri;

	/**
	 * Gets the remoteFilename is the local file name on the remote system.
	 * 
	 * @return the remoteFilename.
	 */
	public String getRemoteFileName() {
		return remoteFileName;
	}

	/**
	 * Sets the remoteFilename is the local file name on the remote system.
	 * 
	 * @param remoteFileName
	 */
	public void setRemoteFileName(String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}

	/**
	 * Gets the remoteUri. The URI of the file, need to be resolvable from the
	 * local machine.
	 * 
	 * @return the remoteUri
	 */
	public URI getRemoteUri() {
		return this.remoteUri;
	}

	/**
	 * Sets the remoteUri. The URI of the file, need to be resolvable from the
	 * local machine.
	 * 
	 * @param remoteUri
	 */
	public void setRemoteUri(URI remoteUri) {
		this.remoteUri = remoteUri;
	}

	/**
	 * Gets the errorUri, This represents the URI if the images is part of a
	 * failed process.
	 * 
	 * @return the errorUri
	 */
	public URI getErrorUri() {
		return errorUri;
	}

	/**
	 * Sets the errorUri, This represents the URI if the images is part of a
	 * failed process.
	 * 
	 * @param errorUri
	 */
	public void setErrorUri(URI errorUri) {
		this.errorUri = errorUri;
	}

}
