package de.unigoettingen.sub.commons.ocrComponents.webservice;

import java.io.Serializable;

public class Item implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String value;
	private Integer prio;
	private Long time;
	public Item(Integer prio, Long time, String value){
		this.prio = prio;
		this.time = time;
		this.value = value;
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
