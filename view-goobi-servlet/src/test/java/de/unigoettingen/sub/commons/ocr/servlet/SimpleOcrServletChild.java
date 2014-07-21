package de.unigoettingen.sub.commons.ocr.servlet;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import de.unigoettingen.sub.commons.ocr.util.FileManager;
import de.unigoettingen.sub.ocr.controller.OcrEngineStarter;

public class SimpleOcrServletChild extends SimpleOcrServlet {

	private static final long serialVersionUID = -2478381173063981130L;
	
	@Override
	protected OcrEngineStarter getEngineStarter() {
		OcrEngineStarter starterMock = mock(OcrEngineStarter.class);
		return starterMock;
	}

	@Override
	protected FileManager getFileManager() {
		FileManager managerMock = mock(FileManager.class);
		try {
			when(managerMock.readFileToString(any(File.class))).thenReturn("test content");
		} catch (IOException e) {
		}
		return managerMock;
	}


}
