package de.unigoettingen.sub.commons.ocrComponents.hazelcast;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class AbbyyOCRProcess implements Serializable, Runnable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String value;
	private Integer prio;
	private Long time;
	
	public final static Logger logger = LoggerFactory
	.getLogger(AbbyyOCRProcess.class);

	public AbbyyOCRProcess(Integer prio, Long time, String value){
		this.prio = prio;
		this.time = time;
		this.value = value;
	}
	
	@Override
	public void run() {
		System.out.println("### OCR Engine Start run ###");
		System.out.println("#### finisched #### " + getPrio() + " "
				+ getTime() + " " + getValue());
	}
		
	
	public Integer getPrio() {
		return prio;
	}
	public void setPrio(Integer prio) {
		this.prio = prio;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}

}
