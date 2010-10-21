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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.ConfigParser;

public class ConfigParserTest {

	final static Logger logger = LoggerFactory.getLogger(ConfigParserTest.class);

	protected static ConfigParser cp = null;

	@BeforeClass
	public static void init () throws ConfigurationException {
		try {
			cp = new ConfigParser().parse();
		} catch (RuntimeException e) {
			logger.info("If run from Maven an Exception is expected", e);
		}
		assertNotNull(cp);
	}

	@Test
	public void testConfiguration () {
		cp = new ConfigParser().parse();
		assertNotNull(cp.getConfig());
	}

	@Test
	public void testUrl () throws ConfigurationException {
		cp = new ConfigParser().parse();
		assertFalse(cp.getDebugAuth());
		assertNotNull(cp.getServerURL());
	}

	@Test
	public void testAuth () throws ConfigurationException {
		System.setProperty(ConfigParser.DEBUG_PROPERTY, "true");
		cp = new ConfigParser().parse();
		assertTrue(cp.getDebugAuth());
	}

}
