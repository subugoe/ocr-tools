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


import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;





/**
 * The Class OCRProcessMetadataImpl. used to obtain a description of the
 * {@link OCRProcess} and it's results This can be used to filter the results
 * for accuracy or to save it for further processing.
 */
public class OCRProcessMetadataImpl implements OCRProcessMetadata{

	/**
	 * Instantiates a new oCR process metadata impl.
	 */
	public OCRProcessMetadataImpl() {
	}
	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getEncoding()
	 */
	@Override
	public String getEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getLinebreak()
	 */
	@Override
	public String getLinebreak() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getFormat()
	 */
	@Override
	public String getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getDocumentType()
	 */
	@Override
	public String getDocumentType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getDocumentTypeVersion()
	 */
	@Override
	public String getDocumentTypeVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getSoftwareName()
	 */
	@Override
	public String getSoftwareName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getSoftwareVersion()
	 */
	@Override
	public String getSoftwareVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getLanguages()
	 */
	@Override
	public List<Locale> getLanguages() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getScripts()
	 */
	@Override
	public List<String> getScripts() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getTextNote()
	 */
	@Override
	public String getTextNote() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getProcessingNote()
	 */
	@Override
	public String getProcessingNote() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getCharacterAccuracy()
	 */
	@Override
	public BigDecimal getCharacterAccuracy() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getWordAccuracy()
	 */
	@Override
	public BigDecimal getWordAccuracy() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata#getDuration()
	 */
	@Override
	public Long getDuration() {
		// TODO Auto-generated method stub
		return null;
	}

}
