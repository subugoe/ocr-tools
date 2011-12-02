package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class DirAbbyySimulator extends Thread {

	private String resultTemplate = System.getProperty("user.dir")
			+ "/src/test/resources/hotfolder/template.xml.result.xml";
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";

	private File hotfolder;

	public DirAbbyySimulator(File hotfolder) {
		this.hotfolder = hotfolder;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			try {
				sleep(500);

				File inputDir = new File(hotfolder + "/" + INPUT);
				File outputDir = new File(hotfolder + "/" + OUTPUT);

				for (File inFile : inputDir.listFiles()) {
					String name = inFile.getName();

					// just delete all tifs
					if (name.endsWith(".tif")) {
						inFile.delete();

						// found xml ticket
					} else if (name.endsWith(".xml")) {
						inFile.delete();
						
						// give the abbyyOcrProcess the files that it waits for
						try {
							File resultXml = new File(outputDir + "/" + name
									+ ".result.xml");
							FileUtils.copyFile(new File(resultTemplate),
									resultXml);
							File resultOcr = new File(outputDir + "/"
									+ name.substring(0, name.length() - 4)
									+ ".txt");
							FileUtils.writeStringToFile(resultOcr,
									"some OCR text");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
