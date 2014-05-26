package de.unigoettingen.sub.commons.ocr.engine.abbyy;

/*

© 2009, 2010, SUB Goettingen. All rights reserved.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

import java.net.URL;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import de.uni_goettingen.sub.commons.ocr.api.AbstractOCREngine;
import de.uni_goettingen.sub.commons.ocr.api.OCREngine;
import de.uni_goettingen.sub.commons.ocr.api.exceptions.OCRException;

public abstract class AbstractAbbyyOCREngine extends AbstractOCREngine implements OCREngine {
	protected AbstractConfiguration config;

	protected static PropertiesConfiguration loadConfig (URL configUrl) {
		PropertiesConfiguration pc = new PropertiesConfiguration();
		try {
			pc.load(configUrl);
		} catch (ConfigurationException e) {
			throw new OCRException(e);
		}
		return pc;
	}

	protected static Configuration loadConfig (String configName) {
		URL configUrl = AbstractAbbyyOCREngine.class.getResource("/" + configName);
		return loadConfig(configUrl);
	}

	//TODO:Add configurable mappings, this can be used for other Finereader based implementations as well
	/*
	protected Map<OCRExportFormat, String> loadFormatMappings (String settingName) throws OCREngineConfigurationException {
		String property = this.getClass().getSimpleName() + ".formatMapping";
		PropertiesConfiguration pc;
		try {
			pc = loadConfiguration();
		} catch (OCREngineConfigurationException e) {
			return null;
		}
		List<String> mappings = (List<String>) pc.getList(property);
		if (cmd == null) {
			return null;
		}
		return mappings;
	}

	protected Map<OCRLanguage, String> loadLanguageMappings (String settingName) throws OCREngineConfigurationException {
		String property = this.getClass().getSimpleName() + ".languageMapping";
		PropertiesConfiguration pc;
		try {
			pc = loadConfiguration();
		} catch (OCREngineConfigurationException e) {
			return null;
		}
		List<String> settings = (List<String>) pc.getList(property);
		if (cmd == null) {
			return null;
		}
		return settings;
	}

	protected Map<OCRExportFormat, List<String>> loadFormatSettings (String settingName) throws OCREngineConfigurationException {
		String property = this.getClass().getSimpleName() + ".formatSetting";
		PropertiesConfiguration pc;
		try {
			pc = loadConfiguration();
		} catch (OCREngineConfigurationException e) {
			return null;
		}
		List<String> mappings = (List<String>) pc.getList(property);
		if (cmd == null) {
			return null;
		}
		return mappings;
	}

	*/

}
