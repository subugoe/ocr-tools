package de.unigoettingen.sub.commons.ocr.util.merge;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;

public class MergerProvider {

	public Merger createMerger(OcrFormat fileFormat) {
		
		switch (fileFormat) {
			case TXT: return new TextMerger();
			case XML: return new AbbyyXmlMerger();
			case PDF: 
			case PDFA: return new PdfMerger();
			case METADATA: return new ResultXmlMerger();
			case HOCR: return new HocrMerger();
			default: throw new IllegalArgumentException("Merging is not supported for the format '" + fileFormat + "'");
		}
		
	}
	
}
