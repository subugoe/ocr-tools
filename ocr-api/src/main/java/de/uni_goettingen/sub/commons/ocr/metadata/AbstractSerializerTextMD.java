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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import noNamespace.TextMDDocument;
import noNamespace.TextMDDocument.TextMD;
import noNamespace.TextMDDocument.TextMD.CharacterInfo;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.ByteSize;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.CharacterSize;
import noNamespace.TextMDDocument.TextMD.Encoding;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.Linebreak.Enum;
import noNamespace.TextMDDocument.TextMD.Encoding.EncodingPlatform;
import noNamespace.TextMDDocument.TextMD.Encoding.EncodingSoftware;
import noNamespace.TextMDDocument.TextMD.Language;
import noNamespace.TextMDDocument.TextMD.MarkupBasis;

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
	/**
	 * This Map contains the mapping from java.util.Locale to the Strings needed
	 * by TextMD
	 */
	public final static Map<Locale, String> LANGUAGE_MAP = new HashMap<Locale, String>();

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

	static {
		/**
		 * Language(s) used in work. Use ISO 639-2 codes
		 */
		// only Abbyy Recognition Languages
		LANGUAGE_MAP.put(new Locale("ab"), "abk");
		LANGUAGE_MAP.put(new Locale("az"), "aze");
		LANGUAGE_MAP.put(new Locale("af"), "afr");
		LANGUAGE_MAP.put(new Locale("am"), "amh");
		LANGUAGE_MAP.put(new Locale("ay"), "aym");
		/*
		 * LANGUAGE_MAP.put(new Locale("ar"), "ara"); LANGUAGE_MAP.put(new
		 * Locale("as"), "asm"); LANGUAGE_MAP.put(new Locale("aa"), "aar");
		 */

		LANGUAGE_MAP.put(new Locale("ba"), "bak");
		LANGUAGE_MAP.put(new Locale("be"), "bel");
		LANGUAGE_MAP.put(new Locale("bg"), "bul");
		LANGUAGE_MAP.put(new Locale("br"), "bre");
		/*
		 * LANGUAGE_MAP.put(new Locale("bo"), "tib/bod"); LANGUAGE_MAP.put(new
		 * Locale("bh"), "bih"); LANGUAGE_MAP.put(new Locale("bi"), "bis");
		 * LANGUAGE_MAP.put(new Locale("bn"), "ben");
		 */

		LANGUAGE_MAP.put(new Locale("ca"), "cat");
		LANGUAGE_MAP.put(new Locale("co"), "cos");
		LANGUAGE_MAP.put(new Locale("cs"), "cze/ces");
		LANGUAGE_MAP.put(new Locale("cy"), "wel/cym");

		LANGUAGE_MAP.put(new Locale("da"), "dan");
		LANGUAGE_MAP.put(Locale.GERMAN, "ger/deu");
		/* LANGUAGE_MAP.put(new Locale("dz"), "Bhutani"); */

		LANGUAGE_MAP.put(new Locale("el"), "gre/ell");
		LANGUAGE_MAP.put(Locale.ENGLISH, "eng");
		LANGUAGE_MAP.put(new Locale("es"), "spa");
		LANGUAGE_MAP.put(new Locale("et"), "est");
		LANGUAGE_MAP.put(new Locale("eu"), "Basque");
		/* LANGUAGE_MAP.put(new Locale("eo"), "epo"); */

		LANGUAGE_MAP.put(new Locale("fi"), "fin");
		LANGUAGE_MAP.put(new Locale("fo"), "fao");
		LANGUAGE_MAP.put(new Locale("fr"), "fre/fra");
		LANGUAGE_MAP.put(new Locale("fy"), "fry");
		/*
		 * LANGUAGE_MAP.put(new Locale("fa"), "per/fas"); LANGUAGE_MAP.put(new
		 * Locale("fj"), "fij");
		 */

		LANGUAGE_MAP.put(new Locale("ga"), "gle");
		LANGUAGE_MAP.put(new Locale("gd"), "gla");
		LANGUAGE_MAP.put(new Locale("gl"), "glg ");
		LANGUAGE_MAP.put(new Locale("gn"), "grn");
		LANGUAGE_MAP.put(new Locale("gu"), "guj");

		LANGUAGE_MAP.put(new Locale("ha"), "hau");
		LANGUAGE_MAP.put(new Locale("he"), "heb");
		LANGUAGE_MAP.put(new Locale("hr"), "scr/hrv");
		LANGUAGE_MAP.put(new Locale("hy"), "arm/hye");
		LANGUAGE_MAP.put(new Locale("hu"), "Hungarian");
		/* LANGUAGE_MAP.put(new Locale("hi"), "hin"); */

		LANGUAGE_MAP.put(new Locale("id"), "ind");
		LANGUAGE_MAP.put(new Locale("it"), "ita");
		/*
		 * LANGUAGE_MAP.put(new Locale("ie"), "ile"); LANGUAGE_MAP.put(new
		 * Locale("ik"), "ipk"); LANGUAGE_MAP.put(new Locale("is"), "ice/isl");
		 * LANGUAGE_MAP.put(new Locale("ia"), "ina"); LANGUAGE_MAP.put(new
		 * Locale("iu"), "iku");
		 */

		LANGUAGE_MAP.put(new Locale("ja"), "jpn");
		/* LANGUAGE_MAP.put(new Locale("jw"), "jav"); */

		LANGUAGE_MAP.put(new Locale("ko"), "kor");
		LANGUAGE_MAP.put(new Locale("ku"), "kur");
		LANGUAGE_MAP.put(new Locale("ky"), "kir");
		LANGUAGE_MAP.put(new Locale("kk"), "kaz");
		/*
		 * LANGUAGE_MAP.put(new Locale("ka"), "geo/kat"); LANGUAGE_MAP.put(new
		 * Locale("kn"), "kan"); LANGUAGE_MAP.put(new Locale("km"),
		 * "Cambodian"); LANGUAGE_MAP.put(new Locale("ks"), "kas");
		 */

		LANGUAGE_MAP.put(new Locale("la"), "lat");
		LANGUAGE_MAP.put(new Locale("lt"), "lit");
		LANGUAGE_MAP.put(new Locale("lv"), "lav");
		/*
		 * LANGUAGE_MAP.put(new Locale("ln"), "lin"); LANGUAGE_MAP.put(new
		 * Locale("lo"), "lao");
		 */

		LANGUAGE_MAP.put(new Locale("mg"), "mlg");
		LANGUAGE_MAP.put(new Locale("mi"), "mao/mri");
		LANGUAGE_MAP.put(new Locale("mk"), "mac/mkd");
		LANGUAGE_MAP.put(new Locale("ms"), "may/msa");
		LANGUAGE_MAP.put(new Locale("mn"), "mon");
		LANGUAGE_MAP.put(new Locale("mo"), "mol");
		LANGUAGE_MAP.put(new Locale("mt"), "mlt");
		/*
		 * LANGUAGE_MAP.put(new Locale("my"), "bur/mya"); LANGUAGE_MAP.put(new
		 * Locale("ml"), "mal"); LANGUAGE_MAP.put(new Locale("mr"), "mar");
		 */

		LANGUAGE_MAP.put(new Locale("nl"), "dut/nld");
		LANGUAGE_MAP.put(new Locale("no"), "nor");
		/*
		 * LANGUAGE_MAP.put(new Locale("na"), "nau"); LANGUAGE_MAP.put(new
		 * Locale("ne"), "nep");
		 */

		LANGUAGE_MAP.put(new Locale("oc"), "oci");
		/*
		 * LANGUAGE_MAP.put(new Locale("om"), "orm");LANGUAGE_MAP.put(new
		 * Locale("or"), "ori");
		 */

		LANGUAGE_MAP.put(new Locale("pl"), "pol");
		LANGUAGE_MAP.put(new Locale("pt"), "por");
		/*
		 * LANGUAGE_MAP.put(new Locale("pa"), "Punjabi"); LANGUAGE_MAP.put(new
		 * Locale("ps"), "Pashto");
		 */

		LANGUAGE_MAP.put(new Locale("qu"), "que");

		LANGUAGE_MAP.put(new Locale("rm"), "roh");
		LANGUAGE_MAP.put(new Locale("ru"), "rus");
		LANGUAGE_MAP.put(new Locale("ro"), "rum/ron");
		/*
		 * LANGUAGE_MAP.put(new Locale("rw"), "kin"); LANGUAGE_MAP.put(new
		 * Locale("rn"), "Kirundi");
		 */

		LANGUAGE_MAP.put(new Locale("sk"), "slo/slk");
		LANGUAGE_MAP.put(new Locale("sv"), "swe");
		LANGUAGE_MAP.put(new Locale("sl"), "slv");
		LANGUAGE_MAP.put(new Locale("sm"), "smo");
		LANGUAGE_MAP.put(new Locale("sn"), "sna");
		LANGUAGE_MAP.put(new Locale("so"), "som");
		LANGUAGE_MAP.put(new Locale("sq"), "alb/sqi");
		LANGUAGE_MAP.put(new Locale("sr"), "scc/srp");
		LANGUAGE_MAP.put(new Locale("sw"), "swa");
		/*
		 * LANGUAGE_MAP.put(new Locale("sa"), "san"); LANGUAGE_MAP.put(new
		 * Locale("sd"), "snd"); LANGUAGE_MAP.put(new Locale("sg"), "sag");
		 * LANGUAGE_MAP.put(new Locale("sh"), "Serbo-Croatian");
		 * LANGUAGE_MAP.put(new Locale("si"), "sin"); LANGUAGE_MAP.put(new
		 * Locale("su"), "sun"); LANGUAGE_MAP.put(new Locale("ss"), "Siswati");
		 * LANGUAGE_MAP.put(new Locale("st"), "Sesotho");
		 */

		LANGUAGE_MAP.put(new Locale("tg"), "tgk");
		LANGUAGE_MAP.put(new Locale("th"), "tha");
		LANGUAGE_MAP.put(new Locale("tk"), "tuk");
		LANGUAGE_MAP.put(new Locale("tl"), "tgl");
		LANGUAGE_MAP.put(new Locale("to"), "tog");
		LANGUAGE_MAP.put(new Locale("tr"), "tur");
		LANGUAGE_MAP.put(new Locale("tt"), "Tatar");
		/*
		 * LANGUAGE_MAP.put(new Locale("ts"), "tso"); LANGUAGE_MAP.put(new
		 * Locale("ti"), "tir"); LANGUAGE_MAP.put(new Locale("tn"), "Setswana");
		 * LANGUAGE_MAP.put(new Locale("tw"), "twi"); LANGUAGE_MAP.put(new
		 * Locale("ta"), "tam"); LANGUAGE_MAP.put(new Locale("te"), "tel");
		 */

		LANGUAGE_MAP.put(new Locale("ug"), "uig");
		LANGUAGE_MAP.put(new Locale("uk"), "ukr");
		LANGUAGE_MAP.put(new Locale("uz"), "uzb");
		/* LANGUAGE_MAP.put(new Locale("ur"), "urd"); */

		/*
		 * LANGUAGE_MAP.put(new Locale("vi"), "vie"); LANGUAGE_MAP.put(new
		 * Locale("vo"), "vol");
		 */

		LANGUAGE_MAP.put(new Locale("wo"), "wol");

		LANGUAGE_MAP.put(new Locale("xh"), "xho");

		LANGUAGE_MAP.put(new Locale("yi"), "yid");
		/* LANGUAGE_MAP.put(new Locale("yo"), "yor"); */

		LANGUAGE_MAP.put(new Locale("zu"), "zul");
		LANGUAGE_MAP.put(new Locale("zh"), "chi/zho");
		/* LANGUAGE_MAP.put(new Locale("za"), "zha"); */
		
		// TODO attribute  xsi:schemaLocation
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
		
	//	opts.setUseCDataBookmarks();
		opts.setUseDefaultNamespace();
	
	}
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
		
		TextMDDocument textMDDoc = TextMDDocument.Factory.newInstance(opts);
		TextMD textMD = textMDDoc.addNewTextMD();

		// CharacterInfo
		CharacterInfo characterInfo = textMD.addNewCharacterInfo();

		// byte_order
		/*characterInfo
				.setByteOrder(noNamespace.TextMDDocument.TextMD.CharacterInfo.ByteOrder.LITTLE);
*/
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

		Encoding encoding = textMD.addNewEncoding();
		// QUALITY
