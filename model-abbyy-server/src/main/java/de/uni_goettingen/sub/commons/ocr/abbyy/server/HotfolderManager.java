package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class HotfolderManager {

	private Hotfolder hotfolder;
	static Object monitor = new Object();

	public HotfolderManager(Hotfolder initHotfolder) {
		hotfolder = initHotfolder;
	}

	public void deleteOutputs(Map<OCRFormat, OCROutput> outputs) throws IOException {
		for (Map.Entry<OCRFormat, OCROutput> entry : outputs.entrySet()) {
			AbbyyOCROutput out = (AbbyyOCROutput) entry.getValue();
			URI remoteUri = out.getRemoteUri();
			hotfolder.deleteIfExists(remoteUri);
		}
	}

	public void deleteImages(List<OCRImage> images) throws IOException {
		for (OCRImage ocrImage : images) {
			AbbyyOCRImage image = (AbbyyOCRImage) ocrImage;
			URI remoteUri = image.getRemoteUri();
			hotfolder.deleteIfExists(remoteUri);
			URI errorImageUri = image.getErrorUri();
			hotfolder.deleteIfExists(errorImageUri);
		}
	}

	public void copyImagesToHotfolder(List<OCRImage> ocrImages) throws IOException {
		for (OCRImage ocrImage : ocrImages) {
			AbbyyOCRImage image = (AbbyyOCRImage) ocrImage;
			URI fromUri = image.getUri();
			URI toUri = image.getRemoteUri();
			hotfolder.copyFile(fromUri, toUri);
		}
	}

	public void retrieveResults(Map<OCRFormat, OCROutput> ocrOutputs) throws IOException {
		for (Map.Entry<OCRFormat, OCROutput> entry : ocrOutputs.entrySet()) {
			AbbyyOCROutput o = (AbbyyOCROutput) entry.getValue();

			URI remoteUri = o.getRemoteUri();
			URI localUri = o.getUri();
			hotfolder.copyFile(remoteUri, localUri);
			hotfolder.deleteIfExists(remoteUri);
		}
	}

	public void createAndSendTicket(AbbyyTicket abbyyTicket, String name) throws IOException, URISyntaxException {
		String ticketFileName = name + ".xml";
		URI inputTicketUri = new URI(abbyyTicket.getRemoteInputFolder().toString() + ticketFileName);
		
		synchronized (monitor) {
			OutputStream os = hotfolder.createTmpFile(ticketFileName);
			abbyyTicket.write(os, name);
			os.close();
		}
		
		//TODO: remove
//		URI ticketLogPath = new File("/home/dennis/temp/tickets/" + ticketFileName).toURI();
//		hotfolder.copyTmpFile(ticketFileName, ticketLogPath);

		hotfolder.copyTmpFile(ticketFileName, inputTicketUri);
		hotfolder.deleteTmpFile(ticketFileName);
	}

	
	
}
