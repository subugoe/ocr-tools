package de.unigoettingen.sub.commons.ocrComponents.webservice;
/*

Copyright 2010 SUB Goettingen. All rights reserved.
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



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ByUrlResponseType", propOrder = {
    "success",
    "returncode",
    "message",
    "toolProcessingTime",
    "processingLog",
    "processingUnit",
    "outputUrl"
})
public class ByUrlResponseType {

    private boolean success;
    private int returncode;
    @XmlElement(required = true)
    private String message;
    private Long toolProcessingTime;
    @XmlElement(required = true)
    private String processingLog;
    @XmlElement(required = true)
    private String processingUnit;
    @XmlSchemaType(name = "anyURI")
    private String outputUrl;

   
    
    public ByUrlResponseType(){
    	
    }
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean value) {
        this.success = value;
    }

    public int getReturncode() {
        return returncode;
    }

    public void setReturncode(int value) {
        this.returncode = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        this.message = value;
    }

    public Long getToolProcessingTime() {
        return toolProcessingTime;
    }

    public void setToolProcessingTime(Long value) {
        this.toolProcessingTime = value;
    }

    public String getProcessingLog() {
        return processingLog;
    }

    public void setProcessingLog(String value) {
        this.processingLog = value;
    }

    public String getProcessingUnit() {
        return processingUnit;
    }

    public void setProcessingUnit(String value) {
        this.processingUnit = value;
    }

    public String getOutputUrl() {
        return outputUrl;
    }

    public void setOutputUrl(String value) {
        this.outputUrl = value;
    }

}
