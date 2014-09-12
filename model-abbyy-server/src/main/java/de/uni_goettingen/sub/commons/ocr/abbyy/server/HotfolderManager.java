package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.HotfolderProvider;
import de.uni_goettingen.sub.commons.ocr.api.OcrImage;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.unigoettingen.sub.commons.ocr.util.Pause;

public class HotfolderManager {
	private final static Logger logger = LoggerFactory.getLogger(HotfolderManager.class);
	private Hotfolder hotfolder;
	static Object monitor = new Object();
	private HotfolderProvider hotfolderProvider = new HotfolderProvider();
	private Pause pause = new Pause();

	public HotfolderManager(String serverUrl, String user, String password) {
		hotfolder = hotfolderProvider.createHotfolder(serverUrl, user, password);
	}
	
	// for unit tests
	HotfolderManager(Hotfolder initHotfolder) {
		hotfolder = initHotfolder;
	}
	void setPause(Pause newPause) {
		pause = newPause;
	}

	public void deleteOutputs(List<OcrOutput> outputs) throws IOException {
		for (OcrOutput out : outputs) {
			AbbyyOutput abbyyOut = (AbbyyOutput) out;
			URI remoteUri = abbyyOut.getRemoteUri();
			hotfolder.deleteIfExists(remoteUri);
		}
	}

	public void deleteImages(List<OcrImage> images) throws IOException {
		for (OcrImage ocrImage : images) {
			AbbyyImage image = (AbbyyImage) ocrImage;
			URI remoteUri = image.getRemoteUri();
			hotfolder.deleteIfExists(remoteUri);
			URI errorImageUri = image.getErrorUri();
			hotfolder.deleteIfExists(errorImageUri);
		}
	}

	public void copyImagesToHotfolder(List<OcrImage> ocrImages) throws IOException {
		for (OcrImage ocrImage : ocrImages) {
			AbbyyImage image = (AbbyyImage) ocrImage;
			URI fromUri = image.getLocalUri();
			URI toUri = image.getRemoteUri();
			hotfolder.copyFile(fromUri, toUri);
		}
	}

	public void retrieveResults(List<OcrOutput> ocrOutputs) throws IOException {
		for (OcrOutput entry : ocrOutputs) {
			AbbyyOutput o = (AbbyyOutput) entry;

			URI remoteUri = o.getRemoteUri();
			URI localUri = o.getLocalUri();
			hotfolder.copyFile(remoteUri, localUri);
			hotfolder.deleteIfExists(remoteUri);
		}
	}

	public void createAndSendTicket(AbbyyTicket abbyyTicket, String name) throws IOException, URISyntaxException {
		String ticketFileName = name + ".xml";
		URI inputTicketUri = abbyyTicket.getRemoteInputUri();
		
		synchronized (monitor) {
			OutputStream os = hotfolder.createTmpFile(ticketFileName);
			abbyyTicket.write(os, name);
			os.close();
		}
		
		//TODO: remove
//		URI ticketLogPath = new java.io.File("/home/dennis/temp/tickets/" + ticketFileName).toURI();
//		hotfolder.copyTmpFile(ticketFileName, ticketLogPath);

		hotfolder.copyTmpFile(ticketFileName, inputTicketUri);
		hotfolder.deleteTmpFile(ticketFileName);
	}

	public void waitForResults(long timeout, long waitInterval,
			List<OcrOutput> outputs, URI errorResultXmlUri) throws TimeoutException, IOException {		
		long start = System.currentTimeMillis();
		
		List<URI> mustBeThereUris = extractFromOutputs(outputs);
		
		outerLoop:
		while (true) {

			checkIfError(start, timeout, errorResultXmlUri);

			for (URI expectedUri : mustBeThereUris) {
				if (!hotfolder.exists(expectedUri)) {
					logger.debug(expectedUri.toString() + " is not yet available");
					waitALittle(waitInterval);
					continue outerLoop;
				}
			}
			logger.debug("Got all files.");
			break;
		}
	}

	private List<URI> extractFromOutputs(List<OcrOutput> outputs) {
		List<URI> mustBeThereUris = new ArrayList<URI>();
		for (OcrOutput out : outputs) {
			final AbbyyOutput output = (AbbyyOutput) out;
			URI uri = output.getRemoteUri();
			mustBeThereUris.add(uri);
		}
		return mustBeThereUris;
	}
	
	private void checkIfError(long start, long timeout, URI errorResultXmlUri) throws TimeoutException, IOException {
		if (System.currentTimeMillis() > start + timeout) {
			
			logger.error("Waited too long - fail ");
			throw new TimeoutException();
		}
		if (hotfolder.exists(errorResultXmlUri)) {
			logger.error("Server reported an error in file: " + errorResultXmlUri);
			throw new TimeoutException();
		}
	}
	
	private void waitALittle(long waitInterval) {
		logger.debug("Waiting for " + waitInterval
				+ " milli seconds (" + waitInterval/1000/60 + " minutes)");
		pause.forMilliseconds(waitInterval);
	}

	public void checkIfEnoughSpace(long maxSize, URI inputFolder,
			URI outputFolder, URI errorFolder) throws IOException {
		if (maxSize != 0) {

			long totalFileSize = hotfolder.getTotalSize(inputFolder) 
					+ hotfolder.getTotalSize(outputFolder) 
					+ hotfolder.getTotalSize(errorFolder);

			logger.debug("TotalFileSize = " + totalFileSize);
			if (totalFileSize > maxSize) {
				logger.error("Size of files is too much files. Max size of all files is "
						+ maxSize
						+ ". Size of files on server: "
						+ totalFileSize + ".\nExit program.");
				throw new IllegalStateException("Max size of files exceeded");
			}
		} else {
			logger.warn("Server state checking is disabled.");
		}
	}

	public void deleteTicket(AbbyyTicket abbyyTicket) throws IOException, URISyntaxException {
		hotfolder.deleteIfExists(abbyyTicket.getRemoteInputUri());
		hotfolder.deleteIfExists(abbyyTicket.getRemoteErrorUri());
	}

	//TODO: remove?
	public String readFromErrorFile(URI errorResultUri, String processName) throws IOException, XMLStreamException {
		String errorDescription = "";
		logger.debug("Trying to parse file" + errorResultUri + " (" + processName + ")");
		if (hotfolder.exists(errorResultUri)) {
			XmlParser xmlParser = new XmlParser();
			logger.debug("Trying to parse EXISTS file" + errorResultUri + " (" + processName + ")");
			InputStream is = hotfolder.openInputStream(errorResultUri);
			errorDescription = xmlParser.xmlresultErrorparse(is, processName);
			//hotfolder.deleteIfExists(errorResultUri);
		}
		return errorDescription;
	}

	
}
