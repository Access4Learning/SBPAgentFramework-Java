/*
 * DOCacheDAO.java
 * Created: 14/10/2011
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

package systemic.sif.sbpframework.persist.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

import systemic.sif.sbpframework.persist.common.BasicTransaction;
import systemic.sif.sbpframework.persist.common.HibernateUtil;
import systemic.sif.sbpframework.persist.model.DOCObject;
import systemic.sif.sbpframework.persist.model.DOCache;
import au.com.systemic.framework.utils.StringUtils;

/**
 * @author Joerg Huber
 *
 */
public class DOCacheDAO extends BaseDAO
{
	
	protected final Logger logger = Logger.getLogger(getClass());
	
    /**
	 * This method attempts to retrieve a cached SIF Object based on the object name, object key (flatten key) and the
	 * agentId. Since each object can only be provided from one zone the zoneID is irrelevant and is not required to
	 * determine if an object is already cached. If the object isn't cached then null is returned.
	 *
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
	 * @param sifObjectName the name of the SIF Object for which the cached object info shall be returned.
	 * @param flatKey A flattened key of the cached object that makes this object unique.
	 * @param applicationId The application ID for which the cached object shall be returned. 
	 * @param zoneId The zone ID for which the cached object shall be returned. 
	 * @param loadAll TRUE=>Load all children elements (lazy loading forced). FALSE no children objects are loaded. 
	 * 
     * @return The DOCache object if it exists in the cache already, null if it does not exist.
     * 
	 * @throws IllegalArgumentException: Any of the arguments is null.
	 * @throws PersistenceException: There is an issue with the underlying database. An error is logged.
	 */
    @SuppressWarnings("unchecked")
    public DOCache retrieveCachedObject(BasicTransaction tx, String sifObjectName, String flatKey, String applicationId, String zoneId, boolean loadAll) throws IllegalArgumentException, PersistenceException
    {
        if (StringUtils.isEmpty(sifObjectName) || StringUtils.isEmpty(flatKey) ||  StringUtils.isEmpty(applicationId)  ||  StringUtils.isEmpty(zoneId))
        {
            throw new IllegalArgumentException("sifObjecttName, flatKey, applicationId or zoneId is empty or null.");
        }

        try
        {
            Criteria criteria = tx.getSession().createCriteria(DOCache.class)
               .add(Restrictions.eq("sifObjectName", sifObjectName))
               .add(Restrictions.eq("objectKeyValue", flatKey))
               .add(Restrictions.eq("applicationId", applicationId))
               .add(Restrictions.eq("zoneId", zoneId));

            List<DOCache> cachedObjectList = criteria.list();
            
            // There can only be a maximum of one
            if (cachedObjectList.isEmpty()) // not in cache, yet
            {
            	logger.debug("No entry in DOC for application = '"+ applicationId + "', Sif Object = '" + sifObjectName+ "', zone = '" + zoneId + "' and flattened Key = '"+flatKey+"'.");
                return null;
            }
            else // already cached
            {
            	DOCache cachedObject = cachedObjectList.get(0);
            	
            	if (loadAll)
            	{
            		HibernateUtil.loadSubObject(cachedObject.getDependentObjects());
            	}
                return cachedObject;
            }
        }
        catch (HibernateException e)
        {
            throw new PersistenceException("Unable to retrieve DOCache for application = '"+ applicationId + "', Sif Object = '" + sifObjectName+ "', zone = '" + zoneId + "' and flattened Key = '"+flatKey+"'.", e);
        }
    }

    /**
     * This method saves the given object to the DB. all sub-elements are saved as well. After the save the cacheObject
     * will have a new ID if it is a new object.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param cacheObject The object to save to the cache.
     * 
     * @throws IllegalArgumentException  cacheObject parameter is null.
     * @throws PersistenceException      A database error occurred.
     */
    public void save(BasicTransaction tx, DOCache cacheObject) throws IllegalArgumentException, PersistenceException
    {
        if (cacheObject == null)
        {
            throw new IllegalArgumentException("cacheObject is null.");
        }

        try
        {
        	tx.getSession().saveOrUpdate(cacheObject);
        }
        catch (HibernateException e)
        {
            throw new PersistenceException("Unable to save cacheObject " + cacheObject + ".", e);
        }    	
    }

