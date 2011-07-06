package de.uni_goettingen.sub.commons.ocr.abbyy.server;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.AbstractHotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcess;
import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;
import de.unigoettingen.sub.commons.ocr.util.OCRUtil;

/**
 * The Class AbbyyServerOCREngine. The Engine is also the entry point for
 * different Hotfolder implementations, You can change the implementation
 * indirectly by changing the given configuration. Just construct an empty
 * configuration or create one from a configuration file and call the method
 * {@link ConfigParser.setHotfolderClass()}.
 */

public class AbbyyServerOCREngine extends AbstractOCREngine implements
		OCREngine {
	public static final String name = "0.5";
	public static final String version = AbbyyServerOCREngine.class
			.getSimpleName();
	protected int divNumber, restNumber, splitNumberForSubProcess, imagesNumberForSubprocess = 15;
	// The max threads.
	protected static Integer maxThreads;
	// protected ExecutorService pool = new OCRExecuter(maxThreads);
	// The done date.
	protected Long startTimeForProcess = null;
	protected AbbyySerializerTextMD abbyySerializerTextMD;
	// The done date.
	protected Long endTimeForProcess = null;
	/** The Constant logger. */
	final static Logger logger = LoggerFactory
			.getLogger(AbbyyServerOCREngine.class);

	// The configuration.
	protected static ConfigParser config;

	protected Hotfolder hotfolder;
	protected OCRProcessMetadata ocrProcessMetadata;
	/** single instance of AbbyyServerOCREngine. */
	private static AbbyyServerOCREngine _instance;

	// The check server state.
	protected static Boolean checkServerState = true;
	protected static Boolean rest = false;

	// OCR Processes
	protected Queue<AbbyyOCRProcess> processes = new ConcurrentLinkedQueue<AbbyyOCRProcess>();
	
	protected HazelcastInstance h = Hazelcast.newHazelcastInstance(null);
	/**
	 * Instantiates a new abbyy server engine.
	 * 
	 * @throws FileSystemException
	 *             the file system exception
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private AbbyyServerOCREngine() throws ConfigurationException {
		config = new ConfigParser().parse();
		hotfolder = AbstractHotfolder.getHotfolder(config.hotfolderClass,
				config);
		maxThreads = config.getMaxThreads();
		checkServerState = config.getCheckServerState();
	}

	/**
	 * Start the threadPooling
	 * 
	 */
	protected void start() {
		started = true;
		ExecutorService pool = new OCRExecuter(maxThreads, hotfolder, h);

		for (OCRProcess process : getOcrProcess()) {
			AbbyyOCRProcess p = (AbbyyOCRProcess) process;
			processes.add(p);

		}
		// pool.e

		for (AbbyyOCRProcess p : processes) {
			ocrProcessMetadata = new AbbyyOCRProcessMetadata();
			startTimeForProcess = System.currentTimeMillis();
			ocrProcessMetadata.setEncoding("UTF-8");
			StringBuffer sbProcessingNote = new StringBuffer();
			BigDecimal totalChar = new BigDecimal(0), totalUncerChar = new BigDecimal(0);
			List <String> urlLocalforSubProcess = new ArrayList<String>();
			Set <String> outpuFormat = new HashSet<String>();
			if(p.getOcrImages().size() > (imagesNumberForSubprocess + imagesNumberForSubprocess/2)){		
				divNumber = p.getOcrImages().size()/imagesNumberForSubprocess;
				restNumber= (p.getOcrImages().size() % imagesNumberForSubprocess);
				if(restNumber >= imagesNumberForSubprocess/2){
					splitNumberForSubProcess = divNumber + 1;
				}else splitNumberForSubProcess = divNumber;				
				int imageCounters = 1;
				int subProcessCounters = 1;
				int once = 1;
				List<OCRImage> imgs = new ArrayList<OCRImage>();
				Map<OCRFormat, OCROutput> outputs = p.getOcrOutputs();				
				for(OCRImage ocrimage : p.getOcrImages()){
					if(splitNumberForSubProcess > subProcessCounters){
						if(imageCounters <= imagesNumberForSubprocess){														
								OCRImage aoi = newOcrImage(ocrimage.getUri());								
								imgs.add(aoi);							
								if(imageCounters == imagesNumberForSubprocess){
									imageCounters = 0;								
									OCRProcess ocrp = newOcrProcess();
									ocrp.setOcrImages(imgs);
									ocrp.setName(p.getName()+ "_" + subProcessCounters + "oF" + splitNumberForSubProcess);									
									StringBuffer sbformat = new StringBuffer();
									for (OCRFormat f : outputs.keySet()) {
										final AbbyyOCROutput o = (AbbyyOCROutput) outputs
												.get(f);									
										OCROutput aoo = newOcrOutput();
										URI localUri = o.getUri();
										String l = localUri.toString().replace(p.getName(), ocrp.getName());
										urlLocalforSubProcess.add(l);
										outpuFormat.add(f.toString());
										try {
											localUri = new URI(l);
										} catch (URISyntaxException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										aoo.setUri(localUri);
										ocrp.addOutput(f, aoo);
										if(once == 1){
											sbformat.append(f.toString() + " ");
										}	
									}
									if(once == 1){
										ocrProcessMetadata.setFormat(sbformat.toString());
										List<Locale> lang = new ArrayList<Locale>();
										for (Locale l : p.getLanguages()) {
											lang.add(l);
										}
										ocrProcessMetadata.setLanguages(lang);
										once++;
									}
									ocrp.setSegmentation(true);
									ocrp.setLanguages(p.getLanguages());
									ocrp.setPriority(p.getPriority());
									ocrp.setTextTyp(p.getTextTyp());
									ocrp.setTime(new Date().getTime());
									pool.execute((AbbyyOCRProcess)ocrp);
									imgs = new ArrayList<OCRImage>();
									subProcessCounters++;
								}										
							imageCounters++;							
						}
					}else{
							OCRImage aoi = newOcrImage(ocrimage.getUri());								
							imgs.add(aoi);
							rest = true;
						}					
					}
					if(rest){
						OCRProcess ocrp = newOcrProcess();
						ocrp.setOcrImages(imgs);
						ocrp.setName(p.getName()+ "_" + subProcessCounters + "oF" + splitNumberForSubProcess);
						for (OCRFormat f : outputs.keySet()) {
							final AbbyyOCROutput o = (AbbyyOCROutput) outputs
									.get(f);									
							OCROutput aoo = newOcrOutput();
							URI localUri = o.getUri();
							String l = localUri.toString().replace(p.getName(), ocrp.getName());
							urlLocalforSubProcess.add(l);
							outpuFormat.add(f.toString());
							try {
								localUri = new URI(l);
							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							aoo.setUri(localUri);
							ocrp.addOutput(f, aoo);
						}
						ocrp.setSegmentation(true);
						ocrp.setLanguages(p.getLanguages());
						ocrp.setPriority(p.getPriority());
						ocrp.setTextTyp(p.getTextTyp());
						ocrp.setTime(new Date().getTime());
						pool.execute((AbbyyOCRProcess)ocrp);
						imgs = new ArrayList<OCRImage>();
					}
					//Merge Metadata and Results
					outpuFormat.add("xml"+ config.reportSuffix);
					
					if(localFileExists(urlLocalforSubProcess, outpuFormat)){						
						List<InputStream> reports = null;
						boolean control = true;
						for(String o : outpuFormat){				
							for(String u : urlLocalforSubProcess){
								//Merge Metadata
								if (o.toLowerCase().equals("xml"+config.reportSuffix)) {
									InputStream isResult = null;
									try {
										isResult = new FileInputStream(new File(u));
										reports.add(isResult);
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}									
									((AbbyyOCRProcessMetadata) ocrProcessMetadata)
											.parseXmlResult(isResult);							 
									totalChar = totalChar.add(((AbbyyOCRProcessMetadata) ocrProcessMetadata).getTotalChar()) ;
									totalUncerChar = totalUncerChar.add(((AbbyyOCRProcessMetadata) ocrProcessMetadata).getTotalUncerChar());
									sbProcessingNote.append(ocrProcessMetadata.getProcessingNote());
								}
								
								if (o.toLowerCase().equals("xml")) {
									InputStream isDoc = null;									
									try {
										isDoc = new FileInputStream(new File(u + "." + o.toLowerCase()));
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if(control){
										((AbbyyOCRProcessMetadata) ocrProcessMetadata)
										.parseXmlExport(isDoc);
										control = false;
									}
									
									
								}
								if (o.toLowerCase().equals("txt")) {
									
								}
								
							}		
						}	
						ocrProcessMetadata.setCharacterAccuracy(totalChar, totalUncerChar);
						ocrProcessMetadata.setProcessingNote(sbProcessingNote.toString());
						//TODO Merge Results
						//serializerTextMD(ocrProcessMetadata, p.getName());
						
					}
					
			}else{
				p.setTime(new Date().getTime());
				pool.execute(p);
			}		
		}

		
		pool.shutdown();
		try {
			pool.awaitTermination(3600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Got a problem with thread pool: ", e);
		}
		h.shutdown();
	}

	private void serializerTextMD(OCRProcessMetadata ocrProcessMetadata,
			String textMD) {
		abbyySerializerTextMD = new AbbyySerializerTextMD(ocrProcessMetadata);
		logger.debug("Creating " + name + "-textMD.xml");
		
		URI urii;
		try {
			String localUrl = "C:/Dokumente und Einstellungen/mabergn.UG-SUB/workspace-ocr/ocr-tools/ocr-cli/target/results/";
			urii = new URI(localUrl +"/"+ textMD+  "-textMD.xml");
			abbyySerializerTextMD.write(new File(urii));
			logger.debug("TextMD Created " + urii.toString());
		} catch (URISyntaxException e) {
			logger.error("CAN NOT Copying Serializer textMD to local " + name
					+ "-textMD.xml", e);
		}
	}
	
	private boolean localFileExists(List<String> url, Set <String> outpuFormat){
		boolean exists = false;
		for(String u : url){
			File file = new File(u);
			File fileReport = new File(u.replace("xml", "xml.result.xml"));
			if(file.exists() && fileReport.exists()){
				exists = true;
			}else {
				exists = true;
				break;
			}
					
		}			
		return exists;
	}
	
	
	
	/**
	 * Gets the single instance of AbbyyServerOCREngine.
	 * 
	 * @return single instance of AbbyyServerOCREngine
	 * 
	 */

	public static AbbyyServerOCREngine getInstance() {

		if (_instance == null) {
			try {
				_instance = new AbbyyServerOCREngine();
			} catch (ConfigurationException e) {
				logger.error("Can't read configuration", e);
				throw new OCRException(e);
			}
		}
		return _instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#newOcrImage(java
	 * .net.URI)
	 */
	@Override
	public OCRImage newOcrImage(URI imageUri) {
		return new AbbyyOCRImage(imageUri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#newOcrProcess()
	 */
	@Override
	public OCRProcess newOcrProcess() {
		return new AbbyyOCRProcess(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine#newOcrOutput()
	 */
	@Override
	public OCROutput newOcrOutput() {
		return new AbbyyOCROutput();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_goettingen.sub.commons.ocr.api.OCREngine#recognize(de.uni_goettingen
	 * .sub.commons.ocr.api.OCRProcess)
	 */
	@Override
	public Observable recognize(OCRProcess process) {
		Observable o = addOcrProcess(process);
		// TODO: Get an Observer from somewhere, probably use a Future
		recognize();
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#recognize()
	 */
	@Override
	public Observable recognize() {
		if (!started && !processes.isEmpty()) {
			start();
		} else if (processes.isEmpty()) {
			throw new IllegalStateException("Queue is empty!");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_goettingen.sub.commons.ocr.api.OCREngine#addOcrProcess(de.
	 * uni_goettingen.sub.commons.ocr.api.OCRProcess)
	 */
	@Override
	public Observable addOcrProcess(OCRProcess process) {
		// TODO: Check if this instanceof works as expected
		if (process instanceof AbbyyOCRProcess) {
			processes.add((AbbyyOCRProcess) process);
		} else {
			processes.add(new AbbyyOCRProcess(process, config));
		}
		return null;
	}

	/**
	 * Creates the process from directory.
	 * 
	 * @param directory
	 *            the directory
	 * @param extension
	 *            the extension
	 * @return the abbyy ocr process
	 */
	public static AbbyyOCRProcess createProcessFromDir(File directory,
			String extension) {
		AbbyyOCRProcess ap = new AbbyyOCRProcess(config);
		List<File> imageDirs = OCRUtil.getTargetDirectories(directory,
				extension);

		for (File id : imageDirs) {
			if (imageDirs.size() > 1) {
				logger.error("Directory " + directory.getAbsolutePath()
						+ " contains more then one image directories");
				throw new OCRException(
						"createProcessFromDir can currently create only one AbbyyOCRProcess!");
			}
			String jobName = id.getName();
			for (File imageFile : OCRUtil.makeFileList(id, extension)) {
				ap.setName(jobName);
				// Remote URL isn't set here because we don't know it yet.
				AbbyyOCRImage aoi = new AbbyyOCRImage(imageFile.toURI());
				aoi.setSize(imageFile.length());
				ap.addImage(aoi);
			}
			ap.processTimeout = (long) ap.getOcrImages().size()
					* ap.config.maxMillisPerFile;
		}

		return ap;
	}

	@Override
	public Boolean init() {
		// TODO: check server connection here
		return true;
	}

	@Override
	public Boolean stop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

}
