import org.junit.Test;

import de.unigoettingen.sub.commons.ocrComponents.cli.OCRCli;



public class OcrCliTest {
	
	@Test
	public void testCli () {
		OCRCli ocr = OCRCli.getInstance();
		String[] args = new String[1];
		args[0] = "-h";
		ocr.configureFromArgs(args);
		
		
	}

	
	
	
	
	
	
	
}
