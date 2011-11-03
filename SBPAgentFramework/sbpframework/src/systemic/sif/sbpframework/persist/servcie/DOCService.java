/*
 * DOCService.java
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

package systemic.sif.sbpframework.persist.servcie;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.PersistenceException;

import openadk.library.ADKSchemaException;
import openadk.library.Element;
import openadk.library.SIFDataObject;

import org.apache.log4j.Logger;

import systemic.sif.sbpframework.common.utils.SIFObjectMetadataCache;
import systemic.sif.sbpframework.persist.dao.BaseDAO;
import systemic.sif.sbpframework.persist.dao.DOCacheDAO;
import systemic.sif.sbpframework.persist.model.DOCObject;
import systemic.sif.sbpframework.persist.model.DOCache;
import systemic.sif.sbpframework.persist.model.DependentKeyInfo;
import systemic.sif.sbpframework.persist.model.DependentObjectInfo;
import systemic.sif.sbpframework.persist.model.SIFObject;
import systemic.sif.sbpframework.persist.model.SIFObjectKey;
import au.com.systemic.framework.utils.DateUtils;
import au.com.systemic.framework.utils.StringUtils;

/**
 * @author Joerg Huber
 *
 */
public class DOCService extends DBService
{
	protected final Logger logger = Logger.getLogger(getClass());

    private DOCacheDAO docCacheDAO = new DOCacheDAO();
	private SIFObjectMetadataCache metadataCache = SIFObjectMetadataCache.getCache();

	@Override
    public BaseDAO getDAO()
    {
    	return docCacheDAO;
    }
	
	/**
	 * This method extracts the dependency info for the given sifObject and then looks up the DOC and marks already
	 * requested dependent objects for the given zone and application. The final list of DOCObject is then returned. If
	 * there are no dependent objects then an empty list is returned.
	 *  
	 * @param sifObject The SIF Object from which all dependencies need to be returned.
	 * @param applicationId Check if dependent objects have already been requested for this application.
	 * @param zoneId Check if dependent objects have already been requested for this zone.
	 * 
	 * @return See description.
	 * 
     * @throws PersistenceException If there is an error in the underlying DB. The error is logged.
     * @throws IllegalArgumentException If any of the parameters is null or empty.
     * @throws Exception some underlying issue. Error is logged.
	 */
	public List<DOCObject> getDependenciesFromSIFObjectAndCache(SIFDataObject sifObject, String applicationId, String zoneId) throws PersistenceException, IllegalArgumentException 
	{
		if ((sifObject == null) || StringUtils.isEmpty(zoneId) || StringUtils.isEmpty(applicationId))
		{
    		throw new IllegalArgumentException("Any of the parameters sifObject, applicationId or zoneId is null or empty.");			
		}
		checkMetadataCache();
		
		List<DOCObject> dependencyList = extractDependentObjectsFromSIFObject(sifObject);
		mergeWithCachedDependencies(dependencyList, applicationId, zoneId);
		
		return dependencyList;
	}
	
    /**
	 * This method attempts to retrieve a cached SIF Object based on the object name, object key (flatten key) and the
	 * agentId. Since each object can only be provided from one zone the zoneID is irrelevant and is not required to
	 * determine if an object is already cached. If the object isn't cached then null is returned.
	 * 
	 * NOTE:
	 * ================================================================================================================
	 * Only the top level object is returned. The dependent object list is not returned as part of this method!!
	 * If the entire object with its dependent objects shall be returned the loadAll parameter must be set to TRUE
	 * ================================================================================================================
	 * 
     * @param sifObject The SIF Object for which the DOCache info shall be returned.
     * @param applicationId The application ID for which the DOCache info shall be returned.
     * @param zoneId The Zone ID for which the DOCache info shall be returned.
     * @param loadAll Indicates if all dependent objects shall be loaded into memory. This is required since information
     *                is 'lazy' loaded by hibernate.
     * 
     * @return The DOCahce object if it exists in the cache already, null if it does not exist.
     * 
     * @throws PersistenceException If there is an error in the underlying DB. The error is logged.
     * @throws IllegalArgumentException If any of the parameters is null or empty.
     * @throws Exception some underlying issue. Error is logged.
	 */
    public DOCache retrieveCachedObject(SIFDataObject sifObject, String applicationId, String zoneId, boolean loadAll) throws PersistenceException, IllegalArgumentException
    {
    	checkMetadataCache();
    	try
    	{
    		if (sifObject != null)
    		{
    			SIFObject cachedObj = metadataCache.getObjectMetadata(sifObject.getElementDef().name());
    			String flatKey = extractFlatKey(sifObject, cachedObj.getOrderedKeyList(), cachedObj.getKeySeparator());

    			if (flatKey != null)
    			{
    				return retrieveCachedObject(sifObject.getElementDef().name(), flatKey, applicationId, zoneId, loadAll);
    			}
    			else
    			{
    	    		logger.error("Failed to retrieve cached object from DOCache. See prvious log entry for details. Object in question:\n"+sifObject.toXML());
    	    		return null;
    			}
    		}
    		else
    		{
    			throw new IllegalArgumentException("sifObject is null.");
    		}
    	}
    	catch (Exception ex)
    	{
    		logger.error("Failed to retrieve cached object from DOCache for object:\n"+sifObject.toXML());
    		exceptionMapper(ex, null, false, false);
    	}
    	return null; // we will never get here
    }

