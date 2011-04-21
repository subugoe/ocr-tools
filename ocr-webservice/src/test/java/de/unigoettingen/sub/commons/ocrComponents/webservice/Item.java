package de.unigoettingen.sub.commons.ocrComponents.webservice;

import java.io.Serializable;

public class Item implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public Item(Integer prio, Long time){
		this.prio = prio;
		this.time = time;
	}
	
	Integer prio;
	
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
	Long time;
}