//		encoding.setQUALITY("good");
		
		// encoding_platform
		EncodingPlatform encodingPlatform = encoding.addNewEncodingPlatform();

		if (ocrProcessMetadata.getLinebreak() != null
				&& !ocrProcessMetadata.getLinebreak().equals("")) {
			encodingPlatform
					.setLinebreak(noNamespace.TextMDDocument.TextMD.Encoding.EncodingPlatform.Linebreak.Enum
							.forString(ocrProcessMetadata.getLinebreak()));
		}

//		encoding_platform.setStringValue("Apple iMac, 2.33 GHz Intel Core 2 Duo, 2 GB 667 MHz DDR2 SDRAM, Mac OS X Version 10.4.11, Phase One P45 digital back, Contax 645 camera.");

		// encoding_software
		EncodingSoftware encodingsoftware = encoding.addNewEncodingSoftware();
		encodingsoftware.setVersion(ocrProcessMetadata.getSoftwareVersion());
		encodingsoftware.setStringValue(ocrProcessMetadata.getSoftwareName());

		// encoding_agent
		/*EncodingAgent encodingagent = encoding.addNewEncodingAgent();
		encodingagent
				.setRole(noNamespace.TextMDDocument.TextMD.Encoding.EncodingAgent.Role.Enum
						.forString("MARKUP"));
		encodingagent.setStringValue("John Q. Doe");*/

		// characterInfo Linebreak
		if (ocrProcessMetadata.getLinebreak() != null
				&& !ocrProcessMetadata.getLinebreak().equals("")) {
			characterInfo.setLinebreak(Enum.forString(ocrProcessMetadata
					.getLinebreak()));
		}
		// TODO characterInfo character_size-encoding
		// characterSize.setEncoding(ocrProcessMetadata.getEncoding());

		// Languages
		/*
		 * Language language = textMD.addNewLanguage();
		 * language.set(noNamespace.TextMDDocument.TextMD.Language.Enum
		 * .forString("alb/sqi"));
		 */

		if (ocrProcessMetadata.getLanguages() != null) {
			for (Locale l : ocrProcessMetadata.getLanguages()) {
				Language language = textMD.addNewLanguage();
				language.set(noNamespace.TextMDDocument.TextMD.Language.Enum
						.forString(LANGUAGE_MAP.get(l)));
			}

		}

		// AltLanguage
		/*AltLanguage altlanguage = textMD.addNewAltLanguage();
		altlanguage.setAuthority("ethnologue");
		altlanguage.setStringValue("als");
*/
		// font_script
