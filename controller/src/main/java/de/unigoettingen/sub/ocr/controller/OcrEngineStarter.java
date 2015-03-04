package de.unigoettingen.sub.ocr.controller;

import java.io.File;
import java.util.Locale;

import de.uni_goettingen.sub.commons.ocr.api.OcrEngine;
import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrProcess;
import de.uni_goettingen.sub.commons.ocr.api.OcrPriority;
import de.uni_goettingen.sub.commons.ocr.api.OcrQuality;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileAccess;
import de.unigoettingen.sub.commons.ocr.util.Mailer;
import de.unigoettingen.sub.commons.ocr.util.OcrParameters;

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
				process.addImage(page.toURI());
			}
			
			for (String outFormat : params.outputFormats) {
				OcrFormat ocrFormat = OcrFormat.valueOf(outFormat.toUpperCase());
				process.addOutput(ocrFormat);
			}
			
			for (String lang : params.inputLanguages) {
				process.addLanguage(new Locale(lang));
			}
			process.setPriority(OcrPriority.fromValue(params.priority));
			process.setTextType(OcrTextType.valueOf(params.inputTextType.toUpperCase()));
			process.setQuality(OcrQuality.BEST);
			engine.addOcrProcess(process);
		}
		
		Mailer mailer = null;
		String mailAddress = params.props.getProperty("email");
		if (mailAddress != null) {
			int estimatedExecTime = engine.getEstimatedDurationInSeconds();
			mailer = beanProvider.getMailer();
			mailer.sendStarted(mailAddress, estimatedExecTime);
		}
		
		engine.recognize();
		
		if (mailAddress != null) {
			mailer.sendFinished(mailAddress, params.outputFolder);
		}
	}
	
}
