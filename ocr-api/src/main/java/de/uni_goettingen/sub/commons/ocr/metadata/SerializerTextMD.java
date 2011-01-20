package de.uni_goettingen.sub.commons.ocr.metadata;

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

import java.io.File;
import java.io.OutputStream;



/**
 * The Interface SerializerTextMD can be used to obtain a description of the.
 * 
 * {@link OCRProcessMetadata} and it's results This can be used to Creating a TextMD,
 * there are two methods which can provide textMD
 * 
 * @author abergna
 */

public interface SerializerTextMD {
		
	/**
	 * Write.
	 *
	 * @param file the file
	 */
	abstract public void write(File file);	
	
	/**
	 * Write.
	 *
	 * @param outputstream the outputstream
	 */
	abstract public void write(OutputStream outputstream);
	
}
