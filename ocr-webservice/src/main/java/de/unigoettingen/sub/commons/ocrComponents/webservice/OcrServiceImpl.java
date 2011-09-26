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
import java.net.URLConnection;
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
	
	private final String APP_NAME = "ocr-webservice";
	
	/** The logger. */
	protected static Logger logger = LoggerFactory
			.getLogger(OcrServiceImpl.class);
		
	
	private URL getUrl(ByUrlRequestType request) throws IOException {
		
		String inputUrlString = request.getInputUrl();
		URL inputUrl = new URL(inputUrlString);
		URLConnection uconn = inputUrl.openConnection();
		String type = uconn.getContentType();

		if (!type.equals("image/tiff"))
			throw new IOException("Not a Tiff image! Was " + type);
		
		return inputUrl;
	}
	
	@Override
	public ByUrlResponseType ocrImageFileByUrl(ByUrlRequestType request) {
		ByUrlResponseType byUrlResponseType = new ByUrlResponseType();
		OCREngine engine;
		String WEBSERVER_PATH,WEBSERVER_HOSTNAME,LOCAL_PATH, jobName;
		int randomNumber;
		Set<Locale> langs;
		HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;
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
			logger.error("Error reading configuration", e);
		}
		
		LOCAL_PATH = properties.getProperty("localpath");
		if(LOCAL_PATH == null || LOCAL_PATH.equals("")){
			LOCAL_PATH = System.getProperty("java.io.tmpdir");
		}
		if(!LOCAL_PATH.endsWith("/")){
			LOCAL_PATH = LOCAL_PATH + "/";
		}
		
		WEBSERVER_PATH = properties.getProperty("webserverpath");
		
		if(WEBSERVER_PATH == null || WEBSERVER_PATH.equals("")){
			WEBSERVER_PATH = System.getProperty("ocrWebservice.root");
		}


		URL inputUrl = null;
		try {
			inputUrl = getUrl(request);
		} catch (IOException e) {
			String error = "URL Error: " + e.getMessage();
			logger.error(error);
			return byURLresponse(WEBSERVER_PATH, error, byUrlResponseType);
		}

		engine = ocrEngineFactory.newOcrEngine();
		OCRProcess aop = engine.newOcrProcess();

		OCRFormat ocrformat = request.getOutputFormat();
		
			List<OCRImage> imgs = new ArrayList<OCRImage>();

			randomNumber = Math.abs((int) ((Math.random()*((int) System.currentTimeMillis()))+1));
			
			jobName = "OcrServiceImplService_outputUrl"+ "_" + randomNumber;
			File file = new File(LOCAL_PATH + randomNumber + "/" + jobName
					+ "/" + randomNumber + ".tif");

			try {
				FileUtils.copyURLToFile(inputUrl, file);
			} catch (IOException e) {
				logger.error("ERROR CAN NOT COPY URL To File");
				String error = "ERROR CAN NOT COPY URL: " + request.getInputUrl()
						+ " To Local File";
				return byURLresponse(WEBSERVER_PATH, error, byUrlResponseType);
			}

			aop.setName(jobName);
			OCRImage aoi = engine.newOcrImage(file.toURI());
			aoi.setSize(file.length());
			imgs.add(aoi);
			aop.setOcrImages(imgs);

			// add format
			OUTPUT_DEFINITIONS = new HashMap<OCRFormat, OCROutput>();

			OCROutput aoo = engine.newOcrOutput();
			URI uri = null;
			
			try {
				uri = new URI(new File(WEBSERVER_PATH).toURI()+ "/" + "temp"
						+ "/" + jobName + "."
						+ ocrformat.toString().toLowerCase());
			} catch (URISyntaxException e) {
				logger.error("URL is Mal formed: " + WEBSERVER_PATH 
						+ "temp" + "/" + jobName + "."
						+ ocrformat.toString().toLowerCase());
				String error = "URL is Mal formed: " + WEBSERVER_PATH 
				+ "temp" + "/" + jobName + "."
				+ ocrformat.toString().toLowerCase();
				file.delete();
				return byURLresponse(WEBSERVER_PATH, error, byUrlResponseType);
			}
			
			aoo.setUri(uri);
			OUTPUT_DEFINITIONS.put(ocrformat, aoo);
			aop.addOutput(ocrformat, aoo);

			if(properties.getProperty("hostname").equals("no") || properties.getProperty("hostname").equals(null)){
				  MessageContext mc = wsContext.getMessageContext();
				  URI url = (URI)mc.get("javax.xml.ws.wsdl.description");
				  String hostname = url.getHost();
				  WEBSERVER_HOSTNAME = "http://"+hostname+"/"+APP_NAME+"/"; 
			  }else WEBSERVER_HOSTNAME = properties.getProperty("hostname");
			  
			
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

			logger.info("Starting recognize method");
			engine.recognize();
			
			if(aop.getOcrProcessMetadata().getDuration() != 0L){
				duration = aop.getOcrProcessMetadata().getDuration();
			}
			file.delete();
			logger.debug("Delete File: "+ file.toString());
			
			try {
				FileUtils.deleteDirectory(file.getParentFile());
				FileUtils.deleteDirectory(new File(LOCAL_PATH + randomNumber));
			} catch (IOException e) {
				logger.error("ERROR CAN NOT deleteDirectory");
			}

			File f = new File(WEBSERVER_PATH + "temp" + "/" + jobName
					+ "." + ocrformat.toString().toLowerCase());
			
			if( !f.exists()){
				logger.error("ERROR File CAN NOT Find: "+ f.toString());
				String error = "ERROR File CAN NOT Find: "+ f.toString();
				return byURLresponse(WEBSERVER_PATH, error, byUrlResponseType);
			}
			
			byUrlResponseType.setMessage("Process finished successfully after " + duration + " milliseconds.");
			byUrlResponseType.setOutputUrl(WEBSERVER_HOSTNAME + "temp" + "/"+ jobName	+ "." + ocrformat.toString().toLowerCase());
			byUrlResponseType.setProcessingLog("========= PROCESSING REQUEST (by URL) =========. "+ "\n" +
												"Using service: OcrServiceImplService. "+ "\n" +
												"Parameter processingUnit: "+ WEBSERVER_HOSTNAME + ".\n" +
												"URL of input image: "+ request.getInputUrl()+ ".\n" +
												"Wrote file " + file.toString()+  ".\n" +
												"OUTFORMAT substitution variable value: "+ocrformat.toString()+ ".\n" +
												"OUTFILE substitution variable value: " + f.getAbsolutePath()+ ".\n" +
												"LANGUAGES substitution variable value: "+ langs.toString() + ".\n" +
												"INFILE substitution variable value: "+ file.toString()+  ".\n" +
												"INTEXTTYPE substitution variable value: "+ request.getTextType().value()+ ".\n" +
												"Process finished successfully with code 0."+ "\n" +
												"Output file has been created successfully.."+ "\n" +
												"Output Url: " + WEBSERVER_HOSTNAME + "temp" + "/" + jobName	+ "." + ocrformat.toString().toLowerCase()+ ".\n" +
												"Output Url-Abbyy-Result : " + WEBSERVER_HOSTNAME + "temp" + "/" + jobName	+ ".xml.result.xml" + ".\n" +
												"Output Url-Summary-File : " + WEBSERVER_HOSTNAME + "temp" + "/" + jobName	+ "-textMD.xml" + ".\n" + 
												"Process finished successfully after " + duration + " milliseconds.."
												);
			
			byUrlResponseType.setProcessingUnit(WEBSERVER_HOSTNAME);
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
