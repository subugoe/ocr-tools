package de.uni_goettingen.sub.commons.ocr.abbyy.server;

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

import java.net.URI;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCROutput;

/**
 * The Class AbbyyOCRImage. Is a representation of an OCROutput.
 * -remoteFileName, the file name as used on the remote
 * system, usally a relative file name and thus represented as a String. -
 * remoteURL, an URL representing the remote file, it should be resolveable from
 * the local Server.-remoteLocation, The remote location represents the location 
 * on the remote system, something like D\:\\Recognition\\GDZ\\output.
 */
public class AbbyyOCROutput extends AbstractOCROutput {

	/** The remote location represents the location on the remote system, something like D\:\\Recognition\\GDZ\\output */
	private String winPathForAbbyy;
	
	/** The local file name on the remote system. */
	private String remoteFilename;
	
	/** The URI of the file, need to be resolvable from the local machine. */
	protected URI remoteUri;
	
	protected AbbyyOCROutput() {
		super();
	}

	public AbbyyOCROutput(AbbyyOCROutput aoo) {
		this.localUri = aoo.localUri;
		this.remoteUri = aoo.remoteUri;
		this.winPathForAbbyy = aoo.winPathForAbbyy;
	}

	public String getWindowsPathForAbbyy() {
		return winPathForAbbyy;
	}

	public void setWindowsPathForAbbyy(String winPathForAbbyy) {
		this.winPathForAbbyy = winPathForAbbyy;
	}

	public URI getRemoteUri() {
		return remoteUri;
	}

	public void setRemoteUri(URI remoteUri) {
		this.remoteUri = remoteUri;
	}

	public String getRemoteFilename() {
		return remoteFilename;
	}

	public void setRemoteFilename(String remoteFilename) {
		this.remoteFilename = remoteFilename;
	}
	
}
