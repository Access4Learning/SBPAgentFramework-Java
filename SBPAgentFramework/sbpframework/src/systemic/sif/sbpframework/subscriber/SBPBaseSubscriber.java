/*
 * SBPBaseSubscriber.java
 * Created: 08/10/2011
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

package systemic.sif.sbpframework.subscriber;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import openadk.library.ElementDef;
import openadk.library.EventAction;
import openadk.library.Query;
import openadk.library.SIFDataObject;
import openadk.library.Zone;
import systemic.sif.sbpframework.common.utils.SIFObjectUtils;
import systemic.sif.sbpframework.persist.model.DOCObject;
import systemic.sif.sbpframework.persist.model.DOCache;
import systemic.sif.sbpframework.persist.model.SIFObject;
import systemic.sif.sbpframework.persist.model.SIFObjectKey;
import systemic.sif.sbpframework.persist.servcie.DOCService;
import systemic.sif.sifcommon.mapping.MappingInfo;

/**
 * This class implements some of the functionality defined in the Australian SBP. This are all infrastructure level
 * functions and include: Start-up Sync Control through a DB and a dependent Object cache. For details please refer to
 * the SBP Agent Framework Developer's Guide.<p><p>
 * 
 * <b>Note:</b><p>
 * Subscribers that are part of the SBP must extend this SBPBaseSubscriber. If your agent deals with other SIF objects 
 * than the ones defined in the SBP then you should extends the SBPNoDOCSubscriber class for these objects and not this
 * class. If you use this class for any other SIF objects than defined in the SBP the behaviour is unknown, most likely
 * will cause your agent to crash.
 * 
 * @author Joerg Huber
 *
 */
public abstract class SBPBaseSubscriber extends SyncSubscriber
{
	/* The Service for the Dependent Object Cache*/
	private DOCService service = new DOCService();
	
