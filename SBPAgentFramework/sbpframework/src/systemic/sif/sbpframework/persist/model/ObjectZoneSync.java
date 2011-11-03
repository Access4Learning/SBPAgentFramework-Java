/*
 * ObjectZoneSync.java
 * Created: 07/10/2011
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


/**
 * @author User
 *
 */
public class ObjectZoneSync
{
	private Long id;
	private String objectName;
	private String agentId;
	private String zoneId;
	private Date lastRequested;
	
	public ObjectZoneSync() {}

	public ObjectZoneSync(Long id) 
	{
		setId(id);
	}
	
	public ObjectZoneSync(String objectName, String zoneId, String agentId) 
	{
		setObjectName(objectName);
		setZoneId(zoneId);
		setAgentId(agentId);
	}

	public ObjectZoneSync(Long id, String objectName, String zoneId, Date lastRequested) 
	{
		setId(id);
		setObjectName(objectName);
		setZoneId(zoneId);
		setLastRequested(lastRequested);
	}
	
	public Long getId()
    {
    	return this.id;
    }
	public void setId(Long id)
    {
    	this.id = id;
    }
	
	public String getObjectName()
    {
    	return this.objectName;
    }
	
	public void setObjectName(String objectName)
    {
    	this.objectName = objectName;
    }
	
	public String getZoneId()
    {
    	return this.zoneId;
    }
	
	public void setZoneId(String zoneId)
    {
    	this.zoneId = zoneId;
    }
	
	public String getAgentId()
    {
    	return this.agentId;
    }

	public void setAgentId(String agentId)
    {
    	this.agentId = agentId;
    }

	public Date getLastRequested()
    {
    	return this.lastRequested;
    }
	
	public void setLastRequested(Date lastRequested)
    {
    	this.lastRequested = lastRequested;
    }
	
	@Override
	public String toString()
	{
		return "id = " + id +
		"\nobjectName = " + objectName +
		"\nagentId = " + agentId +
		"\nzoneId = " + zoneId +
		"\nlastRequested = " + lastRequested;
	}
}
