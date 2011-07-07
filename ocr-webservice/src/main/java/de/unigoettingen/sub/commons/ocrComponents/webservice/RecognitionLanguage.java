package de.unigoettingen.sub.commons.ocrComponents.webservice;
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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;



@XmlType(name = "recognitionLanguage")
@XmlEnum
public enum RecognitionLanguage {

//    //    @XmlEnumValue("Bulgarian")
    bg("Bulgarian"),
//    //    @XmlEnumValue("Catalan")
    ca("Catalan"),
//    //    @XmlEnumValue("Czech")
    cs("Czech"),
//    //    @XmlEnumValue("Danish")
    da("Danish"),
//    //    @XmlEnumValue("Dutch")
    nl("Dutch"),
//    //    @XmlEnumValue("English")
    en("English"),
//    //    @XmlEnumValue("Finnish")
    fi("Finnish"),
//    //    @XmlEnumValue("French")
    fr("French"),
//   //    @XmlEnumValue("German")
    de("German"),
//    //    @XmlEnumValue("Greek")
    el("Greek"),
//    //    @XmlEnumValue("Hungarian")
    hu("Hungarian"),
//    //    @XmlEnumValue("Irish")
    ga("Irish"),
    //    //    @XmlEnumValue("Italian")
    it("Italian"),
    //    //    @XmlEnumValue("Macedonian")
    mk("Macedonian"),
    //    //    @XmlEnumValue("gd")
    gd("scots_gaelic"),
    //    //    @XmlEnumValue("Abkhazian")
    ab("Abkhazian"),
    //    //    @XmlEnumValue("Afrikaans")
    af("Afrikaans"),
    //    //    @XmlEnumValue("Amharic")
    am("Amharic"),
    //    //    @XmlEnumValue("Aymara")
    ay("Aymara"),
    //    //    @XmlEnumValue("Azerbaijani")
    az("Azerbaijani"),
    //    //    @XmlEnumValue("Portuguese")
    pt("Portuguese"),
    //    //    @XmlEnumValue("Romanian")
    ro("Romanian"),
    //    //    @XmlEnumValue("Russian")
    ru("Russian"),
    //    //    @XmlEnumValue("Serbian")
    sr("Serbian"),
    //    //    @XmlEnumValue("Somali")
    so("Somali"),
    //    //    @XmlEnumValue("Slovenian")
    sl("Slovenian"),
    //    @XmlEnumValue("Slovak")
    sk("Slovak"),
    //    @XmlEnumValue("Spanish")
    es("Spanish"),
    //    @XmlEnumValue("Bashkir")
    ba("Bashkir"),
    //    @XmlEnumValue("Byelorussian")
    be("Byelorussian"),
    //    @XmlEnumValue("Breton")
    br("Breton"),
    //    @XmlEnumValue("Corsican")
    co("Corsican"),
    //    @XmlEnumValue("Welsh")
    cy("Welsh"),
    //    @XmlEnumValue("Basque")
    eu("Basque"),
    //    @XmlEnumValue("Estonian")
    et("Estonian"),
    //    @XmlEnumValue("Faroese")
    fo("Faroese"),
    //    @XmlEnumValue("Frisian")
    fy("Frisian"),
    //    @XmlEnumValue("Galician")
    gl("Galician"),
    //    @XmlEnumValue("Guarani")
    gn("Guarani"),
    //    @XmlEnumValue("Gujarati")
    gu("Gujarati"),
    //    @XmlEnumValue("Hausa")
    ha("Hausa"),
    //    @XmlEnumValue("Hebrew")
    he("Hebrew"),
    //    @XmlEnumValue("Croatian")
    hr("Croatian"),
    //    @XmlEnumValue("Armenian")
    hy("Armenian"),
    //    @XmlEnumValue("Indonesian")
    id("Indonesian"),
    //    @XmlEnumValue("Korean")
    ko("Korean"),
    //    @XmlEnumValue("Kurdish")
    ku("Kurdish"),
    //    @XmlEnumValue("Kirghiz")
    ky("Kirghiz"),
    //    @XmlEnumValue("Kazakh")
    kk("Kazakh"),
    //    @XmlEnumValue("Latin")
    la("Latin"),
    //    @XmlEnumValue("Lithuanian")
    lt("Lithuanian"),
    //    @XmlEnumValue("Latvian")
    lv("Latvian"),
    //    @XmlEnumValue("Malagasy")
    mg("Malagasy"),
    //    @XmlEnumValue("Maori")
    mi("Maori"),
    //    @XmlEnumValue("Malay")
    ms("Malay"),
    //    @XmlEnumValue("Mongolian")
    mn("Mongolian"),
    //    @XmlEnumValue("Moldavian")
    mo("Moldavian"),
    //    @XmlEnumValue("Maltese")
    mt("Maltese"),
    //    @XmlEnumValue("Norwegian")
    no("Norwegian"),
    //    @XmlEnumValue("Occitan")
    oc("Occitan"),
    //    @XmlEnumValue("Polish")
    pl("Polish"),
    //    @XmlEnumValue("Quechua")
    qu("Quechua"),
    //    @XmlEnumValue("Swedish")
    sv("Swedish"),
    //    @XmlEnumValue("Samoan")
    sm("Samoan"),
    //    @XmlEnumValue("Shona")
    sn("Shona"),
    //    @XmlEnumValue("Albanian")
    sq("Albanian"),
    //    @XmlEnumValue("Swahili")
    sw("Swahili"),
    //    @XmlEnumValue("Tajik")
    tg("Tajik"),
    //    @XmlEnumValue("Thai")
    th("Thai"),
    //    @XmlEnumValue("Turkmen")
    tk("Turkmen"),
    //    @XmlEnumValue("Tagalog")
    tl("Tagalog"),
    //    @XmlEnumValue("Tonga")
    to("Tonga"),
    //    @XmlEnumValue("Turkish")
    tr("Turkish"),
    //    @XmlEnumValue("Tatar")
    tt("Tatar"),
    //    @XmlEnumValue("Uighur")
    ug("Uighur"),
    //    @XmlEnumValue("Ukrainian")
    uk("Ukrainian"),
    //    @XmlEnumValue("Uzbek")
    uz("Uzbek"),
    //    @XmlEnumValue("Wolof")
    wo("Wolof"),
    //    @XmlEnumValue("Xhosa")
    xh("Xhosa"),
    //    @XmlEnumValue("Yiddish")
    yi("Yiddish"),
    //    @XmlEnumValue("Zulu")
    zu("Zulu"),
    //    @XmlEnumValue("Chinese")
    zh("Chinese"),
    //    @XmlEnumValue("rm")
    rm("rm"),
    
    ja("ja");
    
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
