package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.util.Properties;

public class PropertiesCombiner {

	public static Properties combinePropsPreferringFirst(Properties userProps, Properties fileProps) {
		Properties combinedProps = new Properties(fileProps);
		for (String userKey : userProps.stringPropertyNames()) {
			combinedProps.setProperty(userKey, userProps.getProperty(userKey));
		}
		return combinedProps;
	}
	
}