    /*
     * As above except the flat key is already known for the SIF Object.
     */
    public DOCache retrieveCachedObject(String sifObjectName, String flatKey, String applicationId, String zoneId, boolean loadAll) throws PersistenceException, IllegalArgumentException
    {
    	DOCache cachedObject = null;
       	startTransaction();
    	try
    	{
    		cachedObject = docCacheDAO.retrieveCachedObject(sifObjectName, flatKey, applicationId, zoneId, loadAll);
    		commit();
    	}
    	catch (Exception ex)
    	{
    		rollback();
    		exceptionMapper(ex, "Failed to retrieve cached object from DOCache for Object = '"+sifObjectName+"', flat key = '"+flatKey+"', application ID = '"+applicationId+"' and zoneId = '"+zoneId+"'", true, false);
    	}
		return cachedObject;
    }

    
    /**
     * This method will save the DOCache object with all its child elements. It will assign appropriate properties such
     * as Expire Info (retrieved from the Metadata Cache), received date etc. If the given sifObjectToCache has no 
     * dependencies then it will NOT be persisted. A info message is logged in that case as this is not really an error.
     * 
     * @param sifObjectToCache The object to save to the DOC.
     * @param agentId The agent that requests this save.
     * @param applicationId The application for which the entry is stored. Will be used to pre-populate some information.
     * @param zoneId The zone from which the SIF Object has been received. This will be the zone from which dependent objects
     *               will be requested at some stage.
     * 
     * @throws PersistenceException If the object cannot be persisted. The error is logged.
     * @throws IllegalArgumentException If any of the parameters is null or empty. Metadata Cache is not available.
     */
    public void cacheObject(DOCache sifObjectToCache, String agentId, String applicationId, String zoneId) throws PersistenceException, IllegalArgumentException
    {
    	checkMetadataCache();
    	if ((sifObjectToCache == null) || StringUtils.isEmpty(agentId) || StringUtils.isEmpty(zoneId) || StringUtils.isEmpty(applicationId))
    	{
    		throw new IllegalArgumentException("Any of the parameters sifObjectToCache, agentId, applicationId or zoneId is null or empty.");
    	}
    	if ((sifObjectToCache.getDependentObjects() == null) || ((sifObjectToCache.getDependentObjects().size() == 0)))
    	{
    		logger.info("The object '"+sifObjectToCache.getSifObjectName() + "' has no open dependencies. Will not cache it.");
    	}
    	else
    	{
    		SIFObject cachedObj = metadataCache.getObjectMetadata(sifObjectToCache.getSifObjectName());
    		
	    	//Iterate through object and set the agentId and zoneId where needed, as well as some other default parameters
	    	sifObjectToCache.setAgentId(agentId);
	    	sifObjectToCache.setZoneId(zoneId);
	    	sifObjectToCache.setApplicationId(applicationId);
	    	sifObjectToCache.setReceivedOn(new Date());
	    	sifObjectToCache.setRemainingDependencies(sifObjectToCache.getDependentObjects().size());
	    	sifObjectToCache.setExpiryDate(DateUtils.dateAfter(sifObjectToCache.getReceivedOn(), cachedObj.getDefaultExpiryInMinutes()*60*1000));
	    	sifObjectToCache.setExpiryStrategy(cachedObj.getDefaultExpiryStrategy());
	    	
	    	// Iterate through all dependent objects and set the application and zoneID
	    	for (DOCObject docObject : sifObjectToCache.getDependentObjects())
	    	{
	    		// ensure that the docObject has net yet been requested and only then update the information
	    		if (!docObject.getRequested())
	    		{
	    			// Don't assign the agent because we don't know which agent is responsible for requesting it. Instead
	    			// assign the application ID because whichever agent is responsible for the retrieval of this dependent
	    			// object must pick it up for that application.
	    			docObject.setZoneId(zoneId);
	    			docObject.setRequested(false);
	    			docObject.setApplicationId(applicationId);
	    		}
	    	}
	    	
	    	// Now the object is ready to be saved.
	    	try
	    	{
	        	startTransaction();
	    		docCacheDAO.save(sifObjectToCache);
	    		commit();
	    	}
	    	catch (Exception ex)
	    	{
	    		rollback();
	    		exceptionMapper(ex, "Failed to save SIF Object to DOC Cache:\nObject To Cache:\n"+sifObjectToCache, true, false);
	    	}
    	}
    }
    
