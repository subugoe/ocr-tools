package de.uni_goettingen.sub.commons.ocr.abbyy.server;
/*

© 2010, SUB Goettingen. All rights reserved.
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
