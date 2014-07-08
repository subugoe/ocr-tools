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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;









import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextType;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;
import de.uni_goettingen.sub.commons.ocr.api.OcrFactory;
import de.unigoettingen.sub.commons.ocr.util.FileManager;
import de.unigoettingen.sub.ocr.controller.FactoryProvider;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;
import de.unigoettingen.sub.ocr.controller.OcrParameters;

/**
 * IMPACT Abbyy Fine Reader 8.0 Service. This service provides the basic
 * functionality of the Abbyy Fine Reader 8.0 text recogntion engine for
 * applying OCR to an image file.
 *  
 * @author mabergn
 *
 */

@WebService(endpointInterface = "de.unigoettingen.sub.commons.ocrComponents.webservice.OcrService")
public class OcrServiceImpl implements OcrService {
	@Resource
	private WebServiceContext wsContext;
	
	static String ocrEngineId = "abbyy";
	
	private final String appName = "ws";
	
	/** The logger. */
	private final static Logger LOGGER = LoggerFactory
			.getLogger(OcrServiceImpl.class);
		
	FileManager getFileManager() {
		return new FileManager();
	}
	
	FactoryProvider getFactoryProvider() {
		return new FactoryProvider();
	}
	
	OcrEngineStarter getEngineStarter() {
		return new OcrEngineStarter();
	}
	
