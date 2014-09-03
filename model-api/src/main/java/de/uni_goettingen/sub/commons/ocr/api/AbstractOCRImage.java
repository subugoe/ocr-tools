package de.uni_goettingen.sub.commons.ocr.api;

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


/**
 * The Class AbstractOCRImage is a abstract super class for {@link OCRImage}
 * implementations. {@link OCRImage} represents an image as a {@link URI}
 * reference and a orientation.
 * 
 * @version 0.9
 * @author abergna
 * @author cmahnke
 */
public abstract class AbstractOCRImage implements OCRImage {

	protected URI localUri = null;

	protected Long fileSize = 0l;

	@Override
	public URI getLocalUri() {
		return this.localUri;
	}

	@Override
	public void setLocalUri(URI newUri) {
		this.localUri = newUri;
	}

	public Long getFileSize() {
		return fileSize;
	}

	@Override
	public void setFileSize(Long size) {
		this.fileSize = size;
	}

}