    /**
     * This method saves the given object to the DB. all sub-elements are saved as well. After the save the cacheObject
     * will have a new ID if it is a new object.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param docObject The object to save to the cache.
     * 
     * @throws IllegalArgumentException  cacheObject parameter is null.
     * @throws PersistenceException      A database error occurred.
     */
    public void save(BasicTransaction tx, DOCObject docObject) throws IllegalArgumentException, PersistenceException
    {
        if (docObject == null)
        {
            throw new IllegalArgumentException("docObject is null.");
        }

        try
        {
        	tx.getSession().saveOrUpdate(docObject);
        }
        catch (HibernateException e)
        {
            throw new PersistenceException("Unable to save dependent object " + docObject + ".", e);
        }    	
    }

    /**
     * This method checks if the 'objectToTest' has already been cached for a requested. If so the object is returned
     * so that it can be re-used in another's cached object dependent list. If the object has not yet been cached
     * then null is returned.
     * The 'objectToTest' must hold the following properties for this method to succeed:
     *  - sifObjectName
     *  - applicationId
	 *  - objectKeyValue
	 *  - zoneId
     * If one of the above property is empty or null then a IllegalArgumentException is raised.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param objectToTest The object to find in the DOC.
     * 
     * @return See description.
     * 
     * @throws IllegalArgumentException  objectToTest is null or any of the properties listed above is empty or null.
     * @throws PersistenceException      A database error occurred.
     */
    public DOCObject getCachedDependentObject(BasicTransaction tx, DOCObject objectToTest) throws IllegalArgumentException, PersistenceException
    {
    	return retrieve(tx, objectToTest);
    }
    
    /**
     * This method returns a list of dependent objects that have already been cached for a given SIF Object. If there are
     * no dependent objects that already been cached then an empty list is returned. If the object defined by its parameters
     * is not cached at all then null is returned and an info message is logged.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param sifObjectName The object name for which the dependent object list with already cached objects shall be returned.
     * @param flatKey The key of the above object.
     * @param applicationId The application Id for which the object is registered in the cache.
     * @param zoneId The zone Id for which the object is registered in the cache.
     * 
     * @return See description
     * 
	 * @throws IllegalArgumentException: Any of the arguments is null.
	 * @throws PersistenceException: There is an issue with the underlying database. An error is logged.
     */
    public Set<DOCObject> getAlreadyCachedDependentObjects(BasicTransaction tx, String sifObjectName, String flatKey, String applicationId, String zoneId) throws IllegalArgumentException, PersistenceException
    {
    	// Get the object from the cache.
    	DOCache cachedObject = retrieveCachedObject(tx, sifObjectName, flatKey, applicationId, zoneId, true);
    	if (cachedObject == null)
    	{
    		return null;
    	}
    	
    	return cachedObject.getDependentObjects();    	
    }
    
    /**
     * This method removes the docObject from the DO cache. Since there can be many objects in the DO Cache that 
     * depend on the given object all these need to be updated accordingly.
     * For this method to work, the following properties in the docObject must be set:
     *  - sifObjectName
     *  - applicationId
     *  - objectKeyValue
     *  - zoneId
     *  If the object is not cached as a dependent object then no action is taken.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param docObject The object for which the dependencies shall be returned.
     * 
     * @throws IllegalArgumentException  docObject is null or any of the properties listed above is empty or null.
     * @throws PersistenceException      A database error occurred.
     */
    @SuppressWarnings("unchecked")
    public void removeDependency(BasicTransaction tx, DOCObject docObject) throws IllegalArgumentException, PersistenceException
    {
    	DOCObject depObj = retrieve(tx, docObject);
    	if (depObj != null) // there are waiting objects for this one.
    	{
	    	Criteria criteria = tx.getSession().createCriteria(DOCache.class).createAlias("dependentObjects", "depObj")
		        .add(Restrictions.eq("depObj.sifObjectName", docObject.getSifObjectName()))
		        .add(Restrictions.eq("depObj.objectKeyValue", docObject.getObjectKeyValue()))
		        .add(Restrictions.eq("depObj.applicationId", docObject.getApplicationId()))
		        .add(Restrictions.eq("depObj.zoneId", docObject.getZoneId()));
	        
	        List<DOCache> cachedObjectList = criteria.list();
	        if (!cachedObjectList.isEmpty())
	        {
	        	for (DOCache parentObj : cachedObjectList)
	        	{
	    			// remove depObj from parents dependent object list
	    			parentObj.getDependentObjects().remove(depObj);
	    			parentObj.setRemainingDependencies(parentObj.getDependentObjects().size());
	    			tx.getSession().saveOrUpdate(parentObj);
	        	}
	        }
	        
	        // This is possibly not required because of the 'cascade=all,delete-orphan' in the hibernate mapping
	        tx.getSession().delete(depObj); 
    	}
    }
    
