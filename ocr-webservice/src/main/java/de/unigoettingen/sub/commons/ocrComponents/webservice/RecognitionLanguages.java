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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecognitionLanguages", propOrder = { "recognitionLanguage" })
public class RecognitionLanguages {

	@XmlElement(defaultValue = "de")
	protected List<RecognitionLanguage> recognitionLanguage;

	public List<RecognitionLanguage> getRecognitionLanguage() {
		if (recognitionLanguage == null) {
			recognitionLanguage = new ArrayList<RecognitionLanguage>();
		}
		return this.recognitionLanguage;
	}

}
