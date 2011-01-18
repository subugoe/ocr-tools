package de.uni_goettingen.sub.commons.ocr.abbyy.server;

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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class AbbyyServerSimulatorTest {
	public static String INPUT = "input";
	public static String OUTPUT = "output";
	public static String HOTFOLDER = "hotfolder";
	public static String EXPECTED = "expected";
	public static File TEST_INPUT_FILE, TEST_OUTPUT_FILE, TEST_HOTFOLDER_FILE, TEST_EXPECTED_FILE;
	protected static AbbyyServerSimulator sim;

	@BeforeClass
	public static void init () {
		TEST_INPUT_FILE = new File(System.getProperty("user.dir") + File.separator +"src/test/resources/"+ INPUT);
		TEST_OUTPUT_FILE = new File(System.getProperty("user.dir") + File.separator +"src/test/resources/"+ OUTPUT);
		TEST_HOTFOLDER_FILE = new File(System.getProperty("user.dir") + File.separator +"src/test/resources/"+ HOTFOLDER);
		TEST_EXPECTED_FILE = new File(System.getProperty("user.dir") + File.separator +"src/test/resources/"+  EXPECTED);
	}

	/*public AbbyyServerSimulatorTest() {
	}*/

	@Test
	public void testSimulator () {
		sim = new AbbyyServerSimulator(TEST_HOTFOLDER_FILE, TEST_EXPECTED_FILE);
		
		assertTrue(sim.hotfolder.isDirectory());
		assertTrue(sim.inputHotfolder.isDirectory());
		assertTrue(sim.outputHotfolder.isDirectory());
		assertTrue(sim.errorHotfolder.isDirectory());
		assertTrue(sim.expected.isDirectory());
	}

}
