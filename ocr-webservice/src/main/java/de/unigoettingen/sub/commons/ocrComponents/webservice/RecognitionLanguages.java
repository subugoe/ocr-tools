package de.unigoettingen.sub.commons.ocrComponents.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecognitionLanguages", propOrder = { "recognitionLanguage" })
public class RecognitionLanguages {

	//@XmlElement(defaultValue = "German")
	protected List<RecognitionLanguage> recognitionLanguage;

	public List<RecognitionLanguage> getRecognitionLanguage() {
		if (recognitionLanguage == null) {
			recognitionLanguage = new ArrayList<RecognitionLanguage>();
		}
		return this.recognitionLanguage;
	}

}
