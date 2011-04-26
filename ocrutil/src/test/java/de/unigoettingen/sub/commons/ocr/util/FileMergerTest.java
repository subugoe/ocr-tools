package de.unigoettingen.sub.commons.ocr.util;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileMergerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void mergeFilesTest() {
		File f1 = new File(getClass().getResource("/tmp.hocr1.html").getFile());
		File f2 = new File(getClass().getResource("/tmp.hocr2.html").getFile());
		File f3 = new File(getClass().getResource("/tmp.hocr3.html").getFile());
		
		URL root = getClass().getResource("/");
		File out = new File(root.getPath() + "tmp.hocr");
		
		List<File> images = new ArrayList<File>();	
		
		images.add(f1);
		images.add(f2);
		images.add(f3);
		
		FileMerger.mergeFiles(OCRFormat.HOCR, images, out);
		
	}
	
	
	@After
	public void tearDown() throws Exception {
	}

}
