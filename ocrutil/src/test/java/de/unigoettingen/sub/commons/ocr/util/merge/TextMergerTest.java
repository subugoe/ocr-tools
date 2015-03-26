package de.unigoettingen.sub.commons.ocr.util.merge;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TextMergerTest {

	private final String input1 = "test1";
	private final String input2 = "test2";
	
	@Test
	public void shouldMergeTwo() throws Exception  {
		InputStream stream1 = new ByteArrayInputStream(input1.getBytes());
		InputStream stream2 = new ByteArrayInputStream(input2.getBytes());
		List<InputStream> inputs = Arrays.asList(stream1, stream2);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		Merger mergerSut = new TextMerger();
		mergerSut.mergeBuffered(inputs, output);	
		String result = output.toString();

		assertThat(result, containsString("test1"));
		assertThat(result, containsString("test2"));
	}	

}