	/**
	 * This method needs to be implemented by the actual subscriber developer. It shall check if the dependent
	 * object given by the sifObjectName parameter and the key given by the keyValues does exist in the target 
	 * system. If it exists then TRUE must be returned if it doesn't exist then FALSE must be returned.<p>
	 * If the DOC is enabled and this method returns TRUE for the given sifObjectName and keyValues then the
	 * SIF Object given by the sifObject parameter will be cached and its dependent objects will automatically be
	 * requested by an appropriate subscriber (which can be a subscriber in another agent).<p><p>
	 * 
	 * The keyValues holds the primary key (name as xpath and value) of the dependent SIF Object. In most cases 
	 * this is a SIF refId, but in some cases can be something else. The keyValues parameter is a list of key 
	 * values in case the primary key is made up of a compound key. The list will be in the correct order as 
	 * defined by the SIF specification.<p><p>
	 * 
	 * The parameter sifObjectName and keyValues are extracted from the sifObject. The sifObject is provided for
	 * convenience. There might be the need to get other information out of the sifObject parameter to implement
	 * this method successfully and therefore the availability of this parameter.<p><p>
	 * 
	 * Example of parameters: StudentSchoolEnrollment
	 * <code>
	 * <StudentSchoolEnrollment RefId="A8C3D3E34B359D75101D00AA001A1652">
  	 *   <StudentPersonalRefId>D3E34B359D75101A8C3D00AA001A1652</StudentPersonalRefId>
     *   <SchoolInfoRefId>A4E33E359D99101A8C3D00AA001BB76E</SchoolInfoRefId>
     *   <MembershipType>01</MembershipType>
	 *    .....
	 * </StudentSchoolEnrollment>
	 * </code>
	 * <p><p>
	 * For the above example object the 'sifObject' parameter would be StudentSchoolEnrollment object.
	 * The 'doesObjectExistInTargetSystem()' method would be called twice for the above object by the 
	 * SBPBaseSubscriber because there are 2 dependent objects: <p>
	 * - The Student with RefId=D3E34B359D75101A8C3D00AA001A1652<p>
	 * - The School with RefId=A4E33E359D99101A8C3D00AA001BB76E<p><p>
	 * 
	 * The sifObjectName and keyValues parameters for the two calls would be:<p>
	 * 1st Call (Student): sifObjectName=StudentPersonal<p>
	 *                     keyValues[0].xpathToKey=@RefId, keyValues[0].keyValue=D3E34B359D75101A8C3D00AA001A1652<p>
	 * 2nd Call (School) : sifObjectName=SchoolInfo<p>
	 *                     keyValues[0].xpathToKey=@RefId, keyValues[0].keyValue=A4E33E359D99101A8C3D00AA001BB76E<p><p>
	 * 
	 * @param sifObjectName Name of the dependent SIF Object (i.e. StudentPersonal, SchoolInfo etc).
	 * @param keyValues List of key values. Name of Key is an xpath into the dependent SIF Object, value is the 
	 *                  actual value of key as extracted from the sifObject parameter.
	 * @param sifObject The SIF Object as delivered from the ZIS.
	 * 
	 * @return See description
	 */
	public abstract boolean doesObjectExistInTargetSystem(String sifObjectName, List<SIFObjectKey> keyValues, SIFDataObject sifObject);
	
	
	/**
	 * Default constructor<p>
	 * Initialises the subscriber and also checks if the object this subscriber is responsible for is supported by
	 * the DOC. If not then a IllegalArgumentException is raised and the error is logged.
	 * 
	 * @param subscriberID The unique ID of the subscriber (ie. StudentPersonalSubscriber).
	 * @param dtd The dtd for which this subscriber is responsible for.
	 */
	public SBPBaseSubscriber(String subscriberID, ElementDef dtd) throws IllegalArgumentException
	{
        super(subscriberID, dtd);
        
        // Check if the given SIF Object is supported by the DOC. IF not we must throw an exception
        SIFObject obj = metadataCache.getObjectMetadata(getDtd().name());
        if (obj == null) //not known to DOC
        {
            String errorMsg = getDtd().name()+" is not a SIF Object Type that is supported by this subscriber class. Use SBPNoDOCSubscriber class instead.";
            logger.error("\n################################# ERROR ###################################\n"+errorMsg+"\n###########################################################################\n");
            throw new IllegalArgumentException(errorMsg);
        }
		
        logger.debug(BANNER+getClass().getSimpleName()+" Subscriber created for object = '"+getDtd().name()+"'."+BANNER);
	}
	
    /*---------------------------------------------------------*/
    /*- Housekeeping Methods for the SBP Subscriber Behaviour -*/
    /*---------------------------------------------------------*/

	/**
     * Sends a SIF Query for all dependent objects that are in the cache for this subscriber and application.
     * This will iterate through all zones of this agent to ensure that all dependent objects that have been
     * received from various zones are requested.<p>
     * This method must only run if this subscriber is responsible for a SIF Object that has other SIF objects
     * with a dependency on this object. For example the StudentPersonal is such an object because a number of 
     * other SIF objects depend upon the StudentPersonal such as the StudentSchoolEnrollment, 
     * StudentContactRelationship etc.
     * 
     */
    public void requestDependentObjects()
    {
        if (metadataCache.isDependedUpon(getDtd().name()))
        {
            logger.debug(BANNER+getClass().getSimpleName()+" Subscriber attempts to request pending objects...: "+new Date()+BANNER);
            try
            {
                for (Zone zone : getZones())
                {
                    List<DOCObject> objectToRequest = service.getNotYetRequestedObjects(getDtd().name(), getApplicationID(), zone.getZoneId());
                    for (DOCObject docObj : objectToRequest)
                    {
                        // Use an inner try-block so that individually failed query requests don't stop others of being issued.
                        try
                        {
                            Query query = SIFObjectUtils.makeQueryFromXPathValueList(getDtd(), getAgentConfig().getVersion(), docObj.getKeyForDependentObject());
                            zone.query(query);// Send the query to the Zone.
                            
                            // Now we need to update the docObject to indicate that query has been issued.
                            service.markDependentObjectAsRequested(docObj, getAgentID(), zone.getZoneId());
                        }
                        catch (Exception ex)
                        {
                            logger.error("Failed to issue query for Cached Object:\n"+docObj, ex);
                        }
                    }
                }
            }
            catch (Exception ex) //  should only be IllegalArgumentException, PersistenceException
            {
                logger.error("Failed to retrive lis of not yet requested objects for subscriber: "+getDtd().name(), ex);            
            }
        }
//      else
//      {
//          logger.debug(BANNER+getClass().getSimpleName()+" Subscriber is not required to request pending objects because no objects depend on this one: "+new Date()+BANNER);         
//      }
    }
    
