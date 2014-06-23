package de.uni_goettingen.sub.commons.ocr.abbyy.server.multiuser;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;

public class AbbyyMultiuserFactory  extends AbbyyServerFactory {

	@Override
	public OCREngine createEngine() {
		return new MultiUserAbbyyOCREngine();
	}

}
