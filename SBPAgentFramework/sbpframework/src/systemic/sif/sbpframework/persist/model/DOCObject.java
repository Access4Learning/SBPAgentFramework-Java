/*
 * DOCObject.java
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
import java.util.List;
import java.util.Set;

/**
 * @author Joerg Huber
 *
 */
public class DOCObject
{
	private Long id;
	private String sifObjectName;
	private String agentId;
	private String applicationId;
	private String objectKeyValue;
	private Boolean requested;
	private Date requestDate;
	private String zoneId;
	private Set<DOCache> parents;

	/* Properties for housekeeping but not DB storage */
	private transient List<SIFObjectKey> keyForDependentObject;
	
	public DOCObject() {}

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

	public String getAgentId()
    {
    	return this.agentId;
    }

	public void setAgentId(String agentId)
    {
    	this.agentId = agentId;
    }

	public Boolean getRequested()
    {
    	return this.requested;
    }

	public void setRequested(Boolean requested)
    {
    	this.requested = requested;
    }

	public Date getRequestDate()
    {
    	return this.requestDate;
    }

	public void setRequestDate(Date requestDate)
    {
    	this.requestDate = requestDate;
    }

	public String getZoneId()
    {
    	return this.zoneId;
    }

	public void setZoneId(String zoneId)
    {
    	this.zoneId = zoneId;
    }

	public String getApplicationId()
    {
    	return this.applicationId;
    }


	public void setApplicationId(String applicationId)
    {
    	this.applicationId = applicationId;
    }

	public List<SIFObjectKey> getKeyForDependentObject()
    {
    	return this.keyForDependentObject;
    }

	public Set<DOCache> getParents()
    {
    	return this.parents;
    }
	
	public void setParents(Set<DOCache> parents)
    {
    	this.parents = parents;
    }
	
	public void setKeyForDependentObject(List<SIFObjectKey> keyForDependentObject)
    {
    	this.keyForDependentObject = keyForDependentObject;
    }

	@Override
	public String toString()
	{
		return "id = " + id +
		"\nsifObjectName = " + sifObjectName +
		"\nobjectKeyValue = " + objectKeyValue +
		"\nagentId = " + agentId +
		"\nrequested = " + requested +
		"\nrequestDate = " + requestDate +
		"\napplicationId = " + applicationId +
		"\nzoneId = " + zoneId +
		"\nkeyForDependentObject = " + keyForDependentObject;
	}

}
