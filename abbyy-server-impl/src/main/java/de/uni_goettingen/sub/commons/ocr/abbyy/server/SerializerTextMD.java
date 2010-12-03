package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import noNamespace.TextMDDocument;
import noNamespace.TextMDDocument.TextMD;
import noNamespace.TextMDDocument.TextMD.CharacterInfo;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.Linebreak.Enum;



import org.apache.xmlbeans.XmlOptions;



import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;

public class SerializerTextMD {
	
	protected Map<String, File> tmpfiles = new HashMap<String, File>();
	public final static String NAMESPACE = "info:lc/xmlns/textMD-v3 http://www.loc.gov/standards/textMD/textMD-v3.01a.xsd";
	private static XmlOptions opts = new XmlOptions();
	protected OCRProcessMetadata ocrProcessMetadata;	

	
	public SerializerTextMD(OCRProcessMetadata ocrProcessMetadata){
		this.ocrProcessMetadata = ocrProcessMetadata;
	}
	
	
	
	public void write(){
		
		TextMDDocument textMDDoc = TextMDDocument.Factory.newInstance(opts);
		TextMD textMD = textMDDoc.addNewTextMD();
			
		CharacterInfo characterInfo = textMD.addNewCharacterInfo();
		if(ocrProcessMetadata.getLinebreak() != null){
			characterInfo.setLinebreak(Enum.forString(ocrProcessMetadata.getLinebreak()));
		}
			
		if(ocrProcessMetadata.getProcessingNote() != null){
			textMD.addNewProcessingNote();
			textMD.setProcessingNoteArray(0, ocrProcessMetadata.getProcessingNote());
		}
	
		if(ocrProcessMetadata.getTextNote() != null){
			textMD.addNewTextNote();
			textMD.setTextNoteArray(0, ocrProcessMetadata.getProcessingNote());
		}
		
		//TODO soll weiter gemacht werden
		
		
	}
	
	
	public OutputStream createTmpFile (String name) throws IOException {
		File tmpFile = File.createTempFile(name, null);
		tmpfiles.put(name, tmpFile);
		return new FileOutputStream(tmpFile);
	}
	
}
