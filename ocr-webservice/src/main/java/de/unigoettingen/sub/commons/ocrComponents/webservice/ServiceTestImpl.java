package de.unigoettingen.sub.commons.ocrComponents.webservice;


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
import java.util.Map;
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



@WebService(endpointInterface = "de.unigoettingen.sub.commons.ocrComponents.webservice.ServiceTest")
public class ServiceTestImpl implements ServiceTest {
	
	/** The logger. */
	protected static Logger logger = LoggerFactory
			.getLogger(ServiceTestImpl.class);
	/** The engine. */
	protected static OCREngine engine;
	protected static String WEBSERVER_PATH;
	protected static String LOCAL_PATH;
	/** The language. */
	protected static Set<Locale> langs;
	/** The OUTPU t_ definitions. */
	protected static HashMap<OCRFormat, OCROutput> OUTPUT_DEFINITIONS;
	private String parent, jobName;
	
	// The done date.
	private Long startTime = null;
	
	// The done date.
	private Long endTime = null;
	
	// The done date.
	private Long duration = null;
	
	ByUrlResponseType byUrlResponseType;
	public final static Map<RecognitionLanguage, Locale> LANGUAGE_MAP = new HashMap<RecognitionLanguage, Locale>();

	static {
		LANGUAGE_MAP.put(RecognitionLanguage.ABKHAZIAN , new Locale("ab"));	LANGUAGE_MAP.put(RecognitionLanguage.AZERBAIJANI, new Locale("az"));
		LANGUAGE_MAP.put(RecognitionLanguage.AFRIKANNS, new Locale("af")); 	LANGUAGE_MAP.put(RecognitionLanguage.AMHARIC, new Locale("am"));
		LANGUAGE_MAP.put(RecognitionLanguage.AYMARA, new Locale("ay")); 	
		
		
		
		LANGUAGE_MAP.put(RecognitionLanguage.BASHKIR, new Locale("ba")); 	LANGUAGE_MAP.put(RecognitionLanguage.BYELORUSSIAN, new Locale("be"));
		LANGUAGE_MAP.put(RecognitionLanguage.BULGARIAN, new Locale("bg"));	LANGUAGE_MAP.put(RecognitionLanguage.BRETON, new Locale("br"));
		
		
		LANGUAGE_MAP.put(RecognitionLanguage.CATALAN, new Locale("ca")); 	LANGUAGE_MAP.put(RecognitionLanguage.CORSICAN, new Locale("co"));
		LANGUAGE_MAP.put(RecognitionLanguage.CZECH, new Locale("cs")); 		LANGUAGE_MAP.put(RecognitionLanguage.WELSH, new Locale("cy"));
		
		LANGUAGE_MAP.put(RecognitionLanguage.DANISH, new Locale("da")); 	LANGUAGE_MAP.put(RecognitionLanguage.GERMAN, new Locale("de"));
		
		
		LANGUAGE_MAP.put(RecognitionLanguage.GREEK, new Locale("el"));		LANGUAGE_MAP.put(RecognitionLanguage.ENGLISH, new Locale("en"));
		LANGUAGE_MAP.put(RecognitionLanguage.SPANISH, new Locale("es"));    LANGUAGE_MAP.put(RecognitionLanguage.ESTONIAN, new Locale("et")); 	
		LANGUAGE_MAP.put(RecognitionLanguage.BASQUE, new Locale("eu"));
		
		
		LANGUAGE_MAP.put(RecognitionLanguage.FINNISH, new Locale("fi"));    LANGUAGE_MAP.put(RecognitionLanguage.FAROESE, new Locale("fo"));
		LANGUAGE_MAP.put(RecognitionLanguage.FRENCH, new Locale("fr")); 	LANGUAGE_MAP.put(RecognitionLanguage.FRISIAN, new Locale("fy"));
		
		
		LANGUAGE_MAP.put(RecognitionLanguage.IRISH, new Locale("ga"));		LANGUAGE_MAP.put(RecognitionLanguage.SCOTS_GAELIC, new Locale("gd"));
		LANGUAGE_MAP.put(RecognitionLanguage.GALICIAN, new Locale("gl")); 	LANGUAGE_MAP.put(RecognitionLanguage.GUARANI, new Locale("gn"));
		LANGUAGE_MAP.put(RecognitionLanguage.GUJARATI, new Locale("gu")); 	
		
		LANGUAGE_MAP.put(RecognitionLanguage.HAUSA, new Locale("ha"));		LANGUAGE_MAP.put(RecognitionLanguage.HEBREW, new Locale("he"));
		LANGUAGE_MAP.put(RecognitionLanguage.CROATIAN, new Locale("hr"));	LANGUAGE_MAP.put(RecognitionLanguage.ARMENIAN, new Locale("hy"));
		LANGUAGE_MAP.put(RecognitionLanguage.HUNGARIAN, new Locale("hu")); 	
				
		LANGUAGE_MAP.put(RecognitionLanguage.INDONESIAN, new Locale("id")); LANGUAGE_MAP.put(RecognitionLanguage.ITALIAN, new Locale("it"));
				
		LANGUAGE_MAP.put(RecognitionLanguage.JAPANESE, new Locale("ja"));		
				
		LANGUAGE_MAP.put(RecognitionLanguage.KOREAN, new Locale("ko"));		LANGUAGE_MAP.put(RecognitionLanguage.KURDISH, new Locale("ku"));
		LANGUAGE_MAP.put(RecognitionLanguage.KIRGHIZ, new Locale("ky")); 	LANGUAGE_MAP.put(RecognitionLanguage.KAZAKH, new Locale("kk"));
				
		LANGUAGE_MAP.put(RecognitionLanguage.LATIN, new Locale("la")); 		LANGUAGE_MAP.put(RecognitionLanguage.LITHUANIAN, new Locale("lt"));	
		LANGUAGE_MAP.put(RecognitionLanguage.LATVIAN, new Locale("lv")); 	
		
		LANGUAGE_MAP.put(RecognitionLanguage.MALAGASY, new Locale("mg"));	LANGUAGE_MAP.put(RecognitionLanguage.MAORI, new Locale("mi"));
		LANGUAGE_MAP.put(RecognitionLanguage.MACEDONIAN, new Locale("mk")); LANGUAGE_MAP.put(RecognitionLanguage.MALAY, new Locale("ms"));	
		LANGUAGE_MAP.put(RecognitionLanguage.MONGOLIAN, new Locale("mn")); 	LANGUAGE_MAP.put(RecognitionLanguage.MOLDAVIAN, new Locale("mo"));
		LANGUAGE_MAP.put(RecognitionLanguage.MALTESE, new Locale("mt")); 		
				
		LANGUAGE_MAP.put(RecognitionLanguage.DUTCH, new Locale("nl")); 		LANGUAGE_MAP.put(RecognitionLanguage.NORWEGIAN, new Locale("no"));
			
		LANGUAGE_MAP.put(RecognitionLanguage.OCCITAN, new Locale("oc"));		
			
		LANGUAGE_MAP.put(RecognitionLanguage.POLISH, new Locale("pl"));		LANGUAGE_MAP.put(RecognitionLanguage.PORTUGUESE, new Locale("pt"));			
		
		LANGUAGE_MAP.put(RecognitionLanguage.QUECHUA, new Locale("qu"));
		
		LANGUAGE_MAP.put(RecognitionLanguage.RHARTO_ROMANCE, new Locale("rm"));  LANGUAGE_MAP.put(RecognitionLanguage.RUSSIAN, new Locale("ru"));
		LANGUAGE_MAP.put(RecognitionLanguage.ROMANIAN, new Locale("ro"));		
				
		LANGUAGE_MAP.put(RecognitionLanguage.SLOVAK, new Locale("sk"));		LANGUAGE_MAP.put(RecognitionLanguage.SWEDISH, new Locale("sv"));
		LANGUAGE_MAP.put(RecognitionLanguage.SLOVENIAN, new Locale("sl"));	LANGUAGE_MAP.put(RecognitionLanguage.SAMOAN, new Locale("sm"));
		LANGUAGE_MAP.put(RecognitionLanguage.SHONA, new Locale("sn")); 		LANGUAGE_MAP.put(RecognitionLanguage.SOMALI, new Locale("so"));
		LANGUAGE_MAP.put(RecognitionLanguage.ALBANIAN, new Locale("sq")); 	LANGUAGE_MAP.put(RecognitionLanguage.SERBIAN, new Locale("sr"));
		LANGUAGE_MAP.put(RecognitionLanguage.SWAHILI, new Locale("sw"));
				
		LANGUAGE_MAP.put(RecognitionLanguage.TAJIK, new Locale("tg")); 		LANGUAGE_MAP.put(RecognitionLanguage.THAI, new Locale("th"));
		LANGUAGE_MAP.put(RecognitionLanguage.TURKMEN, new Locale("tk"));	LANGUAGE_MAP.put(RecognitionLanguage.TAGALOG, new Locale("tl"));	
		LANGUAGE_MAP.put(RecognitionLanguage.TONGA, new Locale("to")); 		LANGUAGE_MAP.put(RecognitionLanguage.TURKISH, new Locale("tr"));
		LANGUAGE_MAP.put(RecognitionLanguage.TATAR, new Locale("tt"));
			
		LANGUAGE_MAP.put(RecognitionLanguage.UIGHUR, new Locale("ug"));		LANGUAGE_MAP.put(RecognitionLanguage.UKRAINIAN, new Locale("uk"));
		LANGUAGE_MAP.put(RecognitionLanguage.UZBEK, new Locale("uz"));
				
		LANGUAGE_MAP.put(RecognitionLanguage.WOLOF, new Locale("wo"));
		
		LANGUAGE_MAP.put(RecognitionLanguage.XHOSA, new Locale("xh"));
		
		LANGUAGE_MAP.put(RecognitionLanguage.YIDDISH, new Locale("yi")); 		
				
		LANGUAGE_MAP.put(RecognitionLanguage.ZULU, new Locale("zu"));
		LANGUAGE_MAP.put(RecognitionLanguage.CHINESE, new Locale("zh"));
		

	}
	
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
		engine = ocrEngineFactory.newOcrEngine();
		OCRProcess aop = engine.newOcrProcess();
		startTime = System.currentTimeMillis();
		if (part1.getInputUrl() != null
				&& part1.getInputUrl().startsWith("http")
				&& part1.getInputUrl().endsWith("tif")) {
			List<OCRImage> imgs = new ArrayList<OCRImage>();
			URL inputuri = null;
			String[] urlParts;

			try {
				inputuri = new URL(part1.getInputUrl());
			} catch (MalformedURLException e) {
				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				logger.error("URL is Mal formed: " + part1.getInputUrl());
				String error = "URL is Mal formed: " + part1.getInputUrl();
				return byURLresponse(duration, error);
			}

			urlParts = inputuri.toString().split("/");
			parent = urlParts[urlParts.length - 2];
			jobName = urlParts[urlParts.length - 1].replace(".tif", "");
			File file = new File(LOCAL_PATH + "/" + parent + "/" + jobName
					+ "/" + urlParts[urlParts.length - 1]);

			try {
				FileUtils.copyURLToFile(inputuri, file);
			} catch (IOException e) {
				logger.error("ERROR CAN NOT COPY URL To File");
				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
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
				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				return byURLresponse(duration, error);
			}
			// logger.debug("output Location " + uri.toString());
			aoo.setUri(uri);
			OUTPUT_DEFINITIONS.put(ocrformat, aoo);
			aop.addOutput(ocrformat, aoo);
			langs = new HashSet<Locale>();
			for (RecognitionLanguage r : part1.getOcrlanguages()
					.getRecognitionLanguage()) {
				langs.add(LANGUAGE_MAP.get(r));
			}
			aop.setLanguages(langs);
			aop.setPriority(OCRPriority.fromValue(part1.getOcrPriorityType()
					.value()));
			aop.setTextTyp(OCRTextTyp.fromValue(part1.getTextType().value()));
			engine.addOcrProcess(aop);

			logger.info("Starting recognize method");
			engine.recognize();
			file.delete();
			logger.debug("Delete File: "+ file.toString());
			try {
				FileUtils.deleteDirectory(file.getParentFile());
				FileUtils.deleteDirectory(new File(LOCAL_PATH + "/" + parent));
			} catch (IOException e) {
				logger.error("ERROR CAN NOT deleteDirectory");
			}
			endTime = System.currentTimeMillis();
			File f = new File(WEBSERVER_PATH + "/" + parent + "/" + jobName
					+ "." + ocrformat.toString().toLowerCase());
			if( f.exists()){
				duration = endTime - startTime;
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
			}else {
				duration = endTime - startTime;
				logger.error("ERROR File CAN NOT: "+ f.toString());
				String error = "ERROR File CAN NOT: "+ f.toString();
				return byURLresponse(duration, error);
			}
		} else {
			duration = endTime - startTime;
			String error = "ERROR: " + part1.getInputUrl()
					+ " is null or No URL or no Image from type tif";
			return byURLresponse(duration, error);
		}

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
