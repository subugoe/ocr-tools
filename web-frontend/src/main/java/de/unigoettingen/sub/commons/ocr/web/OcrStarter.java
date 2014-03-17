package de.unigoettingen.sub.commons.ocr.web;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngineFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;

public class OcrStarter implements Runnable {

	private OcrParameters param;

	public void setParameters(OcrParameters newParameters) {
		param = newParameters;
	}

	public String checkParameters() {
		System.out.println(param.email);
		return "OK";
	}

	@Override
	public void run() {
		ApplicationContext ac = new ClassPathXmlApplicationContext("abbyy" + "-context.xml");
		OCREngineFactory ocrEngineFactory = (OCREngineFactory) ac
					.getBean("OCREngineFactory");

		OCREngine engine = ocrEngineFactory.newOcrEngine();

		System.out.println(param.inputFolder);
		File mainFolder = new File(param.inputFolder);
		File[] subFolders = mainFolder.listFiles();
		for (File bookFolder : subFolders) {
			OCRProcess process = engine.newOcrProcess();
			process.setName(bookFolder.getName());
			
			List<OCRImage> bookImages = new ArrayList<OCRImage>();
			for (File imageFile : OCRUtil.makeFileList(bookFolder, param.imageFormat)) {
				OCRImage image = engine.newOcrImage(imageFile.toURI());
				image.setSize(imageFile.length());
				bookImages.add(image);
			}
			process.setOcrImages(bookImages);
			process.setPriority(OCRPriority.NORMAL);
			
			Set<Locale> langs = new HashSet<Locale>();
			for (String lang : param.languages) {
				langs.add(new Locale(lang));
			}
			process.setLanguages(langs);
			process.setTextType(OCRTextType.valueOf(param.textType));
			
			try {
				for (String formatString : param.outputFormats) {
					OCRFormat format = OCRFormat.parseOCRFormat(formatString);
					OCROutput output = engine.newOcrOutput();
					URI uri = new URI(new File(param.outputFolder).toURI()
							+ bookFolder.getName()
							+ "." + format.toString().toLowerCase());
					output.setUri(uri);
					output.setlocalOutput(new File(param.outputFolder).getAbsolutePath());
					process.addOutput(format, output);
				}
			} catch(URISyntaxException e){
				System.out.println("uri: " + e);
			}
			
			engine.addOcrProcess(process);
		}
		

		engine.recognize();
	}


}
