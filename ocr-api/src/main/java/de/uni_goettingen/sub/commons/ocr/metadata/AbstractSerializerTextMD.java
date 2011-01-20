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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import noNamespace.TextMDDocument;
import noNamespace.TextMDDocument.TextMD;
import noNamespace.TextMDDocument.TextMD.CharacterInfo;
import noNamespace.TextMDDocument.TextMD.Encoding;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.Linebreak.Enum;

import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;




/**
 * The Class AbstractSerializerTextMD is a abstract super class for {@link SerializerTextMD}
 * implementations. {@link SerializerTextMD}, there are two methods which can provide textMD
 * 
 */
public abstract class AbstractSerializerTextMD implements SerializerTextMD {

	/** The tmpfiles. */
	protected Map<String, File> tmpfiles = new HashMap<String, File>();
	
	/** The Constant NAMESPACE. */
	public final static String NAMESPACE = "info:lc/xmlns/textMD-v3";
	
	/** The opts. */
	private static XmlOptions opts = new XmlOptions()
			.setDocumentSourceName("textMD");
	
	/** The ocr process metadata. */
	protected OCRProcessMetadata ocrProcessMetadata;
	
	/** The Constant logger. */
	public final static Logger logger = LoggerFactory
			.getLogger(AbstractSerializerTextMD.class);

	/**
	 * Instantiates a new AbstractSerializerTextMD.
	 *
	 * @param ocrProcessMetadata the ocr process metadata
	 */
	public AbstractSerializerTextMD(OCRProcessMetadata ocrProcessMetadata) {
		this.ocrProcessMetadata = ocrProcessMetadata;
	}

	
	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.metadata.SerializerTextMD#write(java.io.OutputStream)
	 */
	public void write(OutputStream outputstream) {
		opts.setSavePrettyPrint();
		opts.setSaveImplicitNamespaces(new HashMap<String, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put("", NAMESPACE);
			}
		});
		opts.setUseCDataBookmarks();
		opts.setUseDefaultNamespace();
		TextMDDocument textMDDoc = TextMDDocument.Factory.newInstance(opts);
		TextMD textMD = textMDDoc.addNewTextMD();

		Encoding encoding = textMD.addNewEncoding();
		encoding.addNewEncodingPlatform()
				.setLinebreak(
						noNamespace.TextMDDocument.TextMD.Encoding.EncodingPlatform.Linebreak.Enum
								.forString(ocrProcessMetadata.getLinebreak()));

		if (ocrProcessMetadata.getLinebreak() != null) {
			CharacterInfo characterInfo = textMD.addNewCharacterInfo();
			characterInfo.setLinebreak(Enum.forString(ocrProcessMetadata
					.getLinebreak()));
		}

		if (ocrProcessMetadata.getProcessingNote() != null) {
			textMD.addNewProcessingNote();
			textMD.setProcessingNoteArray(0,
					ocrProcessMetadata.getProcessingNote());
		}

		if (ocrProcessMetadata.getTextNote() != null) {
			textMD.addNewTextNote();
			textMD.setTextNoteArray(0, ocrProcessMetadata.getTextNote());
		}

		try {
			textMDDoc.save(outputstream, opts);
		} catch (IOException e) {
			logger.error("textMDDoc can not Creating, Missing textMD.xml for "
					+ " : ", e);
		}
	}

	/* (non-Javadoc)
	 * @see de.uni_goettingen.sub.commons.ocr.metadata.SerializerTextMD#write(java.io.File)
	 */
	public void write(File file) {
		try {
			OutputStream outputstream = new FileOutputStream(file);
			write(outputstream);
		} catch (FileNotFoundException e) {
			logger.error("textMDDoc can not Creating, Missing ...-textMD.xml for "
					+ file.getName() + " : ", e);
		}

	}

}