    /**
     * Should be called every so often. This method will check if there are cached objects that have no 
     * dependencies any more. If so the object can be processed by this subscriber and then removed from 
     * the cache. This process is only required for objects that have dependencies such as StaffAssignment.
     * For objects without dependencies such as StudentPersonal there is no need to run this method.
     */
    public void processObjectsWithoutDependencies()
    {
        if (metadataCache.hasDependencies(getDtd().name()))
        {
            logger.debug(BANNER+getClass().getSimpleName()+" Subscriber attempts to process objects with no remaining dependencies...: "+new Date()+BANNER);
            List<DOCache> cachedObjectList = null;
            try
            {
                cachedObjectList = service.getObjectsWithoutDependencies(getDtd().name(), getApplicationID(), getAgentID());
            }
            catch (Exception ex) //  should only be IllegalArgumentException, PersistenceException
            {
                logger.error("Failed to retrive list of Cached Objects without any dependencies for subscriber: "+getDtd().name(), ex);         
                cachedObjectList = null;
            }
            if (cachedObjectList != null)
            {
                for (DOCache cachedObject : cachedObjectList)
                {
                    SIFDataObject sifObject =  SIFObjectUtils.getSIFObjectFromXML(cachedObject.getObjectXML());
                    if (sifObject != null)
                    {
                        if (cachedObject.getIsEvent()) // Event Object
                        {
                            pushSIFEventToProcessQueue(sifObject, getZoneByID(cachedObject.getZoneId()), null, EventAction.valueOf(cachedObject.getEventType()));
                        }
                        else // Response Object
                        {
                            pushSIFObjectToProcessQueue(sifObject, getZoneByID(cachedObject.getZoneId()), null);
                        }
                        service.removeCachedObject(cachedObject);
                    }
                    else
                    {
                        logger.error("Failed to push the cached object to the processing queues. See previous error log entry for details. Cached object with issue:\n"+cachedObject);
                    }
                }
            }
        }
//      else
//      {
//          logger.debug(BANNER+getClass().getSimpleName()+" Subscriber does not need to process objects with no remaining dependencies because this Object does not have dependencies: "+new Date()+BANNER);
//      }
    }

    /*--------------------------------------------------------------------------------*/
	/*- Overridden Methods of the SIFCommon Framework to implement the SBP Behaviour -*/
	/*--------------------------------------------------------------------------------*/

	/*
	 * Override the preProcessEvent. This allows to check if objects needs caching and if so then the event 
	 * does not need to be processed further at this stage.
	 * 
	 * @see systemic.sif.sifcommon.subscriber.BaseSubscriber#preProcessEvent(Event event, Zone zone, MappingInfo mappingInfo)
	 */
	@Override
	protected boolean preProcessEvent(SIFDataObject sifObject, EventAction eventAction, Zone zone, MappingInfo mappingInfo)
    {
		//Check if object required caching.
		boolean cached = cacheEventIfRequired(sifObject, eventAction, zone);
		
		// If it was cached then we do not want to do any more processing.
		if (cached)
		{
			logger.debug(eventAction.name()+" Event for Object "+sifObject.getElementDef().name()+" was cached or has already been cached. No futher action performed.");
			return false;
		}
		
		// If we get here then the event was not cached or did not require caching. It still might be that there
		// are other objects in the cache that had a dependency on the object of this event. Remove these
		// dependencies. Only do this if it is not a DELETE event.
    	if (!EventAction.DELETE.name().equals(service.hasPotentialDependencies(eventAction.name())))
    	{
			logger.debug("Check and remove dependency for "+eventAction.name()+" Event on Object "+sifObject.getElementDef().name()+".");
    		removeDependencies(sifObject, zone);
    	}
		
		// Tell the BaseSubscriber to perform the standard processing with this event (i.e. send it to appropriate
		// queue.
		return true;
    }
    
