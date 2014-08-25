package de.unigoettingen.sub.ocr.controller;

import java.io.File;
import java.net.URI;
import java.util.Locale;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRTextType;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class OcrEngineStarter {

	private FactoryProvider factoryProvider = new FactoryProvider();
	private BeanProvider beanProvider = new BeanProvider();
	private OCREngine engine;
	
	// for unit tests
	void setFactoryProvider(FactoryProvider newProvider) {
		factoryProvider = newProvider;
	}
	// for unit tests
	void setBeanProvider(BeanProvider newBeanProvider) {
		beanProvider = newBeanProvider;
	}
	
	public void startOcrWithParams(OcrParameters params) {
		OcrFactory factory = factoryProvider.createFactory(params.ocrEngine, params.props);
		engine = factory.createEngine();

		FileAccess fileAccess = beanProvider.getFileAccess();
		File[] allBookFolders = fileAccess.getAllFolders(params.inputFolder, params.inputFormats);
		for (File bookFolder : allBookFolders) {
			OCRProcess process = factory.createProcess();
			process.setName(bookFolder.getName());
			File[] allPages = fileAccess.getAllImagesFromFolder(bookFolder, params.inputFormats);
			for (File page : allPages) {
				OCRImage image = factory.createImage();
				image.setUri(page.toURI());
				image.setSize(page.length());
				process.addImage(image);
			}
			
			for (String outFormat : params.outputFormats) {
				OCRFormat ocrFormat = OCRFormat.valueOf(outFormat);
				OCROutput output = factory.createOutput();
				File outputFolder = new File(params.outputFolder);
				URI outputUri = new File(outputFolder, process.getName() + "." + outFormat.toLowerCase()).toURI();
				output.setUri(outputUri);
				output.setlocalOutput(outputFolder.getAbsolutePath());
				process.addOutput(ocrFormat, output);
			}
			
			for (String lang : params.inputLanguages) {
				process.addLanguage(new Locale(lang));
			}
			process.setPriority(OCRPriority.NORMAL);
			process.setTextType(OCRTextType.valueOf(params.inputTextType));
			engine.addOcrProcess(process);
		}
		engine.recognize();
	}
	
	public int getEstimatedDurationInSeconds() {
		if (engine == null) {
			return 0;
		}
		return engine.getEstimatedDurationInSeconds();
	}
}
