package de.unigoettingen.sub.ocr.controller;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.unigoettingen.sub.commons.ocr.util.BeanProvider;
import de.unigoettingen.sub.commons.ocr.util.FileManager;

public class OcrEngineStarter {

	private FactoryProvider factoryProvider = new FactoryProvider();
	private BeanProvider beanProvider = new BeanProvider();
	
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
		OCREngine engine = factory.createEngine();
		
		FileManager manager = beanProvider.getFileManager();
		File[] allBookFolders = manager.getAllFolders(params.inputFolder, params.inputFormats);
		for (File bookFolder : allBookFolders) {
			OCRProcess process = factory.createProcess();
			process.setName(bookFolder.getName());
			File[] allPages = manager.getAllImagesFromFolder(bookFolder, params.inputFormats);
			List<OCRImage> images = new ArrayList<OCRImage>();
			for (File page : allPages) {
				OCRImage image = factory.createImage();
				image.setUri(page.toURI());
				image.setSize(page.length());
				images.add(image);
			}
			process.setOcrImages(images);
			
			for (String outFormat : params.outputFormats) {
				OCRFormat ocrFormat = OCRFormat.valueOf(outFormat);
				OCROutput output = factory.createOutput();
				File outputFolder = new File(params.outputFolder);
				URI outputUri = new File(outputFolder, process.getName() + "." + outFormat.toLowerCase()).toURI();
				output.setUri(outputUri);
				output.setlocalOutput(outputFolder.getAbsolutePath());
				process.addOutput(ocrFormat, output);
			}
			
			Set<Locale> languages = new HashSet<Locale>();
			for (String lang : params.inputLanguages) {
				languages.add(new Locale(lang));
			}
			process.setLanguages(languages);
			process.setPriority(OCRPriority.NORMAL);
			process.setTextType(OCRTextType.valueOf(params.inputTextType));
			engine.addOcrProcess(process);
		}
		engine.recognize();
	}
}