    /**
     * This method returns a list of DOC Objects. The objects are those that have not been requested, yet (requested=false).
     * 
     * Note: 
     * If an agent subscribes to more than one zone for the same sifObject then this method must be called
     * separately for each zone by the agent. If there are no cached and not yet requested objects for the
     * given parameters then an empty list is returned.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param sifObjectName The SIF Object names to search for (i.e StudentPersonal)
     * @param applicationId Only return objects marked for this application.
     * @param zoneId Only return objects marked for this zone.
     * 
     * @return See description
     * 
     * @throws IllegalArgumentException  Any of the parameters is null or empty.
     * @throws PersistenceException      A database error occurred.
     */
    @SuppressWarnings("unchecked")
    public List<DOCObject> getNotYetRequestedObjects(BasicTransaction tx, String sifObjectName, String applicationId, String zoneId) throws IllegalArgumentException, PersistenceException
    {
		if (StringUtils.isEmpty(sifObjectName) || StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(zoneId))
		{
			throw new IllegalArgumentException("Some of the following parameters are either null or empty: sifObjectName, applicationId, zoneId");
		}
        try
        {
            Criteria criteria = tx.getSession().createCriteria(DOCObject.class)
               .add(Restrictions.eq("sifObjectName", sifObjectName))
               .add(Restrictions.eq("applicationId", applicationId))
               .add(Restrictions.eq("zoneId", zoneId))
               .add(Restrictions.eq("requested", Boolean.FALSE));

            return criteria.list();
        }
        catch (Exception ex)
        {
            throw new PersistenceException("Unable to retrieve list of DOCObjects for application = '"+ applicationId + "', Sif Object = '" + sifObjectName+ "', zone = '" + zoneId +"'.", ex);
        }	
    }
    
    /**
     * This method gets all cached objects of a given type for a particular application and agent that have no 
     * remaining dependencies. These are the candidates do be removed later and be processed by the appropriate 
     * subscriber. If there are no Cached Objects without dependencies then an empty list is returned.
     *  
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param sifObjectName The type of cached objects to be returned.
     * @param applicationId The application for which the objects shall be returned.
     * @param agentId The agent for which to get the object list. 
     * 
     * @return See description.
     * 
     * @throws IllegalArgumentException  Any of the parameters is null or empty.
     * @throws PersistenceException      A database error occurred. Error is logged.
     */
    @SuppressWarnings("unchecked")
    public List<DOCache> getObjectsWithoutDependencies(BasicTransaction tx, String sifObjectName, String applicationId, String agentId) throws IllegalArgumentException, PersistenceException
    {
		if (StringUtils.isEmpty(sifObjectName) || StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(agentId))
		{
			throw new IllegalArgumentException("Some of the following parameters are either null or empty: sifObjectName, applicationId, agentId");
		}
        try
        {
            Criteria criteria = tx.getSession().createCriteria(DOCache.class)
               .add(Restrictions.eq("sifObjectName", sifObjectName))
               .add(Restrictions.eq("applicationId", applicationId))
               .add(Restrictions.eq("agentId", agentId))
               .add(Restrictions.eq("remainingDependencies", 0));

            return criteria.list();
        }
        catch (Exception ex)
        {
            throw new PersistenceException("Unable to retrieve list of Cached Objects without dependencies for application = '"+ applicationId + "', Sif Object = '" + sifObjectName + "'and agent = '" + agentId + "'.", ex);
        }	
    }
    
