package de.uni_goettingen.sub.commons.ocr.api.abbyy.sever;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.vfs.FileSystemException;
import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.AbbyyServerEngine;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.Hotfolder;

public class AbbyyServerEngineTest {
	public static AbbyyServerEngine abbyy;
	public Hotfolder hotfolder;

	@Before
	public void init () throws FileSystemException, ConfigurationException {
		PropertiesConfiguration config = new PropertiesConfiguration("config-properties");
		abbyy = AbbyyServerEngine.getInstance();
		//abbyy = mock(AbbyyServerEngine.class);
		/*
		abbyy = new AbbyyServerEngine();
		abbyy.loadConfig(config);
		*/
	}

	@Test
	public void testCli () throws IOException {

		assertNotNull(abbyy);
	}

}
