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

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class AbbyyOCROutput extends AbstractOCROutput {

	protected String remoteLocation;

	public AbbyyOCROutput(OCROutput ocrOutput) {
		super(ocrOutput);
	}

	public AbbyyOCROutput() {

	}

	/**
	 * @return the remoteLocation
	 */
	public String getRemoteLocation () {
		return remoteLocation;
	}

	/**
	 * @param remoteLocation
	 *            the remoteLocation to set
	 */
	public void setRemoteLocation (String remoteLocation) {
		this.remoteLocation = remoteLocation;
	}

}