	/*
	 * Override the preProcessQueryResults. This allows to check if objects needs caching and if so then the 
	 * object  does not need to be processed further at this stage.
	 * 
	 * @see systemic.sif.sifcommon.subscriber.BaseSubscriber#preProcessQueryResults(openadk.library.SIFDataObject, openadk.library.Zone, systemic.sif.sifcommon.mapping.MappingInfo)
	 */
	 
	@Override
	protected boolean preProcessQueryResults(SIFDataObject sifObject, Zone zone, MappingInfo mappingInfo)
	{
		//Check if object required caching.
		boolean cached = cacheObjectIfRequired(sifObject, zone);
		
		// If it was cached then we do not want to do any more processing.
		if (cached)
		{
			logger.debug("Object "+sifObject.getElementDef().name()+" was cached or has already been cached. No futher action performed.");
			return false;
		}
		
		// If we get here then the object was not cached or did not require caching. It still might be that there
		// are other objects in the cache that had a dependency on this object. Remove these dependencies.
		logger.debug("Check and remove dependency on Object "+sifObject.getElementDef().name()+".");
		removeDependencies(sifObject, zone);
		
		// Tell the BaseSubscriber to perform the standard processing with this object (i.e. send it to appropriate queue.
    	return true;		
	}

	/*--------------------------------------------------*/
	/*- Private Methods to implement the SBP Behaviour -*/
	/*--------------------------------------------------*/
    
    /*
     * Needs to be called for responses. Check if object has dependencies, if so call abstract method
     * doesObjectExistInTargetSystem() for each dependent object and if there are still remaining dependencies
     * the object needs to be cached with the remaining dependencies.
     * If the object has been cached then true is returned otherwise false is returned. False indicates that
     * there was no need to cache the object because is has no outstanding dependencies.
     */
    private boolean cacheObjectIfRequired(SIFDataObject sifObject, Zone zone)
    {
    	boolean cached = false;
    	if (sifObject == null)
    	{
    		return false;
    	}
    	
    	if (service.hasPotentialDependencies(sifObject.getElementDef().name()))
    	{
    		// Next we test if the object is already in the cache. If so we don't need to cache at all.
    		String flatKey = service.extractFlatKey(sifObject);
    		DOCache cachedObject = service.retrieveCachedObject(sifObject.getElementDef().name(), flatKey, getApplicationID(), zone.getZoneId(), false);
    		if (cachedObject == null) // not in cache, yet.
    		{  		
	    		List<DOCObject> remainingDependencies = getRemainingDependencies(sifObject, zone.getZoneId());
	    		if (remainingDependencies != null) // we have dependencies => cache object
	    		{
	    			cached = cacheObject(sifObject, flatKey, false, null, remainingDependencies, zone);
	    		}
    		}
    		else // already in cache => mark it as cached.
    		{
    			cached = true;
    		}
    	}
    	return cached;
    }
    
    /*
     * Needs to be called for events. Check if event object has dependencies, if so call abstract method
     * doesObjectExistInTargetSystem() for each dependent object and if there are still remaining dependencies
     * the event needs to be cached with the remaining dependencies.
     * If the object within the event has been cached then true is returned otherwise false is returned. 
     * False indicates that there was no need to cache the object because is has no outstanding dependencies.
     * 
     * NOTE: Only ADD and UPDATE events need to be dealt with that way. DELETE events don't require any of this.
     *       If the event type is DELETE then false is returned as no caching is required for delete events.
     */
    private boolean cacheEventIfRequired(SIFDataObject sifObject, EventAction eventAction, Zone zone)
    {
    	boolean cached = false;
    	if (sifObject == null)
    	{
    		return false;
    	}
    	if (!EventAction.DELETE.name().equals(service.hasPotentialDependencies(eventAction.name())))
    	{
        	if (service.hasPotentialDependencies(sifObject.getElementDef().name()))
        	{
        		// Next we test if the object is already in the cache. If so we don't need to cache at all.
        		String flatKey = service.extractFlatKey(sifObject);
        		DOCache cachedObject = service.retrieveCachedObject(sifObject.getElementDef().name(), flatKey, getApplicationID(), zone.getZoneId(), false);
        		if (cachedObject == null) // not in cache, yet.
        		{  		
    	    		List<DOCObject> remainingDependencies = getRemainingDependencies(sifObject, zone.getZoneId());
    	    		if (remainingDependencies != null) // we have dependencies => cache object
    	    		{
    	    			cached = cacheObject(sifObject, flatKey, true, eventAction, remainingDependencies, zone);
    	    		}
        		}
        		else // already in cache => mark it as cached.
        		{
        			cached = true;
        		}
        	}    		
    	}
    	return cached;
    }
    
