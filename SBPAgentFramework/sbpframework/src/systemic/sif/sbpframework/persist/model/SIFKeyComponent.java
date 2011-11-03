/*
 * SIFKeyComponent.java
 * Created: 20/10/2011
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

import java.io.Serializable;

import systemic.sif.sifcommon.model.XPathValue;

/**
 * @author Joerg Huber
 *
 */
public class SIFKeyComponent extends XPathValue implements Serializable
{
    private static final long serialVersionUID = 103093454987L;

	private Long id = null;
	private Integer sortOrder = 1;
	
	public SIFKeyComponent() {}
	
	public SIFKeyComponent(Long id, String xpath, Integer sortOrder)
	{
		setId(id);
		setXpath(xpath);
		setSortOrder(sortOrder);
	}
	
	public SIFKeyComponent(Long id, String xpath, String value, Integer sortOrder)
	{
		setId(id);
		setXpath(xpath);
		setValue(value);
		setSortOrder(sortOrder);
	}


	public Long getId()
    {
    	return this.id;
    }

	public void setId(Long id)
    {
    	this.id = id;
    }

	public Integer getSortOrder()
    {
    	return this.sortOrder;
    }

	public void setSortOrder(Integer sortOrder)
    {
    	this.sortOrder = sortOrder;
    }

	@Override
	public String toString()
	{
		return "id = " + id +
			"\nsortOrder = " + sortOrder +
			"\n"+super.toString();
	}
	
}
