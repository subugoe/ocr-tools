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
import noNamespace.TextMDDocument.TextMD.AltLanguage;
import noNamespace.TextMDDocument.TextMD.CharacterInfo;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.ByteSize;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.CharacterSize;
import noNamespace.TextMDDocument.TextMD.Encoding;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.Linebreak.Enum;
import noNamespace.TextMDDocument.TextMD.Encoding.EncodingAgent;
import noNamespace.TextMDDocument.TextMD.Encoding.EncodingPlatform;
import noNamespace.TextMDDocument.TextMD.Encoding.EncodingSoftware;
import noNamespace.TextMDDocument.TextMD.Language;
import noNamespace.TextMDDocument.TextMD.MarkupBasis;
import noNamespace.TextMDDocument.TextMD.MarkupLanguage;

import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

/**
 * The Class AbstractSerializerTextMD is a abstract super class for
 * {@link SerializerTextMD} implementations. {@link SerializerTextMD}, there are
 * two methods which can provide textMD
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

	// ByteSize in element character_info
	ByteSize bs;
	CharacterSize cs;

	/**
	 * Instantiates a new AbstractSerializerTextMD.
	 * 
	 * @param ocrProcessMetadata
	 *            the ocr process metadata
	 */
	public AbstractSerializerTextMD(OCRProcessMetadata ocrProcessMetadata) {
		this.ocrProcessMetadata = ocrProcessMetadata;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.metadata.SerializerTextMD#write(java
	 * .io.OutputStream)
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

		// CharacterInfo
		CharacterInfo characterInfo = textMD.addNewCharacterInfo();

		// byte_order
		characterInfo
				.setByteOrder(noNamespace.TextMDDocument.TextMD.CharacterInfo.ByteOrder.LITTLE);

		// TODO byte_size
		// bs.setBigDecimalValue(new BigDecimal(8));
		// characterInfo.setByteSize((ByteSize) bs.getBigDecimalValue());

		// TODO characterInfo character_size
		// cs.setStringValue("variable");
		// CharacterSize characterSize =
		// characterInfo.setCharacterSize(CharacterSize arg);

		if (ocrProcessMetadata.getEncoding() != null
				&& !ocrProcessMetadata.getEncoding().equals(" ")) {
			characterInfo
					.setCharset(noNamespace.TextMDDocument.TextMD.CharacterInfo.Charset.Enum
							.forString(ocrProcessMetadata.getEncoding()));
			/*
			 * if (ocrProcessMetadata.getEncoding().equals("UTF-8")) {
			 * CharacterInfo.CharacterSize.Factory.newInstance(); }
			 */

		}

		// Encoding
		if (ocrProcessMetadata.getLinebreak() != null
				&& !ocrProcessMetadata.getLinebreak().equals("")) {
			Encoding encoding = textMD.addNewEncoding();
			// QUALITY
			encoding.setQUALITY("good");
			// encoding_platform
			EncodingPlatform encoding_platform = encoding
					.addNewEncodingPlatform();
			encoding_platform
					.setLinebreak(noNamespace.TextMDDocument.TextMD.Encoding.EncodingPlatform.Linebreak.Enum
							.forString(ocrProcessMetadata.getLinebreak()));
			encoding_platform
					.setStringValue("Apple iMac, 2.33 GHz Intel Core 2 Duo, 2 GB 667 MHz DDR2 SDRAM, Mac OS X Version 10.4.11, Phase One P45 digital back, Contax 645 camera.");

			// encoding_software
			EncodingSoftware encodingsoftware = encoding
					.addNewEncodingSoftware();
			encodingsoftware.setVersion("0.1.1");
			encodingsoftware.setStringValue("OCRopus");

			// encoding_agent
			EncodingAgent encodingagent = encoding.addNewEncodingAgent();
			encodingagent
					.setRole(noNamespace.TextMDDocument.TextMD.Encoding.EncodingAgent.Role.Enum
							.forString("MARKUP"));
			encodingagent.setStringValue("John Q. Doe");

			// characterInfo Linebreak
			characterInfo.setLinebreak(Enum.forString(ocrProcessMetadata
					.getLinebreak()));
			// TODO characterInfo character_size-encoding
			// characterSize.setEncoding(ocrProcessMetadata.getEncoding());

		}
		// Languages
		Language language = textMD.addNewLanguage();
		language.set(noNamespace.TextMDDocument.TextMD.Language.Enum
				.forString("alb/sqi"));
		/*
		 * if (ocrProcessMetadata.getLanguages() != null){ for (Locale l :
		 * ocrProcessMetadata.getLanguages()){ Language language =
		 * textMD.addNewLanguage(); language.set("BAN"); }
		 * 
		 * }
		 */

		// AltLanguage
		AltLanguage altlanguage = textMD.addNewAltLanguage();
		altlanguage.setAuthority("ethnologue");
		altlanguage.setStringValue("als");

		// font_script
		textMD.addFontScript("Monaco");

		// markup_basis
		MarkupBasis markupBasis = textMD.addNewMarkupBasis();
		markupBasis.setVersion("1.0");
		markupBasis.setStringValue("XML");

		// markup_language
		MarkupLanguage markupLanguage = textMD.addNewMarkupLanguage();
		markupLanguage.setVersion("P5");
		markupLanguage
				.setStringValue("http://memory.loc.gov/natlib/cred/tei_p5/tei_allPlus.xsd");

		// processingNote
		if (ocrProcessMetadata.getProcessingNote() != null
				&& !ocrProcessMetadata.getProcessingNote().equals("")) {
			textMD.addNewProcessingNote();
			textMD.setProcessingNoteArray(0,
					ocrProcessMetadata.getProcessingNote());
		}

		// printRequirements
		textMD.addPrintRequirements("special requirements for printing the item");

		// viewingRequirements
		textMD.addViewingRequirements("Any special hardware or software requirements for viewing the item");

		// TextNote
		if (ocrProcessMetadata.getTextNote() != null
				&& !ocrProcessMetadata.getTextNote().equals("")) {
			textMD.addNewTextNote();
			textMD.setTextNoteArray(0, ocrProcessMetadata.getTextNote());
		}

		// TODO
		// pageOrder not exists
		// representationSequence not exists
		// lineOrientation not exists
		// lineLayout not exists
		// characterFlow not exists

		try {
			textMDDoc.save(outputstream, opts);
		} catch (IOException e) {
			logger.error("textMDDoc can not Creating, Missing textMD.xml for "
					+ " : ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.metadata.SerializerTextMD#write(java
	 * .io.File)
	 */
	public void write(File file) {
		try {
			OutputStream outputstream = new FileOutputStream(file);
			write(outputstream);
		} catch (FileNotFoundException e) {
			logger.error(
					"textMDDoc can not Creating, Missing ...-textMD.xml for "
							+ file.getName() + " : ", e);
		}

	}

}