    /**
     * This method updates the given docObject and marks it as requested. (requested=true, requestDate=now). It is
     * assumed that the object does already exist (i.e must have the property id of not null). This method is intended
     * to be called once a dependent object has been requested, meaning an appropriate SIFQuery has been issued by an
     *  agent (agentId) to a zone (zoneId).
     *  
     * @param docObject The dependent object to update (must have an id of not null!!)
     * @param agentId The agent that has requested the object.
     * @param zoneId The zone to which the request has been sent.
     * 
     * @throws PersistenceException If the object cannot be persisted. The error is logged.
     * @throws IllegalArgumentException If any of the parameters is null or empty. The property docObject.id is null.
     */
    public void markDependentObjectAsRequested(DOCObject docObject, String agentId, String zoneId) throws PersistenceException, IllegalArgumentException
    {
    	if ((docObject == null) || (docObject.getId() == null) || StringUtils.isEmpty(agentId) || StringUtils.isEmpty(zoneId))
    	{
    		throw new IllegalArgumentException("Any of the parameters docObject, docObject.id, agentId or zoneId is null or empty.");
    	}
    	docObject.setAgentId(agentId);
    	docObject.setZoneId(zoneId);
    	docObject.setRequestDate(new Date());
    	docObject.setRequested(Boolean.TRUE);

    	// Now the object is ready to be saved.
    	try
    	{
        	startTransaction();
    		docCacheDAO.save(docObject);
    		commit();
    	}
    	catch (Exception ex)
    	{
    		rollback();
    		exceptionMapper(ex, "Failed to mark Dependent Object as reuested.:\n"+docObject, true, false);
    	}
    	
    }
    
 
    /**
     * This method iterates through the dependentObjectList and replaces elements in that list with the requested
     * information. This includes if the object has already been requested, when and by which agent. Objects that have not
     * been requested are updated with the application ID and Zone Id. If the dependentObjectList is null or empty then
     * no action is taken.
     * For this method to work the following property in each DOCObject of the dependentObjectList must be populated:
     *  - objectKeyValue
     *  - sifObjectName
     * 
     * @param dependentObjectList The list of objects to test
     * @param applicationId The application to test for.
     * @param zoneId The zone to test for.
     * 
     * @throws PersistenceException If there are any errors with the underlying data store. Error is logged.
     * @throws IllegalArgumentException applicationId or zoneId is empty or null.
     */
    public void mergeWithCachedDependencies(List<DOCObject> dependentObjectList, String applicationId, String zoneId) throws PersistenceException, IllegalArgumentException
    {
    	if ((dependentObjectList != null) && (dependentObjectList.size() > 0))
    	{
	    	try
	    	{
	        	startTransaction();
	        	for (int i=0; i<dependentObjectList.size(); i++)
	        	{
	        		DOCObject depObj = dependentObjectList.get(i);
	        		depObj.setApplicationId(applicationId);
	    			depObj.setZoneId(zoneId);
	    			DOCObject reqObj = docCacheDAO.getCachedDependentObject(depObj);
	    			if (reqObj != null)
	    			{
	    				dependentObjectList.set(i, reqObj);
	    			}
	        	}
	        	        	
	    		commit();
	    	}
	    	catch (Exception ex)
	    	{
	    		rollback();
	    		exceptionMapper(ex, "Failed to mark dependent objects as requested.", true, true);
	    	}
    	}
    }
    
