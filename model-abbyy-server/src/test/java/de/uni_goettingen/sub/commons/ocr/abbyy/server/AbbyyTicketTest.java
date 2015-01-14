package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.uni_goettingen.sub.commons.ocr.api.OcrFormat;
import de.uni_goettingen.sub.commons.ocr.api.OcrOutput;
import de.uni_goettingen.sub.commons.ocr.api.OcrPriority;
import de.uni_goettingen.sub.commons.ocr.api.OcrQuality;
import de.uni_goettingen.sub.commons.ocr.api.OcrTextType;

public class AbbyyTicketTest {

	private AbbyyTicket ticketSut;
	private AbbyyProcess processMock = mock(AbbyyProcess.class);
	
	@Before
	public void beforeEachTest() throws Exception {
		when(processMock.getRemoteImageNames()).thenReturn(Arrays.asList("01.tif", "02.tif"));
		when(processMock.getPriority()).thenReturn(OcrPriority.NORMAL);
		when(processMock.getTextType()).thenReturn(OcrTextType.NORMAL);
		
		when(processMock.getLanguages()).thenReturn(new HashSet<Locale>(Arrays.asList(Locale.GERMAN, Locale.ENGLISH)));
		
		when(processMock.getQuality()).thenReturn(OcrQuality.BEST);
		when(processMock.canBeStarted()).thenReturn(true);
				
		when(processMock.getAllOutputFormats()).thenReturn(Collections.singleton(OcrFormat.XML));
		when(processMock.getOutputUriForFormat(OcrFormat.XML)).thenReturn(new URI("file:/test/result.xml"));
		
		when(processMock.getWindowsPathForServer()).thenReturn("c://temp");
		
		ticketSut = new AbbyyTicket(processMock);
	}

	@Test
	public void test() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ticketSut.write(baos);
		
		System.out.println(baos.toString());
	}


}
