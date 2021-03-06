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

import de.uni_goettingen.sub.commons.ocr.api.AbstractOutput;

/**
 * The Class AbbyyImage. Is a representation of an OcrOutput.
 * -remoteFileName, the file name as used on the remote
 * system, usally a relative file name and thus represented as a String. -
 * remoteURL, an URL representing the remote file, it should be resolveable from
 * the local Server.-remoteLocation, The remote location represents the location 
 * on the remote system, something like D\:\\Recognition\\GDZ\\output.
 */
public class AbbyyOutput extends AbstractOutput {
	
	/** The URI of the file, need to be resolvable from the local machine. */
	private URI remoteUri;
	
	public URI getRemoteUri() {
		return remoteUri;
	}

	public void setRemoteUri(URI remoteUri) {
		this.remoteUri = remoteUri;
	}
	
}