    /**
     * This method checks if there are any objects cached that depend on this object. If it finds any objects it will
     * remove that dependency from the DOC. If there are no dependencies no action is taken.
     *  
     * @param sifObject The object for which it is checked if there are dependencies.
     * @param applicationId The application for which to check.
     * @param zoneId The zone this object has been received from.
     * 
     * @throws PersistenceException If there are any errors with the underlying data store. Error is logged.
     * @throws IllegalArgumentException sifObject, applicationId or zoneId is empty or null. Metadata Cache is not available.
     */
    public void checkAndRemoveDependency(SIFDataObject sifObject, String applicationId, String zoneId) throws IllegalArgumentException, PersistenceException
    {
    	checkMetadataCache();
    	if ((sifObject == null) || StringUtils.isEmpty(zoneId) || StringUtils.isEmpty(applicationId))
    	{
    		throw new IllegalArgumentException("Any of the parameters sifObject, applicationId or zoneId is null or empty.");
    	}
    	
		SIFObject cachedObj = metadataCache.getObjectMetadata(sifObject.getElementDef().name());
		String flatKey = extractFlatKey(sifObject, cachedObj.getOrderedKeyList(), cachedObj.getKeySeparator());
		checkAndRemoveDependency(sifObject.getElementDef().name(), flatKey, applicationId, zoneId);
    }

    /*
     * Same as above method but for a known key (flattened) of the sifObject.
     */
    public void checkAndRemoveDependency(String sifObjectName, String flatKey, String applicationId, String zoneId) throws IllegalArgumentException, PersistenceException
    {
		DOCObject docObject = new DOCObject();
		docObject.setSifObjectName(sifObjectName);
		docObject.setObjectKeyValue(flatKey);
		docObject.setApplicationId(applicationId);
		docObject.setZoneId(zoneId);
		
    	try
    	{
        	startTransaction();
        	docCacheDAO.removeDependency(docObject);
    		commit();
    	}
    	catch (Exception ex)
    	{
    		rollback();
    		exceptionMapper(ex, "Failed to remove dependencies for object "+sifObjectName + " and flat key = "+flatKey, true, true);
    	}		    	
    }
    
    /**
     * This method returns a list of DOC Objects. The objects are those that have not been requested, yet (requested=false).
     * The 'keyForDependentObject' of each DOCObject in the list is also populated with the key details of the object.
     * Based on this a SIF Query could easily be constructed as all the key information (xpath and value) is available
     * after a call to this method.
     * 
     * Note: 
     * If an agent subscribes to more than one zone for the same sifObject then this method must be called
     * separately for each zone by the agent. If there are no cached and not yet requested objects for the
     * given parameters then an empty list is returned.
     * 
     * @param sifObjectName The SIF Object names to search for (i.e StudentPersonal)
     * @param applicationId Only return objects marked for this application.
     * @param zoneId Only return objects marked for this zone.
     * 
     * @return See description
     * 
     * @throws IllegalArgumentException  docObject is null or any of the properties listed above is empty or null.
     *                                   Metadata Cache is not available.
     * @throws PersistenceException      A database error occurred.
     */
    public List<DOCObject> getNotYetRequestedObjects(String sifObjectName, String applicationId, String zoneId) throws IllegalArgumentException, PersistenceException
    {
    	checkMetadataCache();
    	List<DOCObject> docList = null;
    	try
    	{
        	startTransaction();
        	docList = docCacheDAO. getNotYetRequestedObjects(sifObjectName, applicationId, zoneId);
    		commit();
    	}
    	catch (Exception ex) //any other exception...
    	{
    		rollback();
    		exceptionMapper(ex, "Unable to retrieve list of DOCObjects for application = '"+ applicationId + "', Sif Object = '" + sifObjectName+ "', zone = '" + zoneId +"'.", true, false);
    	}
    	
    	SIFObject objectMetadata = metadataCache.getObjectMetadata(sifObjectName);
    	for (DOCObject docObject : docList)
    	{
        	List<SIFObjectKey> objectKeys = objectMetadata.getOrderedKeyList();
        	String[] keyComponents = new String[objectKeys.size()];       	
        	if (StringUtils.isEmpty(objectMetadata.getKeySeparator()))
        	{
        		keyComponents[0] = docObject.getObjectKeyValue();
        	}
        	else
        	{
        		keyComponents = docObject.getObjectKeyValue().split(objectMetadata.getKeySeparator());
        	}
        	
        	// Assign key component values to appropriate key value in the ordered list.
        	for (int i=0; i<keyComponents.length; i++)
        	{
        		objectKeys.get(i).setValue(keyComponents[i]);
        	}
        	docObject.setKeyForDependentObject(objectKeys);
    	}
    	
    	return docList;
    }
    
