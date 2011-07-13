package de.unigoettingen.sub.commons.ocrComponents.webservice;
/*

© 2010, SUB Göttingen. All rights reserved.
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


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import org.junit.runner.Result;
import org.junit.runner.JUnitCore;
import org.junit.experimental.ParallelComputer;



/*very importantly, in order to begin the test, 
must remove @Ignore from the class CommandLine and OcrWebservice1. 
@Ignore is only made to avoid problem with CI*/
public class PriorisierungsTest {
	final static Logger logger = LoggerFactory.getLogger(PriorisierungsTest.class);

	
	@Test 
	public void testsRunInParallel() {
		Result result= JUnitCore.runClasses(ParallelComputer.classes(),
				CommandLine.class, OcrWebservice1.class);
		assertTrue(result.wasSuccessful());
	}
	
		
}
	
	
	
	



