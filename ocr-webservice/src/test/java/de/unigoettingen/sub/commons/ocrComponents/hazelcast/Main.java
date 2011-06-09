package de.unigoettingen.sub.commons.ocrComponents.hazelcast;

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
		logger.debug("Sart");
		AbbyyOCREngine engine = AbbyyOCREngine.getInstance();
		JOptionPane.showMessageDialog(null, "Start Hazelcast");
		engine.recognize();
		System.out.println("############ FINISCHED         ##################");
	}

}
