package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import de.uni_goettingen.sub.commons.ocr.abbyy.server.hotfolder.Hotfolder;
import de.uni_goettingen.sub.commons.ocr.api.OCRFormat;
import de.uni_goettingen.sub.commons.ocr.api.OCRImage;
import de.uni_goettingen.sub.commons.ocr.api.OCROutput;

public class HotfolderManager {

	private Hotfolder hotfolder;

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

	
	
}
