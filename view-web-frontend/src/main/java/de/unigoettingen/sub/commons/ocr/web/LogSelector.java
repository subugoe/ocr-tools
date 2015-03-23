package de.unigoettingen.sub.commons.ocr.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.MDC;

public class LogSelector {

	public void logToFile(String filePath) {
		MDC.put("logFile", filePath);
		Properties props = readDefaults();
		props.setProperty("log4j.rootLogger", "INFO, file, CONSOLE");

		props.setProperty("log4j.logger.httpclient.wire", "ERROR");
		props.setProperty("log4j.logger.org.apache.commons.httpclient", "WARN");

		props.setProperty("log4j.appender.file", "org.apache.log4j.FileAppender");
		props.setProperty("log4j.appender.file.File", filePath);
		props.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.file.layout.ConversionPattern", "%-5p %d %l%n        %m%n");

		props.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "%-5p %m %d %l  (%X{logFile})%n");
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(props);
	}
	
	private Properties readDefaults() {
		Properties props = new Properties();
		try {
			InputStream configStream = getClass().getResourceAsStream("/log4j.properties");
			props.load(configStream);
			configStream.close();
		} catch (IOException e) {
			System.out.println("Error: Cannot load configuration file for logging. " + e.getMessage());
		}
		return props;
	}

	public void useDefaults() {
		MDC.remove("logFile");
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(readDefaults());
	}
}
