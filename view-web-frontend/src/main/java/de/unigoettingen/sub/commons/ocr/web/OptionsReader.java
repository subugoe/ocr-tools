package de.unigoettingen.sub.commons.ocr.web;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OptionsReader {

	public static String getLanguages() throws FileNotFoundException, IOException {
		InputStream propsStream = OptionsReader.class.getClassLoader().getResourceAsStream("languages.properties");
		
		BufferedReader r = new BufferedReader(new InputStreamReader(propsStream));
		String langs = "";
		String[] keyValue = r.readLine().split("\\s*=\\s*");
		langs += "<option selected=\"selected\" value=\"" + keyValue[0] + "\">" + keyValue[1] + "</option>\n";
	
		String line = "";
		while ((line = r.readLine()) != null) {
			keyValue = line.split("\\s*=\\s*");
			if (keyValue.length == 2) {
				langs += "<option value=\"" + keyValue[0] + "\">" + keyValue[1] + "</option>\n";
			}
			
		}
			
		return langs;
	}
}