//		textMD.addFontScript("Monaco");

		// markup_basis
		if (ocrProcessMetadata.getFormat() != null) {
			if (ocrProcessMetadata.getFormat().contains(" ")) {
				for (String format : Arrays.asList(ocrProcessMetadata
						.getFormat().split(" "))) {
					MarkupBasis markupBasis = textMD.addNewMarkupBasis();
					if (format.equals("XML")) {
						markupBasis.setVersion("1.0");
					}
					markupBasis.setStringValue(format);
				}
			}
		}

		// markup_language
		/*MarkupLanguage markupLanguage = textMD.addNewMarkupLanguage();
		markupLanguage.setVersion("P5");
		markupLanguage
				.setStringValue("http://memory.loc.gov/natlib/cred/tei_p5/tei_allPlus.xsd");
*/
		// processingNote
		if (ocrProcessMetadata.getProcessingNote() != null
				&& !ocrProcessMetadata.getProcessingNote().equals("")) {
			textMD.addNewProcessingNote();
			textMD.setProcessingNoteArray(0,
					ocrProcessMetadata.getProcessingNote());
		}

		// printRequirements
//		textMD.addPrintRequirements("special requirements for printing the item");

		// viewingRequirements
//		textMD.addViewingRequirements("Any special hardware or software requirements for viewing the item");

		// TextNote
		/*if (ocrProcessMetadata.getTextNote() != null
				&& !ocrProcessMetadata.getTextNote().equals("")) {
			textMD.addNewTextNote();
			textMD.setTextNoteArray(0, ocrProcessMetadata.getTextNote());
		}*/

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
