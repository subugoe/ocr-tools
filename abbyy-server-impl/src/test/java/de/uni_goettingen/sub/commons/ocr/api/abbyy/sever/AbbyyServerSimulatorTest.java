package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

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
