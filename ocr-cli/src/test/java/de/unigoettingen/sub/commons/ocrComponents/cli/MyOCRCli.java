package de.unigoettingen.sub.commons.ocrComponents.cli;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;

public class MyOCRCli extends OCRCli {
		
		public MyOCRCli (OCREngine engine, OCRProcess process) {
			this.engine = engine;
			this.processes.add(process);
		}
		
		public static OCRCli getInstance () {
			
			return null;
		}
		
		public static OCRCli getInstance (OCREngine engine, OCRProcess process) {
			if (_instance == null) {
				_instance = new MyOCRCli(engine, process);
			}
			return _instance;
		}
		
		
}
