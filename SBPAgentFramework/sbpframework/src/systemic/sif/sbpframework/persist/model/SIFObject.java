/*
 * SIFObject.java
 * Created: 10/10/2011
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Joerg Huber
 *
 */
public class SIFObject
{
	/* Currently supported strategies for expired objects */
	public enum EXPRIY_STARTEGY {EXPIRE, REQUEST};
	
	private String name;
	private String keySeparator;
	private Integer defaultExpiryInMinutes;
	private String defaultExpiryStrategy;
	private Set<SIFObjectKey> objectKeys;
	private Set<DependentObjectInfo> dependentObjects;
	
	public SIFObject(){}
		
	public String getName()
    {
    	return this.name;
    }

	public void setName(String name)
    {
    	this.name = name;
    }

	public String getKeySeparator()
    {
    	return this.keySeparator;
    }

	public void setKeySeparator(String keySeparator)
    {
    	this.keySeparator = keySeparator;
    }

	public Integer getDefaultExpiryInMinutes()
    {
    	return this.defaultExpiryInMinutes;
    }

	public void setDefaultExpiryInMinutes(Integer defaultExpiryInMinutes)
    {
    	this.defaultExpiryInMinutes = defaultExpiryInMinutes;
    }

	public String getDefaultExpiryStrategy()
    {
    	return this.defaultExpiryStrategy;
    }

	public void setDefaultExpiryStrategy(String defaultExpiryStrategy)
    {
    	this.defaultExpiryStrategy = defaultExpiryStrategy;
    }

	public Set<SIFObjectKey> getObjectKeys()
    {
    	return this.objectKeys;
    }

	public void setObjectKeys(Set<SIFObjectKey> objectKeys)
    {
    	this.objectKeys = objectKeys;
    }
	
	/**
	 * Returns the keys of this object as a list ordered by sortOrder. This is a true copy of the key 
	 * stored internally.
	 */
	public List<SIFObjectKey> getOrderedKeyList()
	{
		ArrayList<SIFObjectKey> keyList = new ArrayList<SIFObjectKey>(getObjectKeys());
	
		// set correct order
		for (SIFObjectKey key : getObjectKeys())
		{
			keyList.set(key.getSortOrder() - 1, new SIFObjectKey(null, key.getXpath(), key.getSortOrder()));
		}
		return keyList;
	}

	public Set<DependentObjectInfo> getDependentObjects()
    {
    	return this.dependentObjects;
    }

	public void setDependentObjects(Set<DependentObjectInfo> dependentObjects)
    {
    	this.dependentObjects = dependentObjects;
    }

	@Override
	public String toString()
	{
		return "name = " + name +
		"\nkeySeparator = " + keySeparator +
		"\ndefaultExpiryInMinutes = " + defaultExpiryInMinutes +
		"\ndefaultExpiryStrategy = " + defaultExpiryStrategy +
		"\nobjectKeys = " + objectKeys +
		"\ndependentObjects = " + dependentObjects;
	}
}
