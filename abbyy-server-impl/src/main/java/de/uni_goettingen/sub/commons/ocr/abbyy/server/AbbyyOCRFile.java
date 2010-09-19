package de.uni_goettingen.sub.commons.ocr.abbyy.server;
/*

© 2010, SUB Göttingen. All rights reserved.
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

public class AbbyyOCRFile extends AbstractOCRImage implements OCRImage {
	//This represents the filename that should be written to the ticket.
	protected String remoteFileName;

	//This represents the URL to the remote system
	protected URL remoteURL;

	public AbbyyOCRFile(URL imageUrl) {
		super(imageUrl);
	}
	
	public AbbyyOCRFile(URL imageUrl, URL remoteURL, String remoteFileName) {
		super(imageUrl);
		this.remoteURL = remoteURL;
		this.remoteFileName = remoteFileName;
	}

	public String getRemoteFileName() {
		return remoteFileName;
	}

	public void setRemoteFileName(String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}

	public URL getRemoteURL() {
		return remoteURL;
	}

	public void setRemoteURL(URL remoteURL) {
		this.remoteURL = remoteURL;
	}

}
