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

import static org.junit.Assert.*;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public class ConfigParserTest {

	final static Logger logger = LoggerFactory.getLogger(ConfigParserTest.class);

	@Test
	public void newConfig() {
		ConfigParser config = new ConfigParser().parse();
		assertNotNull(config);
	}
	
	@Test(expected=OCRException.class)
	public void newConfigException() {
		new ConfigParser().parse(null);
		fail();
	}
	
	@Test
	public void testConfiguration () {
		ConfigParser config = new ConfigParser().parse();
		assertNotNull(config.getConfig());
		assertNotNull(config.getServerURL());
		assertNotNull(config.getInput());
		assertNotNull(config.getError());
		assertNotNull(config.getOutput());
		assertTrue(config.getMaxFiles() > 0);
		assertTrue(config.getMaxSize() > 0);
		assertTrue(config.getMaxThreads() > 0);
		assertNotNull(config.getHotfolderClass());
		assertNotNull(config.getCheckServerState());
	}

	@Test
	public void testAuth () throws ConfigurationException {
		System.setProperty(ConfigParser.DEBUG_PROPERTY, "true");
		ConfigParser config = new ConfigParser().parse();
		assertTrue(config.getDebugAuth());
	}

}
