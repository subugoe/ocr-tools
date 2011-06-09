package de.unigoettingen.sub.commons.ocrComponents.hazelcast;

public interface OCRProcess {
	
	public Integer getPrio() ;
	
	public void setPrio(Integer prio) ;
	
	public Long getTime() ;
	
	public void setTime(Long time) ;

	public String getValue() ;

	public void setValue(String value) ;

}
