/*
 * SIFObjectMetadataCache.java
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

package systemic.sif.sbpframework.common.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;

import systemic.sif.sbpframework.persist.model.DependentKeyInfo;
import systemic.sif.sbpframework.persist.model.DependentObjectInfo;
import systemic.sif.sbpframework.persist.model.SIFObject;
import systemic.sif.sbpframework.persist.servcie.SIFObjectMetadataService;

/**
 * @author Joerg Huber
 *
 */
public class SIFObjectMetadataCache
{
	protected static final Logger logger = Logger.getLogger(SIFObjectMetadataCache.class);

	/* The definition of the cache structure. */
	private HashMap<String, SIFObject> sifObjectMetadataCache = null;
	
	private HashSet<String> objectsDependUpon = null;

	
	/* Singleton definition for SIFObjectMetadataCache object */
	private static SIFObjectMetadataCache instance = null;
	
	public static SIFObjectMetadataCache getCache()
	{
		try
		{
			if (instance == null)
			{
				instance = new SIFObjectMetadataCache();
			}
		}
		catch (Exception ex)
		{
			logger.error("Failed to load Metadata Cache for all SIF Objects.",ex);
			instance = null;
		}
		
		return instance;
	}
	
	/**
	 * This method returns the metadata information for the given SIF Object Name. If the cache doesn't hold any
	 * information for this object then null is returned.
	 * 
	 * @param sifObjectName The name of the SIF Object for which the metadata information shall be returned.
	 * 
	 * @return See description.
	 */
	public SIFObject getObjectMetadata(String sifObjectName)
	{
		SIFObject data = null;
		if (sifObjectMetadataCache != null)
		{
			data = sifObjectMetadataCache.get(sifObjectName);
		}
		
		return data;
	}
	
	/**
	 * This method simply checks if the SIF object given by its name (i.e StudentSchoolEnrolment) has potential
	 * dependencies based on the metadata that is available on the object. TRUE is returned if the object has
	 * dependencies defined in the metadata cache. False is returned if there are no known dependencies based
	 * on the metadata cache (i.e StudentPersonal has no dependencies where as the StudentSchoolEnrollment has the
	 * student and school as dependencies.). If the sifObjectName is  not known in the metadata cache then
	 * FALSE is returned, indicating no caching required.
	 * 
	 * @param sifObjectName The name of the SIF Object to test for.
	 * 
	 * @return See description.
	 */
	public boolean hasDependencies(String sifObjectName)
	{
		SIFObject obj = getObjectMetadata(sifObjectName);
		if (obj != null)
		{
			return ((obj.getDependentObjects() != null) && (obj.getDependentObjects().size() > 0));		
		}
		// Object not in cache => We don't know anything about dependencies.
		return false;
	}
	
	/**
	 * This method returns true if the given SIF Object given by its name is being depended upon, meaning there
	 * are other SIF objects that have a dependency on this object. For example the StudentPersonal is such an
	 * object because a number of other SIF objects depend upon the StudentPersonal such as the
	 * StudentSchoolEnrollment, StudentContactRelationship etc.
	 * If the object given by its name isn't being depended on by any other object then this method returns false.
	 * 
	 * @param sifObjectName The object name for which object the check shall be performed.
	 * 
	 * @return See description.
	 */
	public boolean isDependedUpon(String sifObjectName)
	{
		return objectsDependUpon.contains(sifObjectName);
	}
	
	@Override
	public String toString()
	{
		return "Metadata Info:\n"+sifObjectMetadataCache.toString()+
			   "\nDependend Upon Objects:\n"+objectsDependUpon;
	}
	
	/*---------------------*/
	/*-- Private Methods --*/
	/*---------------------*/
	private SIFObjectMetadataCache()
	{
		sifObjectMetadataCache =  new HashMap<String, SIFObject>();
		objectsDependUpon = new HashSet<String>();
		loadCache(sifObjectMetadataCache, objectsDependUpon);		
	}
	
	private void loadCache(HashMap<String, SIFObject> cache, HashSet<String> dependUponSet) throws IllegalArgumentException, PersistenceException
	{
		Timer timer = new Timer();
		timer.start();
		logger.debug("Load SIF Object Metadata Cache...");
		
		DOCacheProperties cacheProperties = DOCacheProperties.getDOCacheProperties();
		if (cacheProperties == null)
		{
			throw new IllegalArgumentException("Cannot load DOCacheProperties from file 'DOCache.properties'.");						
		}
		
		SIFObjectMetadataService service = new SIFObjectMetadataService();
		List<SIFObject> sifObjects = service.getAllSIFObjectMetadata();
		
		// Lookup cacheProperties for each object and override default values as required.
		for (SIFObject obj : sifObjects)
		{
			// First Override expiry strategy
			obj.setDefaultExpiryStrategy(cacheProperties.getExpiryStrategy(obj.getName(), obj.getDefaultExpiryStrategy()));
			
			// Now update the Expiry minutes
			obj.setDefaultExpiryInMinutes(cacheProperties.getExpiryMinutes(obj.getName(), obj.getDefaultExpiryInMinutes()));
			
			// Now check if some dependencies shall be ignored. If so remove them from dependency list of the object
			for (Iterator<DependentObjectInfo> iter = obj.getDependentObjects().iterator(); iter.hasNext();)
			{
				DependentObjectInfo depObj = iter.next();
				if (depObj.getParentObject() != null)
				{
					if (cacheProperties.getIgnoreDependency(obj.getName(), depObj.getParentObject().getName()))
					{
						iter.remove();
					}
				}
				else // Indicator Dependencies
				{
					DependentKeyInfo keyInfo = getIndicatorField(depObj);
					for (Iterator<SIFObject> validObjIter = keyInfo.getValidIndicatorList().iterator(); validObjIter.hasNext();)
					{
						SIFObject validObj = validObjIter.next();
						if (cacheProperties.getIgnoreDependency(obj.getName(), validObj.getName()))
						{
							validObjIter.remove();
						}
					}
					if (keyInfo.getValidIndicatorList().size() == 0) // none left
					{
						iter.remove();
					}
				}
			}
			cache.put(obj.getName(), obj);
		}
		
		// Scan through the cache again and check which objects are being depended upon and store them in the
		// hashset.
		for (SIFObject obj : cache.values())
		{
			for (DependentObjectInfo depObj : obj.getDependentObjects())
			{
				if (depObj.getParentObject() != null)
				{
					dependUponSet.add(depObj.getParentObject().getName());
				}
				else // Indicator Dependencies
				{
					DependentKeyInfo keyInfo = getIndicatorField(depObj);
					for (SIFObject validObj : keyInfo.getValidIndicatorList())
					{
						if (!cacheProperties.getIgnoreDependency(obj.getName(), validObj.getName()))
						{
							dependUponSet.add(validObj.getName());
						}
					}				
				}
			}
		}		
		
		timer.finish();
		logger.debug("SIF Object Metadata Cache loaded. Time taken: "+timer.timeTaken()+"ms");
	}

	
	public DependentKeyInfo getIndicatorField(DependentObjectInfo depObj)
	{
		for (DependentKeyInfo key : depObj.getKeyInfoList())
		{
			if (key.getIndicatorField())
			{
				return key;
			}
		}
		return null; // none found}
	}
}