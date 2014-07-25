package de.unigoettingen.sub.commons.ocr.util.abbyy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageMapper {

	private final static Map<Locale, String> LANGUAGE_MAP = new HashMap<Locale, String>();

	static {

		// See http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt 
		/**Technical contents of ISO 639:1988 (E/F)
		 * "Code for the representation of names of languages".
		 * The Registration Authority for ISO 639 is Infoterm, Osterreichisches
		 * Normungsinstitut (ON), Postfach 130, A-1021 Vienna, Austria.
		*/
		// only Abbyy Recognition Languages
		LANGUAGE_MAP.put(new Locale("ab"), "Abkhazian");	LANGUAGE_MAP.put(new Locale("az"), "Azerbaijani");
		LANGUAGE_MAP.put(new Locale("af"), "Afrikaans"); 	LANGUAGE_MAP.put(new Locale("am"), "Amharic");
		LANGUAGE_MAP.put(new Locale("ay"), "Aymara"); 	
		/*LANGUAGE_MAP.put(new Locale("ar"), "Arabic"); 		LANGUAGE_MAP.put(new Locale("as"), "Assamese");
		LANGUAGE_MAP.put(new Locale("aa"), "Afar"); */
		
		
		LANGUAGE_MAP.put(new Locale("ba"), "Bashkir"); 		LANGUAGE_MAP.put(new Locale("be"), "Byelorussian");
		LANGUAGE_MAP.put(new Locale("bg"), "Bulgarian");	LANGUAGE_MAP.put(new Locale("br"), "Breton");
		/*LANGUAGE_MAP.put(new Locale("bo"), "Tibetan"); 		LANGUAGE_MAP.put(new Locale("bh"), "Bihari");
		LANGUAGE_MAP.put(new Locale("bi"), "Bislama"); 		LANGUAGE_MAP.put(new Locale("bn"), "Bengali");*/
		
		LANGUAGE_MAP.put(new Locale("ca"), "Catalan"); 		LANGUAGE_MAP.put(new Locale("co"), "Corsican");
		LANGUAGE_MAP.put(new Locale("cs"), "Czech"); 		LANGUAGE_MAP.put(new Locale("cy"), "Welsh");
		
		LANGUAGE_MAP.put(new Locale("da"), "Danish"); 		LANGUAGE_MAP.put(Locale.GERMAN, "German");
		/*LANGUAGE_MAP.put(new Locale("dz"), "Bhutani");*/
		
		LANGUAGE_MAP.put(new Locale("el"), "Greek");		LANGUAGE_MAP.put(Locale.ENGLISH, "English");
		LANGUAGE_MAP.put(new Locale("es"), "Spanish");      LANGUAGE_MAP.put(new Locale("et"), "Estonian"); 	
		LANGUAGE_MAP.put(new Locale("eu"), "Basque");
		/*LANGUAGE_MAP.put(new Locale("eo"), "Esperanto");*/ 
		
		LANGUAGE_MAP.put(new Locale("fi"), "Finnish");      LANGUAGE_MAP.put(new Locale("fo"), "Faroese");
		LANGUAGE_MAP.put(new Locale("fr"), "French"); 		LANGUAGE_MAP.put(new Locale("fy"), "Frisian");
		/*LANGUAGE_MAP.put(new Locale("fa"), "Persian");  LANGUAGE_MAP.put(new Locale("fj"), "Fiji"); */
		
		LANGUAGE_MAP.put(new Locale("ga"), "Irish");		LANGUAGE_MAP.put(new Locale("gd"), "Scots Gaelic");
		LANGUAGE_MAP.put(new Locale("gl"), "Galician"); 	LANGUAGE_MAP.put(new Locale("gn"), "Guarani");
		LANGUAGE_MAP.put(new Locale("gu"), "Gujarati"); 	
		
		LANGUAGE_MAP.put(new Locale("ha"), "Hausa");		LANGUAGE_MAP.put(new Locale("he"), "Hebrew");
		LANGUAGE_MAP.put(new Locale("hr"), "Croatian");		LANGUAGE_MAP.put(new Locale("hy"), "Armenian");
		LANGUAGE_MAP.put(new Locale("hu"), "Hungarian"); 	
		/*LANGUAGE_MAP.put(new Locale("hi"), "Hindi"); */
		
		LANGUAGE_MAP.put(new Locale("id"), "Indonesian"); 	LANGUAGE_MAP.put(new Locale("it"), "Italian");
		/*LANGUAGE_MAP.put(new Locale("ie"), "Interlingue"); 	LANGUAGE_MAP.put(new Locale("ik"), "Inupiak");
		LANGUAGE_MAP.put(new Locale("is"), "Icelandic"); 	 LANGUAGE_MAP.put(new Locale("ia"), "Interlingua");
		LANGUAGE_MAP.put(new Locale("iu"), "Inuktitut");  */ 
		
		LANGUAGE_MAP.put(new Locale("ja"), "Japanese");		
		/*LANGUAGE_MAP.put(new Locale("jw"), "Javanese");*/
		
		LANGUAGE_MAP.put(new Locale("ko"), "Korean");		LANGUAGE_MAP.put(new Locale("ku"), "Kurdish");
		LANGUAGE_MAP.put(new Locale("ky"), "Kirghiz"); 		LANGUAGE_MAP.put(new Locale("kk"), "Kazakh");
		/*LANGUAGE_MAP.put(new Locale("ka"), "Georgian");		LANGUAGE_MAP.put(new Locale("kn"), "Kannada"); 		
		LANGUAGE_MAP.put(new Locale("kl"), "Greenlandic"); 	LANGUAGE_MAP.put(new Locale("km"), "Cambodian");
		LANGUAGE_MAP.put(new Locale("ks"), "Kashmiri");*/
		
		LANGUAGE_MAP.put(new Locale("la"), "Latin"); 		LANGUAGE_MAP.put(new Locale("lt"), "Lithuanian");	
		LANGUAGE_MAP.put(new Locale("lv"), "Latvian");
		/*LANGUAGE_MAP.put(new Locale("ln"), "Lingala");  LANGUAGE_MAP.put(new Locale("lo"), "Laothian");	*/
	 	
		
		LANGUAGE_MAP.put(new Locale("mg"), "Malagasy");		LANGUAGE_MAP.put(new Locale("mi"), "Maori");
		LANGUAGE_MAP.put(new Locale("mk"), "Macedonian"); 	LANGUAGE_MAP.put(new Locale("ms"), "Malay");	
		LANGUAGE_MAP.put(new Locale("mn"), "Mongolian"); 	LANGUAGE_MAP.put(new Locale("mo"), "Moldavian");
		LANGUAGE_MAP.put(new Locale("mt"), "Maltese"); 		
		/*LANGUAGE_MAP.put(new Locale("my"), "Burmese"); LANGUAGE_MAP.put(new Locale("ml"), "Malayalam");
		LANGUAGE_MAP.put(new Locale("mr"), "Marathi");
		*/
		
		LANGUAGE_MAP.put(new Locale("nl"), "Dutch"); 		LANGUAGE_MAP.put(new Locale("no"), "Norwegian");
		/*LANGUAGE_MAP.put(new Locale("na"), "Nauru");		LANGUAGE_MAP.put(new Locale("ne"), "Nepali");*/
	
			LANGUAGE_MAP.put(new Locale("oc"), "Occitan");		
		/*LANGUAGE_MAP.put(new Locale("om"), "Oromo");LANGUAGE_MAP.put(new Locale("or"), "Oriya"); */
		
		LANGUAGE_MAP.put(new Locale("pl"), "Polish");		LANGUAGE_MAP.put(new Locale("pt"), "Portuguese");
		/*LANGUAGE_MAP.put(new Locale("pa"), "Punjabi");	LANGUAGE_MAP.put(new Locale("ps"), "Pashto"); */	
		
		LANGUAGE_MAP.put(new Locale("qu"), "Quechua");
		
		LANGUAGE_MAP.put(new Locale("rm"),"Rhaeto-Romance");  LANGUAGE_MAP.put(new Locale("ru"), "Russian");
		LANGUAGE_MAP.put(new Locale("ro"), "Romanian");		
		/*LANGUAGE_MAP.put(new Locale("rw"), "Kinyarwanda"); LANGUAGE_MAP.put(new Locale("rn"), "Kirundi");*/
		
		LANGUAGE_MAP.put(new Locale("sk"), "Slovak");		LANGUAGE_MAP.put(new Locale("sv"), "Swedish");
		LANGUAGE_MAP.put(new Locale("sl"), "Slovenian");	LANGUAGE_MAP.put(new Locale("sm"), "Samoan");
		LANGUAGE_MAP.put(new Locale("sn"), "Shona"); 		LANGUAGE_MAP.put(new Locale("so"), "Somali");
		LANGUAGE_MAP.put(new Locale("sq"), "Albanian"); 	LANGUAGE_MAP.put(new Locale("sr"), "Serbian");
		LANGUAGE_MAP.put(new Locale("sw"), "Swahili");
		/*LANGUAGE_MAP.put(new Locale("sa"), "Sanskrit");		LANGUAGE_MAP.put(new Locale("sd"), "Sindhi");
		LANGUAGE_MAP.put(new Locale("sg"), "Sangho"); 		LANGUAGE_MAP.put(new Locale("sh"), "Serbo-Croatian");
		LANGUAGE_MAP.put(new Locale("si"), "Sinhalese");	LANGUAGE_MAP.put(new Locale("su"), "Sundanese");
		LANGUAGE_MAP.put(new Locale("ss"), "Siswati");		LANGUAGE_MAP.put(new Locale("st"), "Sesotho");*/
		
		
		
		LANGUAGE_MAP.put(new Locale("tg"), "Tajik"); 		LANGUAGE_MAP.put(new Locale("th"), "Thai");
		LANGUAGE_MAP.put(new Locale("tk"), "Turkmen");		LANGUAGE_MAP.put(new Locale("tl"), "Tagalog");	
		LANGUAGE_MAP.put(new Locale("to"), "Tonga"); 		LANGUAGE_MAP.put(new Locale("tr"), "Turkish");
		LANGUAGE_MAP.put(new Locale("tt"), "Tatar");
		/*LANGUAGE_MAP.put(new Locale("ts"), "Tsonga"); 		LANGUAGE_MAP.put(new Locale("ti"), "Tigrinya"); 	  		
		LANGUAGE_MAP.put(new Locale("tn"), "Setswana");		LANGUAGE_MAP.put(new Locale("tw"), "Twi");
		LANGUAGE_MAP.put(new Locale("ta"), "Tamil");		LANGUAGE_MAP.put(new Locale("te"), "Telugu");*/
	
		LANGUAGE_MAP.put(new Locale("ug"), "Uighur");		LANGUAGE_MAP.put(new Locale("uk"), "Ukrainian");
		LANGUAGE_MAP.put(new Locale("uz"), "Uzbek");
		/*LANGUAGE_MAP.put(new Locale("ur"), "Urdu");*/ 	
		
		/*LANGUAGE_MAP.put(new Locale("vi"), "Vietnamese"); 	LANGUAGE_MAP.put(new Locale("vo"), "Volapuk");*/
		
		LANGUAGE_MAP.put(new Locale("wo"), "Wolof");
		
		LANGUAGE_MAP.put(new Locale("xh"), "Xhosa");
		
		LANGUAGE_MAP.put(new Locale("yi"), "Yiddish"); 		
		/*LANGUAGE_MAP.put(new Locale("yo"), "Yoruba");*/
		
		LANGUAGE_MAP.put(new Locale("zu"), "Zulu");
		LANGUAGE_MAP.put(new Locale("zh"), "Chinese");
		/*LANGUAGE_MAP.put(new Locale("za"), "Zhuang");	*/

	}
	public static String getAbbyyNotation(Locale locale) {
		return LANGUAGE_MAP.get(locale);
	}
	
}
