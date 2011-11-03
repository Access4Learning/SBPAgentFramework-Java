/*
* Copyright 2010-2011 Systemic Pty Ltd
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

import java.util.Properties;

import systemic.sif.sifcommon.agent.SIFBaseAgent;
import au.com.systemic.framework.utils.StringUtils;

/**
 * This class provides a number of handy methods to access the values of the DOCache.properties file. 
 * Name of the property file must be DOCache.properties. This is the file that holds all the configurable values
 * for the Metadata cache that can overwrite behaviour as well as delays and execution frequencies of various
 * task relating to the Dependent Object Cache. The property file must be on the classpath.
 * 
 * @author Joerg Huber
 *
 */
public class DOCacheProperties
{
	private static final String FILE_NAME = "DOCache.properties";
	
	/* Known Expiry Strategies */
	public static final String EXPIRE = "EXPIRE";
	public static final String REQUEST = "REQUEST";
	
	private static final String DEFAULT = "default";
	
	private Properties properties = null;
	
	/* Singleton definition for SIFObjectMetadataCache object */
	private static DOCacheProperties instance = null ;

	public static DOCacheProperties getDOCacheProperties()
	{
		try
		{
			if (instance == null)
			{
				instance = new DOCacheProperties();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			instance = null;
		}
		
		return instance;
	}
	
	public String getExpiryStrategy(String sifObjectName, String defautltStrategy)
	{
		defautltStrategy = StringUtils.isEmpty(defautltStrategy) ? EXPIRE : defautltStrategy.trim();
		return getValueForSIFObject("cache.expiry.strategy", sifObjectName, defautltStrategy);
	}
	
	public int getExpiryMinutes(String sifObjectName, Integer defaultMinutes)
	{
		int defaultIntValue = getNoneNullOrZeroInteger(defaultMinutes, 120);
		return getValueForSIFObject("cache.expiry.minutes", sifObjectName, defaultIntValue);
	}
	
	public int getExpiryCheckFreqMinutes(Integer defaultMinutes)
	{
		int defaultIntValue = getNoneNullOrZeroInteger(defaultMinutes, 60);
		return getValue("cache.expiry.check.frequency.minutes", defaultIntValue);
	}
	
	public int getExpiryCheckStartupDelayInSec(Integer defaultSeconds)
	{
		int defaultIntValue = getNoneNullOrZeroInteger(defaultSeconds, 60);
		return getValue("cache.expiry.startup.delay.seconds", defaultIntValue);
	}

	public int getRequestFreqInSec(String sifObjectName, Integer defaultSeconds)
	{
		int defaultIntValue = getNoneNullOrZeroInteger(defaultSeconds, 60);
		return getValueForSIFObject("cache.request.frequency.seconds", sifObjectName, defaultIntValue);
	}

	public int getRequestStartupDelayInSec(String sifObjectName, Integer defaultSeconds)
	{
		int defaultIntValue = getNoneNullOrZeroInteger(defaultSeconds, 60);
		return getValueForSIFObject("cache.request.startup.delay.seconds", sifObjectName, defaultIntValue);
	}

	public int getResolvedFreqInSec(String sifObjectName, Integer defaultSeconds)
	{
		int defaultIntValue = getNoneNullOrZeroInteger(defaultSeconds, 60);
		return getValueForSIFObject("cache.resolved.frequency.seconds", sifObjectName, defaultIntValue);
	}

	public int getResolvedStartupDelayInSec(String sifObjectName, Integer defaultSeconds)
	{
		int defaultIntValue = getNoneNullOrZeroInteger(defaultSeconds, 60);
		return getValueForSIFObject("cache.resolved.startup.delay.seconds", sifObjectName, defaultIntValue);
	}
	
	public boolean getIgnoreDependency(String parentSifObjectName, String childSifObjectName)
	{
		if (StringUtils.isEmpty(parentSifObjectName) || StringUtils.isEmpty(childSifObjectName))
		{
			return false;
		}
		String value = getValue("cache.ignore."+parentSifObjectName+"."+childSifObjectName, "false");
		if (StringUtils.isEmpty(value))
		{
			return false;
		}
		return StringUtils.toBoolean(value);
	}

	/*---------------------*/
	/*-- Private Methods --*/
	/*---------------------*/

	/**
	 * This method loads the DOCache.properties file.
	 */
	private DOCacheProperties() throws Exception
	{
		properties = null;
        try
        {
        	properties = new Properties();
        	properties.load(SIFBaseAgent.class.getClassLoader().getResourceAsStream(FILE_NAME));
        }
        catch (Exception ex)
        {
            System.out.println("Error accessing/loading " + FILE_NAME+". Ensure wile exists and is on classpath.");
            throw ex;
        } 
	}

	private Integer getValue(String propertyName, Integer defaultValue)
	{
		Integer intValue = null;
		String value = getValue(propertyName, (defaultValue == null) ? (String)null : defaultValue.toString());
		if (value != null)
		{
			try
			{
				intValue = Integer.valueOf(value);
			}
			catch (Exception ex)
			{
				intValue = null;
			}
		}
		return intValue;
	}
	
	/*
	 * Gets the value for the given property and sifObjectName. If the property doesn't exists it will attempt to
	 * find if a 'default' property is set and returns that value. If that doesn't exist either the passed in
	 * default value will be returned.  
	 */
	private Integer getValueForSIFObject(String basePropertyName, String sifObjectName, Integer defaultValue)
	{
		Integer intValue = null;
		String value = getValueForSIFObject(basePropertyName, sifObjectName, (defaultValue == null) ? (String)null : defaultValue.toString());
		if (value != null)
		{
			try
			{
				intValue = Integer.valueOf(value);
			}
			catch (Exception ex)
			{
				intValue = null;
			}
		}
		return intValue;
	}

	private String getValue(String propertyName, String defaultValue)
	{
		String value = properties.getProperty(propertyName);
		
		return (StringUtils.isEmpty(value)) ? defaultValue : value.trim();
	}
	
	/*
	 * Gets the value for the given property and sifObjectName. If the property doesn't exists it will attempt to
	 * find if a 'default' property is set and returns that value. If that doesn't exist either the passed in
	 * default value will be returned.  
	 */
	private String getValueForSIFObject(String basePropertyName, String sifObjectName, String defaultValue)
	{
		String value = null;
		if (StringUtils.notEmpty(sifObjectName))
		{
			value = getValue(basePropertyName+"."+sifObjectName.trim(), (String)null);
		}
		if (StringUtils.isEmpty(value))
		{
			value = getValue(basePropertyName+"."+DEFAULT, defaultValue);
		}
		return value;	
	}
	
	public int getNoneNullOrZeroInteger(Integer intValue, int defaultValue)
	{
		if (intValue == null)
		{
			return defaultValue;
		}
		
		int value = intValue.intValue();
		return value == 0 ? defaultValue : value;
	}
}
