package de.unigoettingen.sub.commons.ocrComponents.webservice;

import java.util.Comparator;

public class ItemComparator implements Comparator<Item> {

	@Override
	public int compare(Item item1, Item item2) {
		int prio = item1.getPrio().compareTo(item2.getPrio());
		
		if (prio != 0)
			return prio;
		else
			return item1.getTime().compareTo(item2.getTime());
	}

}
