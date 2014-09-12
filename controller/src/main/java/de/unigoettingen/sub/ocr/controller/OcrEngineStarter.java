package de.unigoettingen.sub.ocr.controller;

import java.io.File;
import java.util.Locale;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrPriority;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;

public class OcrEngineStarter {

	private FactoryProvider factoryProvider = new FactoryProvider();
	private BeanProvider beanProvider = new BeanProvider();
	private OcrEngine engine;
	
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
			OcrProcess process = factory.createProcess();
			process.setName(bookFolder.getName());
			process.setOutputDir(new File(params.outputFolder));
			File[] allPages = fileAccess.getAllImagesFromFolder(bookFolder, params.inputFormats);
			for (File page : allPages) {
				process.addImage(page.toURI(), page.length());
			}
			
			for (String outFormat : params.outputFormats) {
				OcrFormat ocrFormat = OcrFormat.valueOf(outFormat);
				process.addOutput(ocrFormat);
			}
			
			for (String lang : params.inputLanguages) {
				process.addLanguage(new Locale(lang));
			}
			process.setPriority(OcrPriority.NORMAL);
			process.setTextType(OcrTextType.valueOf(params.inputTextType));
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
