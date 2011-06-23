package de.unigoettingen.sub.commons.ocrComponents.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;



@XmlType(name = "recognitionLanguage")
@XmlEnum
public enum RecognitionLanguage {

//    //    @XmlEnumValue("Bulgarian")
    BULGARIAN("Bulgarian"),
//    //    @XmlEnumValue("Catalan")
    CATALAN("Catalan"),
//    //    @XmlEnumValue("Czech")
    CZECH("Czech"),
//    //    @XmlEnumValue("Danish")
    DANISH("Danish"),
//    //    @XmlEnumValue("Dutch")
    DUTCH("Dutch"),
//    //    @XmlEnumValue("English")
    ENGLISH("English"),
//    //    @XmlEnumValue("Finnish")
    FINNISH("Finnish"),
//    //    @XmlEnumValue("French")
    FRENCH("French"),
//   //    @XmlEnumValue("German")
    GERMAN("German"),
//    //    @XmlEnumValue("Greek")
    GREEK("Greek"),
//    //    @XmlEnumValue("Hungarian")
    HUNGARIAN("Hungarian"),
//    //    @XmlEnumValue("Irish")
    IRISH("Irish"),
    //    //    @XmlEnumValue("Italian")
    ITALIAN("Italian"),
    //    //    @XmlEnumValue("Macedonian")
    MACEDONIAN("Macedonian"),
    //    //    @XmlEnumValue("gd")
    SCOTS_GAELIC("gd"),
    //    //    @XmlEnumValue("Abkhazian")
    ABKHAZIAN("Abkhazian"),
    //    //    @XmlEnumValue("Afrikaans")
    AFRIKANNS("Afrikaans"),
    //    //    @XmlEnumValue("Amharic")
    AMHARIC("Amharic"),
    //    //    @XmlEnumValue("Aymara")
    AYMARA("Aymara"),
    //    //    @XmlEnumValue("Azerbaijani")
    AZERBAIJANI("Azerbaijani"),
    //    //    @XmlEnumValue("Portuguese")
    PORTUGUESE("Portuguese"),
    //    //    @XmlEnumValue("Romanian")
    ROMANIAN("Romanian"),
    //    //    @XmlEnumValue("Russian")
    RUSSIAN("Russian"),
    //    //    @XmlEnumValue("Serbian")
    SERBIAN("Serbian"),
    //    //    @XmlEnumValue("Somali")
    SOMALI("Somali"),
    //    //    @XmlEnumValue("Slovenian")
    SLOVENIAN("Slovenian"),
    //    @XmlEnumValue("Slovak")
    SLOVAK("Slovak"),
    //    @XmlEnumValue("Spanish")
    SPANISH("Spanish"),
    //    @XmlEnumValue("Bashkir")
    BASHKIR("Bashkir"),
    //    @XmlEnumValue("Byelorussian")
    BYELORUSSIAN("Byelorussian"),
    //    @XmlEnumValue("Breton")
    BRETON("Breton"),
    //    @XmlEnumValue("Corsican")
    CORSICAN("Corsican"),
    //    @XmlEnumValue("Welsh")
    WELSH("Welsh"),
    //    @XmlEnumValue("Basque")
    BASQUE("Basque"),
    //    @XmlEnumValue("Estonian")
    ESTONIAN("Estonian"),
    //    @XmlEnumValue("Faroese")
    FAROESE("Faroese"),
    //    @XmlEnumValue("Frisian")
    FRISIAN("Frisian"),
    //    @XmlEnumValue("Galician")
    GALICIAN("Galician"),
    //    @XmlEnumValue("Guarani")
    GUARANI("Guarani"),
    //    @XmlEnumValue("Gujarati")
    GUJARATI("Gujarati"),
    //    @XmlEnumValue("Hausa")
    HAUSA("Hausa"),
    //    @XmlEnumValue("Hebrew")
    HEBREW("Hebrew"),
    //    @XmlEnumValue("Croatian")
    CROATIAN("Croatian"),
    //    @XmlEnumValue("Armenian")
    ARMENIAN("Armenian"),
    //    @XmlEnumValue("Indonesian")
    INDONESIAN("Indonesian"),
    //    @XmlEnumValue("Korean")
    KOREAN("Korean"),
    //    @XmlEnumValue("Kurdish")
    KURDISH("Kurdish"),
    //    @XmlEnumValue("Kirghiz")
    KIRGHIZ("Kirghiz"),
    //    @XmlEnumValue("Kazakh")
    KAZAKH("Kazakh"),
    //    @XmlEnumValue("Latin")
    LATIN("Latin"),
    //    @XmlEnumValue("Lithuanian")
    LITHUANIAN("Lithuanian"),
    //    @XmlEnumValue("Latvian")
    LATVIAN("Latvian"),
    //    @XmlEnumValue("Malagasy")
    MALAGASY("Malagasy"),
    //    @XmlEnumValue("Maori")
    MAORI("Maori"),
    //    @XmlEnumValue("Malay")
    MALAY("Malay"),
    //    @XmlEnumValue("Mongolian")
    MONGOLIAN("Mongolian"),
    //    @XmlEnumValue("Moldavian")
    MOLDAVIAN("Moldavian"),
    //    @XmlEnumValue("Maltese")
    MALTESE("Maltese"),
    //    @XmlEnumValue("Norwegian")
    NORWEGIAN("Norwegian"),
    //    @XmlEnumValue("Occitan")
    OCCITAN("Occitan"),
    //    @XmlEnumValue("Polish")
    POLISH("Polish"),
    //    @XmlEnumValue("Quechua")
    QUECHUA("Quechua"),
    //    @XmlEnumValue("Swedish")
    SWEDISH("Swedish"),
    //    @XmlEnumValue("Samoan")
    SAMOAN("Samoan"),
    //    @XmlEnumValue("Shona")
    SHONA("Shona"),
    //    @XmlEnumValue("Albanian")
    ALBANIAN("Albanian"),
    //    @XmlEnumValue("Swahili")
    SWAHILI("Swahili"),
    //    @XmlEnumValue("Tajik")
    TAJIK("Tajik"),
    //    @XmlEnumValue("Thai")
    THAI("Thai"),
    //    @XmlEnumValue("Turkmen")
    TURKMEN("Turkmen"),
    //    @XmlEnumValue("Tagalog")
    TAGALOG("Tagalog"),
    //    @XmlEnumValue("Tonga")
    TONGA("Tonga"),
    //    @XmlEnumValue("Turkish")
    TURKISH("Turkish"),
    //    @XmlEnumValue("Tatar")
    TATAR("Tatar"),
    //    @XmlEnumValue("Uighur")
    UIGHUR("Uighur"),
    //    @XmlEnumValue("Ukrainian")
    UKRAINIAN("Ukrainian"),
    //    @XmlEnumValue("Uzbek")
    UZBEK("Uzbek"),
    //    @XmlEnumValue("Wolof")
    WOLOF("Wolof"),
    //    @XmlEnumValue("Xhosa")
    XHOSA("Xhosa"),
    //    @XmlEnumValue("Yiddish")
    YIDDISH("Yiddish"),
    //    @XmlEnumValue("Zulu")
    ZULU("Zulu"),
    //    @XmlEnumValue("Chinese")
    CHINESE("Chinese"),
    //    @XmlEnumValue("rm")
    RHARTO_ROMANCE("rm"),
    
    JAPANESE("ja");
    
    private final String value;

    RecognitionLanguage(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RecognitionLanguage fromValue(String v) {
        for (RecognitionLanguage c: RecognitionLanguage.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
