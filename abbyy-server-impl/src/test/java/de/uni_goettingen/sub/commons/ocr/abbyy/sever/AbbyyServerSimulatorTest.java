package de.uni_goettingen.sub.commons.ocr.abbyy.sever;

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
import org.junit.Test;

public class AbbyyServerSimulatorTest {

	@BeforeClass
	public static void init () {

	}

	public AbbyyServerSimulatorTest() {

	}

	@Test
	public void testSimulator () {
		AbbyyServerSimulator sim = new AbbyyServerSimulator(new File("./src/test/resources/hotfolder"), new File("./src/test/resources/expected"));
		assertTrue(sim.input.isDirectory());
		assertTrue(sim.output.isDirectory());
		assertTrue(sim.error.isDirectory());
	}

}
