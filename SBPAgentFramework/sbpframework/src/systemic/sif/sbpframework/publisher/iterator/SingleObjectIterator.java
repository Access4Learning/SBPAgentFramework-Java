/*
 * SingleObjectIterator.java
 * Created: 11/11/2011
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

package systemic.sif.sbpframework.publisher.iterator;

import openadk.library.SIFDataObject;
import openadk.library.tools.mapping.ADKMappingException;
import systemic.sif.sifcommon.BaseInfo;
import systemic.sif.sifcommon.mapping.MappingInfo;
import systemic.sif.sifcommon.publisher.SIFResponseIterator;

/**
 * @author Joerg Huber
 *
 */
public class SingleObjectIterator implements SIFResponseIterator
{
	private boolean hasMore = Boolean.TRUE;
	private SIFDataObject sifObj = null;
	
	public SingleObjectIterator(SIFDataObject sifObj)
	{
		this.sifObj = sifObj;
		hasMore = (sifObj != null);
	}
	
	@Override
    public SIFDataObject getNextSIFObject(BaseInfo baseinfo, MappingInfo mappinginfo) throws ADKMappingException
    {
	    if (hasNext())
	    {
	    	hasMore = Boolean.FALSE;
	    	return sifObj;
	    }
	    return null;
    }

	@Override
    public boolean hasNext()
    {
	    return hasMore;
    }

	@Override
    public void releaseResources() {}

}