    /**
     * This method gets all cached objects of a given type for a particular application and agent that have no 
     * remaining dependencies. These are the candidates do be removed later and be processed by the appropriate 
     * subscriber. If there are no Cached Objects without dependencies then an empty list is returned.
     *  
     * @param sifObjectName The type of cached objects to be returned.
     * @param applicationId The application for which the objects shall be returned.
     * @param agentId The agent for which to get the object list. 
     * 
     * @return See description.
     * 
     * @throws IllegalArgumentException  Any of the parameters is null or empty.
     * @throws PersistenceException      A database error occurred. Error is logged.
     */
    public List<DOCache> getObjectsWithoutDependencies(String sifObjectName, String applicationId, String agentId) throws IllegalArgumentException, PersistenceException
    {
    	List<DOCache> objectList = null;
    	try
    	{
        	startTransaction();
        	objectList = docCacheDAO.getObjectsWithoutDependencies(sifObjectName, applicationId, agentId);
    		commit();
     	}
    	catch (Exception ex) 
    	{
    		rollback();
    		exceptionMapper(ex, "Unable to retrieve list of Cached Objects without dependencies for application = '"+ applicationId + "', Sif Object = '" + sifObjectName + "'and agent = '" + agentId + "'.", true, false);
    	}
   		return objectList;
    }
    
    /**
     * This method returns all objects in the cache that have remaining dependencies but have an expiry date
     * older than the current date and time. These are the candidates that need to either be removed or 
     * re-requested by the agent. If there are no expired objects then an empty list is returned.
     * 
     * @param applicationId Only return expired objects for this application.
     * @param agentId Only return expired object that were initially cached by this agent.
     * 
     * @return See description.
     * 
     * @throws IllegalArgumentException  Any of the parameters is null or empty.
     * @throws PersistenceException      A database error occurred. Error is logged.
     */
    public List<DOCache> getExpiredObjects(String applicationId, String agentId) throws IllegalArgumentException, PersistenceException
    {
    	List<DOCache> objectList = null;
    	try
    	{
        	startTransaction();
        	objectList = docCacheDAO.getExpiredObjects(applicationId, agentId);
    		commit();
     	}
    	catch (Exception ex) 
    	{
    		rollback();
    		exceptionMapper(ex, "Unable to retrieve list of expired Cached Objects for application = '"+ applicationId + "'and agent = '" + agentId + "'.", true, false);
    	}
   		return objectList;
    }
    
    /**
     * This method removes the given object and all its dependencies from the cache. If the object is null then
     * no action is taken.
     * 
     * @param cacheObject The object to remove from the DO Cache.
     * 
     * @throws PersistenceException      A database error occurred.
     */
    public void removeCachedObject(DOCache cacheObject)throws PersistenceException
    {
    	try
    	{
        	startTransaction();
        	docCacheDAO.removeCachedObject(cacheObject);
    		commit();
     	}
    	catch (Exception ex) 
    	{
    		rollback();
    		exceptionMapper(ex, "Unable to remove cacheObject " + cacheObject + ".", true, false);
    	}
    }
    
