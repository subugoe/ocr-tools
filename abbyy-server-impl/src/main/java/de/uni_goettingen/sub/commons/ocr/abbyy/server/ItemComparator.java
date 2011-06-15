package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Comparator;



public class ItemComparator implements Comparator<AbbyyOCRProcess> {

	@Override
	public int compare(AbbyyOCRProcess item1, AbbyyOCRProcess item2) {
		int prio = item1.getPriority().compareTo(item2.getPriority());
		
		if (prio != 0)
			return prio;
		else
			return item1.getTime().compareTo(item2.getTime());
	}

}
