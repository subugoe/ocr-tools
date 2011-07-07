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

    protected boolean success;
    protected int returncode;
    @XmlElement(required = true)
    protected String message;
    protected Long toolProcessingTime;
    @XmlElement(required = true)
    protected String processingLog;
    @XmlElement(required = true)
    protected String processingUnit;
    @XmlSchemaType(name = "anyURI")
    protected String outputUrl;

   
    
    public ByUrlResponseType(){
    	
    }
    /**
     * Gets the value of the success property.
     * 
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the value of the success property.
     * 
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * Gets the value of the returncode property.
     * 
     */
    public int getReturncode() {
        return returncode;
    }

    /**
     * Sets the value of the returncode property.
     * 
     */
    public void setReturncode(int value) {
        this.returncode = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the toolProcessingTime property.
     * 
     */
    public Long getToolProcessingTime() {
        return toolProcessingTime;
    }

    /**
     * Sets the value of the toolProcessingTime property.
     * 
     */
    public void setToolProcessingTime(Long value) {
        this.toolProcessingTime = value;
    }

    /**
     * Gets the value of the processingLog property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessingLog() {
        return processingLog;
    }

    /**
     * Sets the value of the processingLog property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessingLog(String value) {
        this.processingLog = value;
    }

    /**
     * Gets the value of the processingUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessingUnit() {
        return processingUnit;
    }

    /**
     * Sets the value of the processingUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessingUnit(String value) {
        this.processingUnit = value;
    }

    /**
     * Gets the value of the outputUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputUrl() {
        return outputUrl;
    }

    /**
     * Sets the value of the outputUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputUrl(String value) {
        this.outputUrl = value;
    }

}