    /**
     * This method returns all objects in the cache that have remaining dependencies but have an expiry date
     * older than the current date and time. These are the candidates that need to either be removed or 
     * re-requested by the agent. If there are no expired objects then an empty list is returned.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param applicationId Only return expired objects for this application.
     * @param agentId Only return expired object that were initially cached by this agent.
     * 
     * @return See description.
     * 
     * @throws IllegalArgumentException  Any of the parameters is null or empty.
     * @throws PersistenceException      A database error occurred. Error is logged.
     */
    @SuppressWarnings("unchecked")
    public List<DOCache> getExpiredObjects(BasicTransaction tx, String applicationId, String agentId) throws IllegalArgumentException, PersistenceException
    {
		if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(agentId))
		{
			throw new IllegalArgumentException("Some of the following parameters are either null or empty: applicationId, agentId");
		}
        try
        {
            Criteria criteria = tx.getSession().createCriteria(DOCache.class)
               .add(Restrictions.eq("applicationId", applicationId))
               .add(Restrictions.eq("agentId", agentId))
               .add(Restrictions.gt("remainingDependencies", 0))
               .add(Restrictions.le("expiryDate", new Date()));

            return criteria.list();
        }
        catch (Exception ex)
        {
            throw new PersistenceException("Unable to retrieve list of expired Cached Objects for application = '"+ applicationId + "'and agent = '" + agentId + "'.", ex);
        }	
    }

    /**
     * This method removes the given cached dependent object. If the object is null then no action is taken. The
     * object's id property must be set for this method to work.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param docObject The object to remove from the DOC_OBJECT table.
     * 
     * @throws PersistenceException      A database error occurred.
     */
    public void removeDependentObject(BasicTransaction tx, DOCObject docObject)throws PersistenceException
    {
        try
        {
            if ((docObject != null) && (docObject.getId() != null) && (docObject.getId().longValue() > 0))
            {
            	tx.getSession().delete(docObject);
            }
        }
        catch (HibernateException e)
        {
            throw new PersistenceException("Unable to remove dependent object " + docObject + ".", e);
        }    	    	
    }
    
    /**
     * This method removes the given object and all its dependencies from the cache. If the object is null then
     * no action is taken.
     * 
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     * @param cacheObject The object to remove from the DO Cache.
     * 
     * @throws PersistenceException      A database error occurred.
     */
    public void removeCachedObject(BasicTransaction tx, DOCache cacheObject)throws PersistenceException
    {
        try
        {
            if (cacheObject != null)
            {
            	Set<DOCObject> depObjectList = cacheObject.getDependentObjects();
            	tx.getSession().delete(cacheObject);
            	
            	if (depObjectList != null)
            	{
            		for (DOCObject depObj : depObjectList)
            		{
            			// If the number of parents is 1 then this is the last dependency and it can be removed.
            			if (depObj.getParents().size() <= 1)
            			{
            				removeDependentObject(tx, depObj);
            			}
            		}
            	}
            }
        }
        catch (HibernateException e)
        {
            throw new PersistenceException("Unable to remove cacheObject " + cacheObject + ".", e);
        }    	    	
    }
    

    /*---------------------*/
    /*-- Private methods --*/
    /*---------------------*/
    
    /*
     * For this method to work, the following properties in the docObject must be set:
     *  - sifObjectName
     *  - applicationId
     *  - objectKeyValue
     *  - zoneId
     *  If the object is not cached as a dependent object then no action is taken.
     *  
	 * @param tx The Transaction within this method shall operate. MUST NOT BE NULL!
     *  
    * @throws IllegalArgumentException  docObject is null or any of the properties listed above is empty or null.
    * @throws PersistenceException      A database error occurred.
    */
   @SuppressWarnings("unchecked")
   public DOCObject retrieve(BasicTransaction tx, DOCObject docObject) throws IllegalArgumentException, PersistenceException
   {
		if (docObject == null)
		{
			throw new IllegalArgumentException("docObject == null! Not allowed.");
		}
		if (StringUtils.isEmpty(docObject.getSifObjectName())
		        || StringUtils.isEmpty(docObject.getApplicationId())
		        || StringUtils.isEmpty(docObject.getZoneId())
		        || StringUtils.isEmpty(docObject.getObjectKeyValue()))
		{
			throw new IllegalArgumentException("Some of the following properties in the 'docObject' are empty or null: sifObjectName, applicationId, zoneId, objectKeyValue");
		}

		Criteria criteria = tx.getSession().createCriteria(DOCObject.class)
		        .add(Restrictions.eq("sifObjectName", docObject.getSifObjectName()))
		        .add(Restrictions.eq("objectKeyValue", docObject.getObjectKeyValue()))
		        .add(Restrictions.eq("applicationId", docObject.getApplicationId()))
		        .add(Restrictions.eq("zoneId", docObject.getZoneId()));

		List<DOCObject> cachedObjectList = criteria.list();
		
		// There can only be a maximum of one
		if (cachedObjectList.isEmpty())
		{
			return null; // No dependency on this object => return null.
		}
		else
		{
			return cachedObjectList.get(0);
		}
   }
    
}
