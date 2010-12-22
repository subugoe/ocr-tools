package de.uni_goettingen.sub.commons.ocr.abbyy.server;

import java.io.File;

import java.io.IOException;

import java.util.HashMap;
import java.util.Locale;

import java.util.Map;

import noNamespace.TextMDDocument;
import noNamespace.TextMDDocument.TextMD;
import noNamespace.TextMDDocument.TextMD.CharacterInfo;
import noNamespace.TextMDDocument.TextMD.CharacterInfo.Linebreak.Enum;
import noNamespace.TextMDDocument.TextMD.Encoding;
import noNamespace.TextMDDocument.TextMD.Language;



import org.apache.xmlbeans.XmlOptions;



import de.uni_goettingen.sub.commons.ocr.api.OCRProcessMetadata;


@SuppressWarnings("serial")
public class SerializerTextMD {
	
	protected Map<String, File> tmpfiles = new HashMap<String, File>();
	public final static String NAMESPACE = "info:lc/xmlns/textMD-v3";
	private static XmlOptions opts = new XmlOptions();
	protected OCRProcessMetadata ocrProcessMetadata;	

	
	public SerializerTextMD(OCRProcessMetadata ocrProcessMetadata){
		this.ocrProcessMetadata = ocrProcessMetadata;
	}
	
	
	
	public void write(File file) throws IOException{
		
		opts.setSavePrettyPrint();
		opts.setSaveImplicitNamespaces(new HashMap<String, String>() {
			{
				put("", NAMESPACE);
			}
		});
		opts.setUseDefaultNamespace();
		TextMDDocument textMDDoc = TextMDDocument.Factory.newInstance(opts);
		TextMD textMD = textMDDoc.addNewTextMD();
		
		
		
		Encoding encoding =textMD.addNewEncoding();
		encoding.addNewEncodingPlatform().setLinebreak(noNamespace.TextMDDocument.TextMD.Encoding.EncodingPlatform.Linebreak.Enum.forString(ocrProcessMetadata.getLinebreak()));
	
		CharacterInfo characterInfo = textMD.addNewCharacterInfo();
		if(ocrProcessMetadata.getLinebreak() != null){
			characterInfo.setLinebreak(Enum.forString(ocrProcessMetadata.getLinebreak()));
		}
		
		
		
		/*if(ocrProcessMetadata.getProcessingNote() != null){
			textMD.addNewProcessingNote();
			textMD.setProcessingNoteArray(0, ocrProcessMetadata.getProcessingNote());
		}*/
	
		if(ocrProcessMetadata.getTextNote() != null){
			textMD.addNewTextNote();
			textMD.setTextNoteArray(0, ocrProcessMetadata.getTextNote());
		}
		
	
		textMD.save(file,opts);
		
	}
	
	
	
}
