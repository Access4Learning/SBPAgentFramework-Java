/*
 * DependentKeyInfo.java
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

import java.util.Set;

/**
 * @author Joerg Huber
 *
 */
public class DependentKeyInfo extends SIFKeyComponent
{
    private static final long serialVersionUID = 834593454998L;
    private Boolean indicatorField;
	private Set<SIFObject> validIndicatorList;

	public DependentKeyInfo() {}
	
	public DependentKeyInfo(Long id, String xpathToKey, Integer sortOrder, Boolean indicatorField)
	{
		super(id, xpathToKey, sortOrder);
		setIndicatorField(indicatorField);
	}
	
	public Boolean getIndicatorField()
    {
    	return this.indicatorField;
    }

	public void setIndicatorField(Boolean indicatorField)
    {
    	this.indicatorField = indicatorField;
    }

	public Set<SIFObject> getValidIndicatorList()
    {
    	return this.validIndicatorList;
    }

	public void setValidIndicatorList(Set<SIFObject> validIndicatorList)
    {
    	this.validIndicatorList = validIndicatorList;
    }

	@Override
	public String toString()
	{
		String indicatorObjs = null;
		if ((validIndicatorList != null) && (validIndicatorList.size() > 0))
		{
			for (SIFObject sifObj : validIndicatorList)
			{
				indicatorObjs = (indicatorObjs == null ? "": indicatorObjs + ", ") + sifObj.getName();
			}
		}
		return "indicatorField = " + indicatorField +
			   "\nvalidIndicatorList = "+indicatorObjs +
			   "\n"+super.toString();
	}
}
