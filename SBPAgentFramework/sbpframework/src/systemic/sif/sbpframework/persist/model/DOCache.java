/*
 * DOCache.java
 * Created: 11/10/2011
 *
 * Copyright 2011 Systemic Pty Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package systemic.sif.sbpframework.persist.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Joerg Huber
 *
 */
public class DOCache
{
	private Long id;
	private String sifObjectName;
	private String objectKeyValue;
	private Boolean isEvent;
    private String eventType;
	private String objectXML;
	private Date receivedOn;
	private String agentId;
	private String zoneId;
	private String applicationId;
	private Integer remainingDependencies;
	private Date expiryDate;
	private String expiryStrategy;
	private Set<DOCObject> dependentObjects;
	
	public DOCache() {}
	

	public Long getId()
    {
    	return this.id;
    }

	public void setId(Long id)
    {
    	this.id = id;
    }

	public String getSifObjectName()
    {
    	return this.sifObjectName;
    }

	public void setSifObjectName(String sifObjectName)
    {
    	this.sifObjectName = sifObjectName;
    }

	public String getObjectKeyValue()
    {
    	return this.objectKeyValue;
    }

	public void setObjectKeyValue(String objectKeyValue)
    {
    	this.objectKeyValue = objectKeyValue;
    }

    public Boolean getIsEvent()
    {
    	return this.isEvent;
    }


	public void setIsEvent(Boolean isEvent)
    {
    	this.isEvent = isEvent;
    }


	public String getEventType()
    {
    	return this.eventType;
    }


	public void setEventType(String eventType)
    {
    	this.eventType = eventType;
    }

	public String getObjectXML()
    {
    	return this.objectXML;
    }

	public void setObjectXML(String objectXML)
    {
    	this.objectXML = objectXML;
    }

	public Date getReceivedOn()
    {
    	return this.receivedOn;
    }

	public void setReceivedOn(Date receivedOn)
    {
    	this.receivedOn = receivedOn;
    }

	public String getAgentId()
    {
    	return this.agentId;
    }

	public void setAgentId(String agentId)
    {
    	this.agentId = agentId;
    }

	public String getZoneId()
    {
    	return this.zoneId;
    }

	public String getApplicationId()
    {
    	return this.applicationId;
    }


	public void setApplicationId(String applicationId)
    {
    	this.applicationId = applicationId;
    }

	public void setZoneId(String zoneId)
    {
    	this.zoneId = zoneId;
    }

	public Integer getRemainingDependencies()
    {
    	return this.remainingDependencies;
    }

	public void setRemainingDependencies(Integer remainingDependencies)
    {
    	this.remainingDependencies = remainingDependencies;
    }

	public Date getExpiryDate()
    {
    	return this.expiryDate;
    }

	public void setExpiryDate(Date expiryDate)
    {
    	this.expiryDate = expiryDate;
    }

	public String getExpiryStrategy()
    {
    	return this.expiryStrategy;
    }

	public void setExpiryStrategy(String expiryStrategy)
    {
    	this.expiryStrategy = expiryStrategy;
    }

	public Set<DOCObject> getDependentObjects()
    {
    	return this.dependentObjects;
    }

	public void setDependentObjects(Set<DOCObject> dependentObjects)
    {
    	this.dependentObjects = dependentObjects;
    }
	
	public void setDependentObjectsAsList(List<DOCObject> dependentObjects)
    {
    	this.dependentObjects = new HashSet<DOCObject>(dependentObjects);
    }

	@Override
	public String toString()
	{
		return "id = " + id +
		"\nsifObjectName = " + sifObjectName +
		"\nobjectKeyValue = " + objectKeyValue +
		"\nisEvent = " + isEvent +
		"\neventType = " + eventType +
		"\nreceivedOn = " + receivedOn +
		"\nzoneId = " + zoneId +
		"\napplicationId = " + applicationId +
		"\nagentId = " + agentId +
		"\nremainingDependencies = " + remainingDependencies +
		"\nexpiryDate = " + expiryDate +
		"\nexpiryStrategy = " + expiryStrategy +
		"\nobjectXML = " + objectXML +
		"\ndependentObjects = " + dependentObjects ;
	}
	
}
