

import org.junit.Test;

import de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli;



public class OcrCliTest {
	
	
	@Test
	public void testCli () {
		OCRCli ocr = OCRCli.getInstance();
		
		String[] args = new String[4];
		args[0] = "-l Deutsch,English";
		args[1] = "-o D:/Recognition/GDZ/output";
		args[2] = "-fTXT,PDF";
		args[3] = "C:/webdav/";
		ocr.configureFromArgs(args);
		
		
	}

	
}