    /**
     * This method updates all Cached Objects and their dependencies according to the expiry strategy. Currently
     * there are 2 strategies supported: EXPIRE and REQUEST. In case of EXPIRE the cached object and its 
     * dependencies will be removed from the cache and a info message is logged. In case of REQUEST all dependent
     * objects for each cached object will be marked as not requested (requested=false), so that it can be 
     * requested again by the appropriate subscribers. The Cached Object will update its expiry date accordingly.
     * 
     * @param applicationId Only update/remove expired objects for this application.
     * @param agentId Only update/remove expired object that were initially cached by this agent.
     * 
     * @throws IllegalArgumentException  Any of the parameters is null or empty.
     * @throws PersistenceException      A database error occurred. Error is logged.
     */
    public void updateExpiredObjects(String applicationId, String agentId) throws IllegalArgumentException, PersistenceException
    {
    	checkMetadataCache();
		if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(agentId))
		{
			throw new IllegalArgumentException("Some of the following parameters are either null or empty: applicationId, agentId");
		}
    	try
    	{
        	startTransaction();
       		Date now = new Date();
       		List<DOCache> getExpiredObjects = docCacheDAO.getExpiredObjects(applicationId, agentId);
       		for (DOCache cachedObject : getExpiredObjects)
       		{
       			// Should one test against the strategy in the metadata cache or what is in the DB?
       			// If test against metadata cache then a simple restart of the agent would apply the new
       			// strategy, while the DB value is 'sticky' until the next update cycle. Also objects marked as
       			// 'EXPIRE' would expire while using the metadata cache would allow the expire value to override
       			// immediately after restart.
       			
       			// This test uses the DB value...
       			if (cachedObject.getExpiryStrategy().equals(SIFObject.EXPRIY_STARTEGY.EXPIRE.name()))
       			{
       				docCacheDAO.removeCachedObject(cachedObject);
       			}
       			else if (cachedObject.getExpiryStrategy().equals(SIFObject.EXPRIY_STARTEGY.REQUEST.name()))
       			{
       				// Update all dependent objects to requested = false
       				for (DOCObject depObj : cachedObject.getDependentObjects())
       				{
       					depObj.setRequested(Boolean.FALSE);
       				}
       				
       				// Reset the expiry date and strategy according to the object metadata cache values
       				SIFObject sifObjectMetadata = metadataCache.getObjectMetadata(cachedObject.getSifObjectName());
       				cachedObject.setExpiryDate(DateUtils.dateAfter(now, sifObjectMetadata.getDefaultExpiryInMinutes()*60*1000));
       				cachedObject.setExpiryStrategy(sifObjectMetadata.getDefaultExpiryStrategy());
       				
       				//save the updated values
       				docCacheDAO.save(cachedObject);
       			}
       			else // Invalid expiry strategy.
       			{
       				logger.error("Invalid Expiry Strategy ("+cachedObject.getExpiryStrategy()+") defined for Cached Object "+cachedObject.getSifObjectName()+" and Key = "+cachedObject.getObjectKeyValue()+".");
       			}
       		}       	
    		commit();
     	}
    	catch (Exception ex) 
    	{
    		rollback();
    		exceptionMapper(ex, "Unable to update the expiry startegy for all expired cached objects for application = "+applicationId+" and agent = "+agentId+". See previous error log entry for detail.", true, false);
    	}
    }
    
    /*-----------------------------------------------------------------------------*/
    /*-- Methods that operate on the SIFObject only but don't require a database --*/ 
    /*-----------------------------------------------------------------------------*/
    
	/**
	 * This method simply checks if the SIF object given by its name (i.e StudentSchoolEnrolment) has potential
	 * dependencies based on the metadata that is available on the object. TRUE is returned if the object has
	 * dependencies defined in the metadata cache. False is returned if there are no known dependencies based
	 * on the metadata cache (i.e StudentPersonal has no dependencies where as the StudentSchoolEnrollment has the
	 * student and school as dependencies.). If the sifObjectName is null or not known in the metadata cache then
	 * FALSE is returned, indicating no caching required.
	 * 
	 * @param sifObjectName The name of the SIF Object to test for.
	 * 
	 * @return See description.
	 */
	public boolean hasPotentialDependencies(String sifObjectName)
	{
		if (metadataCache == null)
		{
			return false;
		}
		if (StringUtils.isEmpty(sifObjectName))
		{
			return false;
		}
		return metadataCache.hasDependencies(sifObjectName);
	}

	/**
     * This method attempts to extract the primary key from the SIF Object as defined by the SIF Spec and flattens it.
     * If the key is made up of more than one component the various components are separated by a separator as defined
     * in the Metadata cache (generally a '|' character). The order of the key components is the same as the order of the
     * key definition in the SIF Spec.
     * 
     * @param sifObject From which the primary key shall be extracted and flattened.
     * 
     * @return The flattened key of the SIFObject as described above.
     * 
     * @throws IllegalArgumentException Object is null or doesn't contain the primary key as expected by the SIF Spec.
     */
    public String extractFlatKey(SIFDataObject sifObject) throws IllegalArgumentException
    {
		if (metadataCache != null) // we cannot determine info about object if cache is not enabled.
		{
			SIFObject cachedObj = metadataCache.getObjectMetadata(sifObject.getElementDef().name());
			return extractFlatKey(sifObject, cachedObj.getOrderedKeyList(), cachedObj.getKeySeparator());
		} 
		else
		{
			throw new IllegalArgumentException("MetadataCache not inistialised. Cannot determine key for sif object.");
		}
    }
    
    /**
     * This method checks SIF Object Cache if the given sifObject has dependencies and if so it will extract all dependencies
     * and returns them. Each dependency is taken from the sifObject content by means of looking up the SIF Object cache and
     * its object metadata information.
     * 
     * @throws PersistenceException If there are any errors with the underlying data store. Error is logged.
     * @throws IllegalArgumentException Metadata Cache is not available. Error is logged.
     * 
     */
    public List<DOCObject> extractDependentObjectsFromSIFObject(SIFDataObject sifObject) throws PersistenceException, IllegalArgumentException
    {
    	checkMetadataCache();
    	List<DOCObject> dependencies = null;
    	
    	try
    	{
    		if (sifObject != null)
    		{
    			if (metadataCache.hasDependencies(sifObject.getElementDef().name()))
    			{
    				SIFObject cachedObj = metadataCache.getObjectMetadata(sifObject.getElementDef().name());
    				dependencies = new ArrayList<DOCObject>();
    				for (DependentObjectInfo dependendInfo : cachedObj.getDependentObjects())
    				{
    					extractDependentObjects(sifObject, cachedObj, dependendInfo, dependencies);    					
    				}
    			}		
    		}
    	}
    	catch (Exception ex)
    	{
    		exceptionMapper(ex, "Failed to determine dependencies for object:\n"+sifObject.toXML(), true, false);
    	}
		return dependencies;
    }
    
    
    /*---------------------*/
    /*-- Private Methods --*/
    /*---------------------*/
	private void extractDependentObjects(SIFDataObject sifObject, SIFObject sourceObj,  DependentObjectInfo dependendObjInfo, List<DOCObject> dependencies) throws ADKSchemaException
	{
		if (dependendObjInfo.getListOfObjects())
		{
			// Deal with the case where there are many dependent objects of the same type (i.e. TeachingGroup->TeachingGroupStudent)
			int idx = 1;
			String xpathToListObj = dependendObjInfo.getXpathToList()+"["+idx+"]";
			while (sifObject.getElementOrAttribute(xpathToListObj) != null)
			{
				DOCObject depObj = extractDependentObjectInfo(sifObject, sourceObj, dependendObjInfo, xpathToListObj+"/");
				if (depObj != null)// Found valid dependent object
				{
					dependencies.add(depObj);
				}
				idx++;
				xpathToListObj = dependendObjInfo.getXpathToList()+"["+idx+"]";
			}
		}
		else
		{
			DOCObject depObj = extractDependentObjectInfo(sifObject, sourceObj, dependendObjInfo, "");
			if (depObj != null)// Found valid dependent object
			{
				dependencies.add(depObj);
			}
		}
	}

	private DOCObject extractDependentObjectInfo(SIFDataObject sifObject, SIFObject sourceObj,  DependentObjectInfo dependendObjInfo, String xpathToListObject) throws ADKSchemaException
	{
		DOCObject depObj = new DOCObject();
		depObj.setSifObjectName(dependendObjInfo.getParentObject().getName());
		
		//Extract Key Definition Info
		List<SIFObjectKey> keyForDependentObject = new ArrayList<SIFObjectKey>();
		for (SIFObjectKey key : dependendObjInfo.getParentObject().getOrderedKeyList())
		{
			keyForDependentObject.add(new SIFObjectKey(null, key.getXpath(), key.getSortOrder()));
		}

		// Extract key values from actual SIF Object
		if (extractDependentKeyValuesFromSIFObject(sifObject, xpathToListObject, dependendObjInfo, sourceObj, keyForDependentObject))
		{
			depObj.setKeyForDependentObject(keyForDependentObject);
			
			// Flatten key and store it in the appropriate property.
			depObj.setObjectKeyValue(flattenOrderedKey(keyForDependentObject, dependendObjInfo.getParentObject().getKeySeparator()));
			depObj.setRequested(Boolean.FALSE);
			
			return depObj;
		}
		else
		{
			logger.error("Dependency "+ depObj.getSifObjectName() + " for object "+sourceObj.getName()+" not added. See prvious error log entry for details.");				
			return null;
		}
	}
	
	private boolean extractDependentKeyValuesFromSIFObject(SIFDataObject sifObject, String xpathPrefix, DependentObjectInfo dependendInfo, SIFObject sourceObj, List<SIFObjectKey> orderedKeyForParentObj) throws ADKSchemaException
	{
		boolean allKeysHaveValue = true;
		List<DependentKeyInfo> dependentKeyInfoList = dependendInfo.getOrderedKeyInfoList();

		// Iterate through keyForDependentObject to see which key values we must retrieve and in what sort_order.
		if (dependentKeyInfoList.size() != orderedKeyForParentObj.size())
		{
			logger.error("There number of key components of object ("+sifObject.getElementDef().name()+") doesn't match the number of key components of the dependent object ("+dependendInfo.parentObject.getName()+").");
			allKeysHaveValue = false;
		}
		else
		{
			for (int i=0; i<orderedKeyForParentObj.size(); i++)
			{
				SIFObjectKey parentObjKey = orderedKeyForParentObj.get(i);
				
				// extract the key value from the SIF Object based on xPath of dependent object info
				Element elem = sifObject.getElementOrAttribute(xpathPrefix+dependentKeyInfoList.get(i).getXpath());
				if (elem != null)
				{
					parentObjKey.setValue(elem.getTextValue());
				}
				else
				{
					logger.error("There is no key defined for the Object "+ sourceObj.getName() + " with key of "+dependentKeyInfoList.get(i).getXpath());
					allKeysHaveValue = false;
				}
			}			
		}

		return allKeysHaveValue;
	}
    
	/*
	 * This method attempts to extract the primary key of the given SIFObject. If it fails then an IllegalArgumentException is
	 * returned and the error is logged accordingly.
	 */
	private String extractFlatKey(SIFDataObject sifObject, List<SIFObjectKey> orderedKeylist, String separator)  throws IllegalArgumentException
	{
		boolean allKeysHaveValue = true;
		try
		{
			for (int i=0; i<orderedKeylist.size(); i++)
			{
				SIFObjectKey key = orderedKeylist.get(i);
					
				// extract the key value from the SIF Object based on xPath of dependent object info
				Element elem = sifObject.getElementOrAttribute(key.getXpath());
				if (elem != null)
				{
					key.setValue(elem.getTextValue());
				}
				else
				{
					logger.error("There is no key defined for the Object "+ sifObject.getElementDef().name() + " with key of "+key.getXpath());
					allKeysHaveValue = false;
				}
			}
			if (allKeysHaveValue)
			{
				return flattenOrderedKey(orderedKeylist, separator);
			}
			else
			{
	    		String errmsg = "Failed to extract and flatten primary key from "+sifObject.getElementDef().name()+". See prvious error log entry for details.";
	    		logger.error(errmsg);
	    		throw new IllegalArgumentException(errmsg+"\n"+sifObject.toXML());
			}
		}
		catch (ADKSchemaException ex)
		{
    		String errmsg = "Failed to extract and flatten primary key from "+sifObject.getElementDef().name()+"\n"+sifObject.toXML();
    		logger.error(errmsg);
    		throw new IllegalArgumentException(errmsg, ex);			
		}
	}

	private String flattenOrderedKey(List<SIFObjectKey> orderedKeylist, String separator)
	{		
		// Now the list is in the correct order, we can start flatten the key into a string
		StringBuffer flatKey = new StringBuffer();
		boolean addSeparator = false;
		for (SIFObjectKey key : orderedKeylist)
		{
			if (addSeparator)
			{
				flatKey.append(separator);
			}
			flatKey.append(key.getValue());
			addSeparator= true;
		}
		
		return flatKey.toString();
	}
	
	private void checkMetadataCache() throws IllegalArgumentException
	{
		if (metadataCache == null) // we cannot determine info about object if cache is not enabled.
		{
			String errorMsg = "MetadataCache not inistialised. Cannot execute requested service method in DOCService class.";
			logger.error(errorMsg);
   			throw new IllegalArgumentException(errorMsg);
   		}
	}
	
	/*
	 * This method takes the given exception and simply re-throws it if it is a IllegalArgumentException, PersistenceException.
	 * Any other exception is mapped to a persistence exception since this service mainly deals with DB operations.
	 * The given errorMsg is added to the IllegalArgumentException or PersistenceException if addErrorMsgToStandardEx is
	 * true. If the exception is any other type then the error message is added regardless addErrorMsgToStandardEx parameter.
	 * The errorMsg is logged if logError is set to true.
	 */
	private void exceptionMapper(Exception ex, String errorMsg, boolean logError, boolean addErrorMsgToStandardEx) throws IllegalArgumentException, PersistenceException
	{
		if (logError)
		{
			logger.error(errorMsg, ex);
		}
		if (ex instanceof IllegalArgumentException)
		{
			if (addErrorMsgToStandardEx)
			{
				throw new IllegalArgumentException(errorMsg, ex);
			}
			throw (IllegalArgumentException)ex;				
		}
		if 	(ex instanceof PersistenceException)
		{
			if (addErrorMsgToStandardEx)
			{
				throw new PersistenceException(errorMsg, ex);
			}
			throw (PersistenceException)ex;			
		}
		
		// If we get here the ex is of any other type
		throw new PersistenceException(errorMsg, ex);
	}
	
}
