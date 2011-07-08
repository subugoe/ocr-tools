package de.unigoettingen.sub.commons.ocrComponents.webservice;
/*

Copyright 2010 SUB Goettingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;


import javax.jws.WebService;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;



import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngineFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp;



@WebService(endpointInterface = "de.unigoettingen.sub.commons.ocrComponents.webservice.OcrService")
public class OcrServiceImpl implements OcrService {
	
	/** The logger. */
	protected static Logger logger = LoggerFactory
			.getLogger(OcrServiceImpl.class);
	/** The engine. */
	protected static OCREngine engine;
	protected static String WEBSERVER_PATH;
	protected static String LOCAL_PATH;
	/** The language. */
	protected static Set<Locale> langs;
	/** The OUTPU t_ definitions. */
	protected static HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;
	private String parent, jobName;
	
	// The duration.
	private Long duration = 0L;
	
	ByUrlResponseType byUrlResponseType;

	
	@Override
	public ByUrlResponseType ocrImageFileByUrl(ByUrlRequestType part1) {
		byUrlResponseType = new ByUrlResponseType();
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(
				"contentWebservice.xml"));
		OCREngineFactory ocrEngineFactory = (OCREngineFactory) factory
				.getBean("OCREngineFactory");

		Properties properties = new Properties();
		InputStream stream;
		try {
			stream = getClass().getResource("/webservice-config.properties")
					.openStream();
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			logger.error("Error reading configuration", e);
		}
		WEBSERVER_PATH = properties.getProperty("webserverpath");
		LOCAL_PATH = properties.getProperty("localpath");
		if(LOCAL_PATH == null || LOCAL_PATH.equals("")){
			LOCAL_PATH = System.getProperty("java.io.tmpdir");
		}
		if(!LOCAL_PATH.endsWith("/")){
			LOCAL_PATH = LOCAL_PATH + "/";
		}
		engine = ocrEngineFactory.newOcrEngine();
		OCRProcess aop = engine.newOcrProcess();

		if (part1.getInputUrl() == null
				&& !part1.getInputUrl().startsWith("http")
				&& !part1.getInputUrl().endsWith("tif")) {

			String error = "ERROR: " + part1.getInputUrl()
					+ " is null or No URL or no Image from type tif";
			return byURLresponse(duration, error);
		
		}else{
			
			List<OCRImage> imgs = new ArrayList<OCRImage>();
			URL inputuri = null;
			String[] urlParts;

			try {
				inputuri = new URL(part1.getInputUrl());
			} catch (MalformedURLException e) {
				logger.error("URL is Mal formed: " + part1.getInputUrl());
				String error = "URL is Mal formed: " + part1.getInputUrl();
				return byURLresponse(duration, error);
			}

			urlParts = inputuri.toString().split("/");
			parent = urlParts[urlParts.length - 2];
			jobName = urlParts[urlParts.length - 1].replace(".tif", "");
			File file = new File(LOCAL_PATH + parent + "/" + jobName
					+ "/" + urlParts[urlParts.length - 1]);

			try {
				FileUtils.copyURLToFile(inputuri, file);
			} catch (IOException e) {
				logger.error("ERROR CAN NOT COPY URL To File");
				String error = "ERROR CAN NOT COPY URL: " + part1.getInputUrl()
						+ " To Local File";
				return byURLresponse(duration, error);
			}

			aop.setName(jobName);
			OCRImage aoi = engine.newOcrImage(file.toURI());
			aoi.setSize(file.length());
			imgs.add(aoi);
			aop.setOcrImages(imgs);

			// add format
			OUTPUT_DEFINITIONS = new HashMap<OCRFormat, OCROutput>();

			OCRFormat ocrformat = part1.getOutputFormat();
			OCROutput aoo = engine.newOcrOutput();
			URI uri = null;
			
			try {
				uri = new URI(new File(WEBSERVER_PATH).toURI() + "/" + parent
						+ "/" + jobName + "."
						+ ocrformat.toString().toLowerCase());
			} catch (URISyntaxException e) {
				logger.error("URL is Mal formed: " + WEBSERVER_PATH + "/"
						+ parent + "/" + jobName + "."
						+ ocrformat.toString().toLowerCase());
				String error = "URL is Mal formed: " + WEBSERVER_PATH + "/"
				+ parent + "/" + jobName + "."
				+ ocrformat.toString().toLowerCase();
				return byURLresponse(duration, error);
			}
			// logger.debug("output Location " + uri.toString());
			aoo.setUri(uri);
			OUTPUT_DEFINITIONS.put(ocrformat, aoo);
			aop.addOutput(ocrformat, aoo);
			langs = new HashSet<Locale>();
			
			for (RecognitionLanguage r : part1.getOcrlanguages()
					.getRecognitionLanguage()) {
			//	langs.add(LANGUAGE_MAP.get(r));
				langs.add(new Locale(r.toString()));
			}
			aop.setLanguages(langs);
			aop.setPriority(OCRPriority.fromValue(part1.getOcrPriorityType()
					.value()));
			aop.setTextTyp(OCRTextTyp.fromValue(part1.getTextType().value()));
			engine.addOcrProcess(aop);

			logger.info("Starting recognize method");
			engine.recognize();
			
			if(aop.getOcrProcessMetadata().getDuration() != 0L){
				duration = aop.getOcrProcessMetadata().getDuration();
			}
			file.delete();
			logger.debug("Delete File: "+ file.toString());
			
			try {
				FileUtils.deleteDirectory(file.getParentFile());
				FileUtils.deleteDirectory(new File(LOCAL_PATH + parent));
			} catch (IOException e) {
				logger.error("ERROR CAN NOT deleteDirectory");
			}

			File f = new File(WEBSERVER_PATH + "/" + parent + "/" + jobName
					+ "." + ocrformat.toString().toLowerCase());
			
			if( !f.exists()){
				logger.error("ERROR File CAN NOT Find: "+ f.toString());
				String error = "ERROR File CAN NOT Find: "+ f.toString();
				return byURLresponse(duration, error);
			}
			
			byUrlResponseType.setMessage("Process finished successfully after " + duration + " milliseconds.");
			byUrlResponseType.setOutputUrl(WEBSERVER_PATH + "/" + parent + "/" + jobName	+ "." + ocrformat.toString().toLowerCase());
			byUrlResponseType.setProcessingLog("========= PROCESSING REQUEST (by URL) =========. "+ "\n" +
												"Using service: IMPACT Abbyy Fine Reader 2 Service "+ "\n" +
												"Parameter processingUnit: "+ WEBSERVER_PATH + "\n" +
												"URL of input image: "+ part1.getInputUrl()+ "\n" +
												"Wrote file " + file.toString()+  "\n" +
												"OUTFORMAT substitution variable value: "+ocrformat.toString()+ "\n" +
												"OUTFILE substitution variable value: " + f.getAbsolutePath()+ "\n" +
												"LANGUAGES substitution variable value: "+ langs.toString() + "\n" +
												"INFILE substitution variable value: "+ file.toString()+  "\n" +
												"INTEXTTYPE substitution variable value: "+ part1.getTextType().value()+ "\n" +
												"Process finished successfully with code 0."+ "\n" +
												"Output file has been created successfully.."+ "\n" +
												"Output Url: " + WEBSERVER_PATH + "/" + parent + "/" + jobName	+ "." + ocrformat.toString().toLowerCase()+ "\n" +
												"Output Url-Abbyy-Result : " + WEBSERVER_PATH + "/" + parent + "/" + jobName	+ ".xml.result.xml" + "\n" +
												"Output Url-Summary-File : " + WEBSERVER_PATH + "/" + parent + "/" + jobName	+ "-textMD.xml" + "\n" + 
												"Process finished successfully after " + duration + " milliseconds."
												);
			
			byUrlResponseType.setProcessingUnit(WEBSERVER_PATH);
			byUrlResponseType.setReturncode(0);
			byUrlResponseType.setSuccess(true);
			byUrlResponseType.setToolProcessingTime(duration);									
		} //end else

		return byUrlResponseType;
		
	}

	
	ByUrlResponseType byURLresponse(Long duration, String error) {
		byUrlResponseType.setMessage("Process finished unsuccessfully after "
				+ duration + " milliseconds.");
		byUrlResponseType.setOutputUrl("Output Url: ...");
		byUrlResponseType.setProcessingLog(error);
		byUrlResponseType.setProcessingUnit(WEBSERVER_PATH);
		byUrlResponseType.setReturncode(1);
		byUrlResponseType.setSuccess(false);
		byUrlResponseType.setToolProcessingTime(duration);
		return byUrlResponseType;
	}
}
