package de.unigoettingen.sub.commons.ocrComponents.cli;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ConcurrentModificationException;

import org.junit.Test;


public class IntegrationTest {

	@Test(expected=ConcurrentModificationException.class)
	public void test() throws URISyntaxException, IOException {
		Main main = new Main();
		FakeHotfolder hotfolderMock = mock(FakeHotfolder.class);
		when(hotfolderMock.exists(any(URI.class))).thenReturn(true);
		FakeHotfolder.instance = hotfolderMock;
		// -f PDF -l de -t NORMAL -o /home/dennis/digi/cli_output /home/dennis/digi/cli_input
		main.executeOld(new String[] {"-f", "PDF", "-l", "de", "-t", "NORMAL", "-o", "/home/dennis/digi/cli_output", "/home/dennis/digi/cli_input"});
	}

}
