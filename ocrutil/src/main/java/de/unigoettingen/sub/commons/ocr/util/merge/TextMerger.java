package de.unigoettingen.sub.commons.ocr.util.merge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class TextMerger implements Merger {

	@Override
	public void merge(List<InputStream> inputs, OutputStream output) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(output);
			// Ascii page break dec 12, hex 0c
			char pb = (char) 12;
			// Use the platform dependent separator here
			String seperator = System.getProperty("line.separator");

			int f = 0;
			while (f < inputs.size()) {
				InputStreamReader isr = new InputStreamReader(inputs.get(f), "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					osw.write(line);
					osw.write(seperator);
				}
				osw.write(pb);

				br.close();
				isr.close();
				f++;
			}
			osw.close();
		} catch (IOException e) {
			throw new IllegalStateException("Error while merging files.", e);
		}	
	}

}
