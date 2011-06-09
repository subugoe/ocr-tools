package de.unigoettingen.sub.commons.ocrComponents.hazelcast;

import java.util.Comparator;

public class ItemComparator implements Comparator<AbbyyOCRProcess> {

	@Override
	public int compare(AbbyyOCRProcess item1, AbbyyOCRProcess item2) {
		int prio = item1.getPrio().compareTo(item2.getPrio());
		
		if (prio != 0)
			return prio;
		else
			return item1.getTime().compareTo(item2.getTime());
	}

}
