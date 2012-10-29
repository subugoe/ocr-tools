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
import de.uni_goettingen.sub.commons.ocr.api.OCREngineFactory;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRPriority;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess.OCRTextTyp;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

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
	
	private final String appName = "ocr-webservice";
	
	/** The logger. */
	private final static Logger LOGGER = LoggerFactory
			.getLogger(OcrServiceImpl.class);
		
	
	private URL getUrl(ByUrlRequestType request) throws IOException {
		
		String inputUrlString = request.getInputUrl();
		URL inputUrl = new URL(inputUrlString);
//		URLConnection uconn = inputUrl.openConnection();
//		String type = uconn.getContentType();
//
//		if (!type.equals("image/tiff"))
//			throw new IOException("Not a Tiff image! Was " + type);
		
		return inputUrl;
	}
	
	@Override
	public ByUrlResponseType ocrImageFileByUrl(ByUrlRequestType request) {
		ByUrlResponseType byUrlResponseType = new ByUrlResponseType();
		OCREngine engine;
		String webserverPath,webserverHostname,localPath, jobName;
		int randomNumber;
		Set<Locale> langs;
		HashMap<OCRFormat, OCROutput> outputDefinitions;
		Long duration = 0L;

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
			LOGGER.error("Error reading configuration", e);
		}
		
		localPath = properties.getProperty("localpath");
		if(localPath == null || localPath.equals("")){
			localPath = System.getProperty("java.io.tmpdir");
		}
		if(!localPath.endsWith("/")){
			localPath = localPath + "/";
		}
		
		webserverPath = properties.getProperty("webserverpath");
		
		if(webserverPath == null || webserverPath.equals("")){
			webserverPath = System.getProperty("ocrWebservice.root");
		}


		URL inputUrl = null;
		try {
			inputUrl = getUrl(request);
		} catch (IOException e) {
			String error = "URL Error: " + e.getMessage();
			LOGGER.error(error);
			return byURLresponse(webserverPath, error, byUrlResponseType);
		}

		engine = ocrEngineFactory.newOcrEngine();
		OCRProcess aop = engine.newOcrProcess();

		OCRFormat ocrformat = request.getOutputFormat();
		
			List<OCRImage> imgs = new ArrayList<OCRImage>();

			randomNumber = Math.abs((int) ((Math.random()*((int) System.currentTimeMillis()))+1));
			
			jobName = "OcrServiceImplService_outputUrl"+ "_" + randomNumber;
			File file = new File(localPath + randomNumber + "/" + jobName
					+ "/" + randomNumber + ".tif");

			try {
				FileUtils.copyURLToFile(inputUrl, file);
			} catch (IOException e) {
				LOGGER.error("ERROR CAN NOT COPY URL To File");
				String error = "ERROR CAN NOT COPY URL: " + request.getInputUrl()
						+ " To Local File";
				return byURLresponse(webserverPath, error, byUrlResponseType);
			}

			aop.setName(jobName);
			OCRImage aoi = engine.newOcrImage(file.toURI());
			aoi.setSize(file.length());
			imgs.add(aoi);
			aop.setOcrImages(imgs);

			// add format
			outputDefinitions = new HashMap<OCRFormat, OCROutput>();

			OCROutput aoo = engine.newOcrOutput();
			URI uri = null;
			String temp = "temp";
			try {
				uri = new URI(new File(webserverPath).toURI()+ "/" + temp
						+ "/" + jobName + "."
						+ ocrformat.toString().toLowerCase());
			} catch (URISyntaxException e) {
				LOGGER.error("URL is Malformed: " + webserverPath 
						+ temp + "/" + jobName + "."
						+ ocrformat.toString().toLowerCase());
				String error = "URL is Malformed: " + webserverPath 
				+ temp + "/" + jobName + "."
				+ ocrformat.toString().toLowerCase();
				boolean wasDeleted = file.delete();
				if (!wasDeleted) {
					LOGGER.error("Could not delete file " + file.getAbsolutePath());
				}
				return byURLresponse(webserverPath, error, byUrlResponseType);
			}
			
			aoo.setUri(uri);
			outputDefinitions.put(ocrformat, aoo);
			aop.addOutput(ocrformat, aoo);

			if(properties.getProperty("hostname") == null || properties.getProperty("hostname").equals("no")){
				  MessageContext mc = wsContext.getMessageContext();
				  URI url = (URI)mc.get("javax.xml.ws.wsdl.description");
				  String hostname = url.getHost();
				  webserverHostname = "http://"+hostname+"/"+appName+"/"; 
			  }else {
				  webserverHostname = properties.getProperty("hostname");
			  }
			  
			
			langs = new HashSet<Locale>();
			
			for (RecognitionLanguage r : request.getOcrlanguages()
					.getRecognitionLanguage()) {
				langs.add(new Locale(r.toString()));
			}
			aop.setLanguages(langs);
			aop.setPriority(OCRPriority.fromValue(request.getOcrPriorityType()
					.value()));
			aop.setTextTyp(OCRTextTyp.fromValue(request.getTextType().value()));
			engine.addOcrProcess(aop);

			LOGGER.info("Starting recognize method");
			engine.recognize();
			
			OCRProcessMetadata meta = aop.getOcrProcessMetadata();
			if(meta != null && meta.getDuration() != 0L){
				duration = aop.getOcrProcessMetadata().getDuration();
			}
			file.delete();
			LOGGER.debug("Delete File: "+ file.toString());
			
			try {
				FileUtils.deleteDirectory(file.getParentFile());
				FileUtils.deleteDirectory(new File(localPath + randomNumber));
			} catch (IOException e) {
				LOGGER.error("ERROR CAN NOT delete Directory");
			}

			File f = new File(webserverPath + temp + "/" + jobName
					+ "." + ocrformat.toString().toLowerCase());
			
			if( !f.exists()){
				LOGGER.error("ERROR. CANNOT Find File: "+ f.toString());
				String error = "File could not be processed: " + inputUrl;
				return byURLresponse(webserverPath, error, byUrlResponseType);
			}
			String newLine = ".\n";
			byUrlResponseType.setMessage("Process finished successfully after " + duration + " milliseconds.");
			byUrlResponseType.setOutputUrl(webserverHostname + temp + "/"+ jobName	+ "." + ocrformat.toString().toLowerCase());
			byUrlResponseType.setProcessingLog("========= PROCESSING REQUEST (by URL) =========. "+ "\n" +
												"Using service: OcrServiceImplService. "+ "\n" +
												"Parameter processingUnit: "+ webserverHostname + newLine +
												"URL of input image: "+ request.getInputUrl()+ newLine +
												"Wrote file " + file.toString()+  newLine +
												"OUTFORMAT substitution variable value: "+ocrformat.toString()+ newLine +
												"OUTFILE substitution variable value: " + f.getAbsolutePath()+ newLine +
												"LANGUAGES substitution variable value: "+ langs.toString() + newLine +
												"INFILE substitution variable value: "+ file.toString()+  newLine +
												"INTEXTTYPE substitution variable value: "+ request.getTextType().value()+ newLine +
												"Process finished successfully with code 0."+ "\n" +
												"Output file has been created successfully.."+ "\n" +
												"Output Url: " + webserverHostname + temp + "/" + jobName	+ "." + ocrformat.toString().toLowerCase()+ newLine +
												"Output Url-Abbyy-Result : " + webserverHostname + temp + "/" + jobName	+ ".xml.result.xml" + newLine +
												"Output Url-Summary-File : " + webserverHostname + temp + "/" + jobName	+ "-textMD.xml" + newLine + 
												"Process finished successfully after " + duration + " milliseconds.."
												);
			
			byUrlResponseType.setProcessingUnit(webserverHostname);
			byUrlResponseType.setReturncode(0);
			byUrlResponseType.setSuccess(true);
			byUrlResponseType.setToolProcessingTime(duration);									
		

		return byUrlResponseType;
		
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
