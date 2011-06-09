package de.unigoettingen.sub.commons.ocrComponents.hazelcast1;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Main {

	/** The Constant logger. */
	final static Logger logger = LoggerFactory
			.getLogger(Main.class);
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		logger.debug(" 1 Sart");
		AbbyyOCREngine engine = AbbyyOCREngine.getInstance();
		JOptionPane.showMessageDialog(null, "Start Hazelcast 1");
		engine.recognize();
		System.out.println("############ 1 FINISCHED         ##################");
	}

}