	@Override
	public ByUrlResponseType ocrImageFileByUrl(ByUrlRequestType request) {
		ByUrlResponseType response = new ByUrlResponseType();

		FileManager fileManager = getFileManager();
		
		Properties props = fileManager.getFileProperties("webservice-config.properties");

		String localPath = props.getProperty("localpath");
		if(localPath == null || localPath.equals("")){
			localPath = System.getProperty("java.io.tmpdir");
		}
		if(!localPath.endsWith("/")){
			localPath += "/";
		}
		
		String webserverPath = props.getProperty("webserverpath");
		
		if(webserverPath == null || webserverPath.equals("")){
			webserverPath = System.getProperty("ocrWebservice.root");
		}
		if(!webserverPath.endsWith("/")){
			webserverPath += "/";
		}

		int randomNumber = Math.abs((int) ((Math.random()*((int) System.currentTimeMillis()))+1));
		
		String jobName = "OcrServiceImplService_outputUrl"+ "_" + randomNumber;

		RecognitionLanguages langs = request.getOcrlanguages();
		List<String> langStringsList = new ArrayList<String>();
		for (RecognitionLanguage lang : langs.getRecognitionLanguage()) {
			langStringsList.add(lang.toString());
		}

		File file = new File(localPath  + jobName
				+ "/input.tif");

		try {
			URL inputUrl = new URL(request.getInputUrl());
			FileUtils.copyURLToFile(inputUrl, file);
		} catch (IOException e) {
			LOGGER.error("ERROR CAN NOT COPY URL To File");
			String error = "ERROR CAN NOT COPY URL: " + request.getInputUrl()
					+ " To Local File";
			return byURLresponse(webserverPath, error, response);
		}

		String webserverHostname = "";
		if(props.getProperty("hostname") == null || props.getProperty("hostname").equals("no")){
			  MessageContext mc = wsContext.getMessageContext();
			  URI url = (URI)mc.get("javax.xml.ws.wsdl.description");
			  String hostname = url.getHost();
			  webserverHostname = "http://"+hostname+"/"+appName+"/"; 
		  }else {
			  webserverHostname = props.getProperty("hostname");
		  }
		  

		
		OcrParameters params = new OcrParameters();
		params.ocrEngine = ocrEngineId;
		params.outputFormats = new String[]{request.getOutputFormat().toString()};
		params.inputFolder = localPath + jobName;
		params.inputFormats = new String[]{"tif"};
		params.inputLanguages = langStringsList.toArray(new String[]{});
		params.inputTextType = request.getTextType().toString();
		params.priority = "3";
		params.outputFolder = webserverPath + "temp";
		params.props = new Properties();
		
		getEngineStarter().startOcrWithParams(params);
		
//		OcrFactory factory = getFactoryProvider().createFactory(ocrEngineId, new Properties());
//		
//		OCREngine engine = factory.createEngine();
//		
//		OCRProcess aop = engine.newOcrProcess();
//
//		OCRFormat ocrformat = request.getOutputFormat();
//		
//			List<OCRImage> imgs = new ArrayList<OCRImage>();
//
//			aop.setName(jobName);
//			OCRImage aoi = engine.newOcrImage(file.toURI());
//			aoi.setSize(file.length());
//			imgs.add(aoi);
//			aop.setOcrImages(imgs);
//
//			// add format
//			HashMap<OCRFormat, OCROutput> outputDefinitions = new HashMap<OCRFormat, OCROutput>();
//
//			OCROutput aoo = engine.newOcrOutput();
//			URI uri = null;
//			String temp = "temp";
//			try {
//				uri = new URI(new File(webserverPath).toURI()+ "/" + temp
//						+ "/" + jobName + "."
//						+ ocrformat.toString().toLowerCase());
//			} catch (URISyntaxException e) {
//				LOGGER.error("URL is Malformed: " + webserverPath 
//						+ temp + "/" + jobName + "."
//						+ ocrformat.toString().toLowerCase());
//				String error = "URL is Malformed: " + webserverPath 
//				+ temp + "/" + jobName + "."
//				+ ocrformat.toString().toLowerCase();
//				boolean wasDeleted = file.delete();
//				if (!wasDeleted) {
//					LOGGER.error("Could not delete file " + file.getAbsolutePath());
//				}
//				return byURLresponse(webserverPath, error, response);
//			}
//			
//			aoo.setUri(uri);
//			outputDefinitions.put(ocrformat, aoo);
//			aop.addOutput(ocrformat, aoo);
//
//			
//			
//			for (RecognitionLanguage r : request.getOcrlanguages()
//					.getRecognitionLanguage()) {
//				aop.addLanguage(new Locale(r.toString()));
//			}
//			aop.setPriority(OCRPriority.fromValue(request.getOcrPriorityType()
//					.value()));
//			aop.setTextType(OCRTextType.fromValue(request.getTextType().value()));
//			engine.addOcrProcess(aop);
//
//			LOGGER.info("Starting recognize method");
//			engine.recognize();
//			
//			OCRProcessMetadata meta = aop.getOcrProcessMetadata();
//			Long duration = 0L;
//			if(meta != null && meta.getDuration() != 0L){
//				duration = aop.getOcrProcessMetadata().getDuration();
//			}
		
		String temp = "temp";
		Long duration = 5L;
			file.delete();
			LOGGER.debug("Delete File: "+ file.toString());
			
			try {
				FileUtils.deleteDirectory(file.getParentFile());
				FileUtils.deleteDirectory(new File(localPath + randomNumber));
			} catch (IOException e) {
				LOGGER.error("ERROR CAN NOT delete Directory");
			}

			File f = new File(webserverPath + temp + "/" + jobName
					+ "." + params.outputFormats[0].toLowerCase());
			
			if( !f.exists()){
				LOGGER.error("ERROR. CANNOT Find File: "+ f.toString());
				String error = "File could not be processed: " + request.getInputUrl();
				return byURLresponse(webserverPath, error, response);
			}
			String newLine = ".\n";
			response.setMessage("Process finished successfully after " + duration + " milliseconds.");
			response.setOutputUrl(webserverHostname + temp + "/"+ jobName	+ "." + params.outputFormats[0].toLowerCase());
			response.setProcessingLog("========= PROCESSING REQUEST (by URL) =========. "+ "\n" +
												"Using service: OcrServiceImplService. "+ "\n" +
												"Parameter processingUnit: "+ webserverHostname + newLine +
												"URL of input image: "+ request.getInputUrl()+ newLine +
												"Wrote file " + file.toString()+  newLine +
												"OUTFORMAT substitution variable value: "+params.outputFormats[0].toLowerCase()+ newLine +
												"OUTFILE substitution variable value: " + f.getAbsolutePath()+ newLine +
												"LANGUAGES substitution variable value: "+ params.inputLanguages + newLine +
												"INFILE substitution variable value: "+ file.toString()+  newLine +
												"INTEXTTYPE substitution variable value: "+ request.getTextType().value()+ newLine +
												"Process finished successfully with code 0."+ "\n" +
												"Output file has been created successfully.."+ "\n" +
												"Output Url: " + webserverHostname + temp + "/" + jobName	+ "." + params.outputFormats[0].toLowerCase()+ newLine +
												"Output Url-Abbyy-Result : " + webserverHostname + temp + "/" + jobName	+ ".xml.result.xml" + newLine +
												"Output Url-Summary-File : " + webserverHostname + temp + "/" + jobName	+ "-textMD.xml" + newLine + 
												"Process finished successfully after " + duration + " milliseconds.."
												);
			
			response.setProcessingUnit(webserverHostname);
			response.setReturncode(0);
			response.setSuccess(true);
			response.setToolProcessingTime(duration);									
		

		return response;
		
	}

	
	ByUrlResponseType byURLresponse(String webserverPath, String error, ByUrlResponseType byUrlResponseType) {
		byUrlResponseType.setMessage("Process finished unsuccessfully ");
		byUrlResponseType.setOutputUrl("");
		byUrlResponseType.setProcessingLog(error);
		byUrlResponseType.setProcessingUnit(webserverPath);
		byUrlResponseType.setReturncode(1);
		byUrlResponseType.setSuccess(false);
		byUrlResponseType.setToolProcessingTime(0L);
		return byUrlResponseType;
	}
}
