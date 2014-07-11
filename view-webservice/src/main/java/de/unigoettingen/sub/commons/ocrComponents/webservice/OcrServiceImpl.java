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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unigoettingen.sub.commons.ocr.util.FileManager;
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
	
	static String ocrEngineId = "abbyy-multiuser";
	
	private final String appName = "ws";
	
	private final static Logger LOGGER = LoggerFactory
			.getLogger(OcrServiceImpl.class);
		
	FileManager getFileManager() {
		return new FileManager();
	}
		
	OcrEngineStarter getEngineStarter() {
		return new OcrEngineStarter();
	}
	
	String getJobName() {
		int randomNumber = Math.abs((int) ((Math.random()*((int) System.currentTimeMillis()))+1));
		return "OcrServiceImplService_outputUrl"+ "_" + randomNumber;
	}

	@Override
	public ByUrlResponseType ocrImageFileByUrl(ByUrlRequestType request) {
		Date stampStart = new Date();
		
		ByUrlResponseType response = new ByUrlResponseType();

		FileManager fileManager = getFileManager();
		
		Properties props = fileManager.getPropertiesFromFile("webservice-config.properties");

		String inputTempDir = props.getProperty("localpath");
		if(inputTempDir == null || inputTempDir.equals("")){
			inputTempDir = System.getProperty("java.io.tmpdir");
		}
		if(!inputTempDir.endsWith("/")){
			inputTempDir += "/";
		}
		
		String webserverPath = props.getProperty("webserverpath");
		
		if(webserverPath == null || webserverPath.equals("")){
			webserverPath = System.getProperty("ocrWebservice.root");
		}
		if(!webserverPath.endsWith("/")){
			webserverPath += "/";
		}

		String jobName = getJobName();

		RecognitionLanguages langs = request.getOcrlanguages();
		List<String> langStringsList = new ArrayList<String>();
		for (RecognitionLanguage lang : langs.getRecognitionLanguage()) {
			langStringsList.add(lang.toString());
		}

		File imageTempFile = new File(inputTempDir  + jobName + "/input.tif");
		try {
			fileManager.copyUrlToFile(request.getInputUrl(), imageTempFile);
		} catch (IOException e) {
			String error = "ERROR CANNOT COPY URL: " + request.getInputUrl()
					+ " To Local File";
			LOGGER.error(error);
			return getErrorResponse(webserverPath, error, response);
		}
		
		OcrParameters params = new OcrParameters();
		params.ocrEngine = ocrEngineId;
		params.outputFormats = new String[]{request.getOutputFormat().toString()};
		params.inputFolder = inputTempDir + jobName;
		params.inputFormats = new String[]{"tif"};
		params.inputLanguages = langStringsList.toArray(new String[]{});
		params.inputTextType = request.getTextType().toString();
		params.priority = "3";
		params.outputFolder = webserverPath + "ocrresults";
		params.props = new Properties();
		
		getEngineStarter().startOcrWithParams(params);
				
		String resultsDir = "ocrresults";
		
		try {
			fileManager.deleteFile(imageTempFile);
			fileManager.deleteDir(imageTempFile.getParentFile());
		} catch (IOException e) {
			LOGGER.error("Error while cleaning temp data.", e);
		}

		File resultFile = new File(webserverPath + resultsDir + "/" + jobName
				+ "." + params.outputFormats[0].toLowerCase());
		
		if( !fileManager.fileExists(resultFile)){
			LOGGER.error("ERROR. CANNOT Find File: "+ resultFile.toString());
			String error = "File could not be processed: " + request.getInputUrl();
			return getErrorResponse(webserverPath, error, response);
		}
		
		Date stampFinish = new Date();
		long duration = stampFinish.getTime() - stampStart.getTime();

		String webserverHostname = "";
		if(props.getProperty("hostname") == null || props.getProperty("hostname").equals("no")){
			MessageContext mc = wsContext.getMessageContext();
			URI url = (URI) mc.get("javax.xml.ws.wsdl.description");
			String hostname = url.getHost();
			webserverHostname = "http://" + hostname + "/" + appName + "/";
		}else {
			webserverHostname = props.getProperty("hostname");
		}

		String newLine = ".\n";
		response.setMessage("Process finished successfully after " + duration + " milliseconds.");
		response.setOutputUrl(webserverHostname + resultsDir + "/"+ jobName	+ "." + params.outputFormats[0].toLowerCase());
		response.setProcessingLog("========= PROCESSING REQUEST (by URL) =========. "+ "\n" +
											"Using service: OcrServiceImplService. "+ "\n" +
											"Parameter processingUnit: "+ webserverHostname + newLine +
											"URL of input image: "+ request.getInputUrl()+ newLine +
											"Wrote file " + imageTempFile.toString()+  newLine +
											"OUTFORMAT substitution variable value: "+params.outputFormats[0].toLowerCase()+ newLine +
											"OUTFILE substitution variable value: " + resultFile.getAbsolutePath()+ newLine +
											"LANGUAGES substitution variable value: "+ params.inputLanguages + newLine +
											"INFILE substitution variable value: "+ imageTempFile.toString()+  newLine +
											"INTEXTTYPE substitution variable value: "+ request.getTextType().value()+ newLine +
											"Process finished successfully with code 0."+ "\n" +
											"Output file has been created successfully.."+ "\n" +
											"Output Url: " + webserverHostname + resultsDir + "/" + jobName	+ "." + params.outputFormats[0].toLowerCase()+ newLine +
											"Output Url-Abbyy-Result : " + webserverHostname + resultsDir + "/" + jobName	+ ".xml.result.xml" + newLine +
											"Output Url-Summary-File : " + webserverHostname + resultsDir + "/" + jobName	+ "-textMD.xml" + newLine + 
											"Process finished successfully after " + duration + " milliseconds.."
											);
		
		response.setProcessingUnit(webserverHostname);
		response.setReturncode(0);
		response.setSuccess(true);
		response.setToolProcessingTime(duration);									
	

		return response;
		
	}
	
	private ByUrlResponseType getErrorResponse(String webserverPath, String error, ByUrlResponseType byUrlResponseType) {
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