    /*
     * This method checks if the given object, that has been received by a given subscriber and zone is an
     * object for which we have other objects in the cache that depend on this one. If so these dependencies
     * will be removed and this object will be processed as normal.
     */
    private void removeDependencies(SIFDataObject sifObject, Zone zone)
    {
    	try
    	{
    		service.checkAndRemoveDependency(sifObject, getApplicationID(), zone.getZoneId());
    	}
    	catch (Exception ex) //  should only be IllegalArgumentException, PersistenceException
    	{
			logger.error("Failed to check or remove dependencies for the object:\n"+sifObject.toXML(), ex);    		
    	}
    }
    
    /*
     * This method gets all dependencies for the given sifObject from the object itself and then looks up the
     * DOCache and marks the already known dependencies in the cache. For all new dependencies the abstract method
     * doesObjectExistInTargetSystem() of this class is called to determine if the dependent objects are unknown
     * in the target system. All dependencies that are returned with TRUE from the abstract method are then 
     * removed from the dependency list as they don't need to be requested. The final dependency list is then 
     * returned. If there are no remaining dependencies then null is returned.
     */
    private List<DOCObject> getRemainingDependencies(SIFDataObject sifObject, String zoneId) throws PersistenceException
    {
    	List<DOCObject> dependencies = service.getDependenciesFromSIFObjectAndCache(sifObject, getApplicationID(), zoneId);
    	if (dependencies != null)
    	{
    		for (Iterator<DOCObject> i=dependencies.iterator(); i.hasNext();)
    		{
    			DOCObject obj = i.next();
    			
    			// Check if object is an already known dependency in the cache. If so there is no need to check
    			// in the target as it would have been checked before and therefore made it to the cache.
    			if ((obj.getId() == null) || (obj.getId().longValue()<=0))
    			{
    				//check in target. If it does exist we can remove it from the dependency list.
    				if (doesObjectExistInTargetSystem(obj.getSifObjectName(), obj.getKeyForDependentObject(), sifObject))
    				{
    					i.remove();
    				}
    			}
    		}
    		if (dependencies.size() == 0) // no remaining dependencies
    		{
    			dependencies = null;
    		}
    	}
    	
    	return dependencies;
    }

    /*
     * This method stores the actual SIF Object with its dependencies in the cache.
     */
	private boolean cacheObject(SIFDataObject sifObject, String flatKey, boolean isEvent, EventAction eventAction, List<DOCObject> dependencies, Zone zone)
	{
		boolean success = true;
		DOCache cachedObject = new DOCache();
		cachedObject.setSifObjectName(sifObject.getElementDef().name());
		cachedObject.setIsEvent(isEvent);
		if (eventAction != null)
		{
			cachedObject.setEventType(eventAction.name());
		}
		cachedObject.setObjectKeyValue(flatKey);
		cachedObject.setObjectXML(sifObject.toXML());
		cachedObject.setDependentObjectsAsList(dependencies);
		try
		{
			service.cacheObject(cachedObject, getAgentID(), getApplicationID(), zone.getZoneId());
		}
		catch (Exception ex)
		{
			logger.error("Faled to cache SIF Object.\n:"+cachedObject, ex);
			success = false;
		}
		return success;
	}

}
