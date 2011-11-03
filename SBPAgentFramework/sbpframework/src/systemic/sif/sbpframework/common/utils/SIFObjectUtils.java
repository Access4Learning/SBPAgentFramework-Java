/*
 * SIFObjectUtils.java
 * Created: 19/10/2011
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

package systemic.sif.sbpframework.common.utils;

import java.util.List;

import org.apache.log4j.Logger;

import openadk.library.ComparisonOperators;
import openadk.library.ElementDef;
import openadk.library.Query;
import openadk.library.SIFDataObject;
import openadk.library.SIFParser;
import openadk.library.SIFVersion;
import systemic.sif.sifcommon.model.XPathValue;

/**
 * @author Joerg Huber
 *
 * This class implements some handy generic methods for some OpenADK methods. For these methods to work properly
 * it is assumed that the caller of them has called the ADK.initialise() beforehand.
 */
public class SIFObjectUtils
{
	protected final static Logger logger = Logger.getLogger(SIFObjectUtils.class);
	
	/**
	 * This method creates a SIF Query Object based on the sifObjectType, version and xpathValues. The version and
	 * xpathValues are allowed to be null.
	 * 
	 * @param sifObjectType The SIF object type for which to create the SIFQuery
	 * @param version The SIF Version for which to create the query. Allowed to be null.
	 * @param xpathValues List of xpath/value that make up the condition of the SIF Query.  Allowed to be null.
	 * 
	 * @return See Description
	 * 
	 * @throws IllegalArgumentException sifObjectType is null.
	 */
	public static Query makeQueryFromXPathValueList(ElementDef sifObjectType, SIFVersion version, List<? extends XPathValue> xpathValues) throws IllegalArgumentException
	{
		Query query = new Query(sifObjectType);
		if (version != null)
		{
			query.setSIFVersions(version); 
		}
		
		if (xpathValues != null)
		{
			for (XPathValue key : xpathValues)
			{
				query.addCondition(key.getXpath(), ComparisonOperators.EQ, key.getValue());
			}
		}
		return query;
	}
	
	public static SIFDataObject getSIFObjectFromXML(String sifObjectXML) 
	{
		try
		{
			SIFParser parser = SIFParser.newInstance();
			return (SIFDataObject)parser.parse(sifObjectXML);
		}
		catch (Exception ex)
		{
			logger.error("Failed to convert the given XML to a SIF Object:\n"+sifObjectXML, ex);
			return null;
		}
	}
}
