package de.unigoettingen.sub.commons.ocrComponents.webservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class FileTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException {
		URL url = new URL("file:/home/dennis/bla.txt");
		File file = new File("/home/dennis/newdir/bla2.txt");
		FileUtils.copyURLToFile(url, file);
	}

}
