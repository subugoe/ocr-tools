package de.uni_goettingen.sub.commons.ocr.abbyy.ocrsdk;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

public class OcrsdkImageTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void readBytesFromImageFile() throws IOException {
		URI imageUri = new File("src/test/resources/fakeimage.txt").toURI();
		OcrsdkImage image = new OcrsdkImage();
		image.setLocalUri(imageUri);
		byte[] imageBytes = image.getAsBytes();
		assertEquals("byte array length", 2, imageBytes.length);
		assertEquals("first byte", 'a', imageBytes[0]);
		assertEquals("second byte", 'b', imageBytes[1]);
	}

}
