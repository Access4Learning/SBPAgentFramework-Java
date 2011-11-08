/*
 * SyncSubscriber.java
 * Created: 04/11/2011
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

import openadk.library.ADKException;
import openadk.library.ElementDef;
import openadk.library.Query;
import openadk.library.Zone;
import systemic.sif.sbpframework.common.utils.SIFObjectMetadataCache;
import systemic.sif.sbpframework.persist.servcie.SIFSyncService;
import systemic.sif.sifcommon.subscriber.BaseSubscriber;


/**
 * This Class implements the Sync Control Functionality through some tables rather than through the default behaviour of
 * the SIFCommon Framework. This is simply done by overwriting the default sync(Zone zone) method from the BaseSubscriber
 * class in the SIFCommon Framework.<p><p>
 * 
 * It is important to note that if the SIFCommon Framework property file turns off sync altogether then the method in this
 * class has no affect, meaning it will be ignored. This allows a developer to control the sync from outside this bit of
 * code and if there is a need that the agent should never call a sync then the agent._agentid_.sync.frequency 
 * property in the SIFAgent.properties should be set to 0. Please refer to the Developer's Guide of the SIFCommon
 * Framework.
 * 
 * @author Joerg Huber
 *
 */
public abstract class SyncSubscriber extends BaseSubscriber
{
	/* There are some spots this metadata cache is required. */
	protected SIFObjectMetadataCache metadataCache = SIFObjectMetadataCache.getCache();

	protected static final String BANNER = "\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";

	/*
	 * Default constructor
	 */
	public SyncSubscriber(String subscriberID, ElementDef dtd)
	{
        super(subscriberID);
		setDtd(dtd);
	}

	/*
	 * This method overrides the default sync behaviour of the SIFCommon Framework Base Subscriber.
	 * @see systemic.sif.sifcommon.subscriber.BaseSubscriber#sync(openadk.library.Zone)
	 */
    @Override
    public void sync(Zone zone) throws ADKException
    {
        try
        {
        	SIFSyncService service = new SIFSyncService();
            if (service != null)
            {
                // Test if there are any sync required.
                boolean requireSync = service.requiresSyncForObjectInZone(getDtd().name(), getAgentID(), zone.getZoneId());
    
                logger.info(BANNER+getClass().getSimpleName()+".sync() for agent = '" + getAgentID() + "', object = '"+getDtd().name()+"' in zone = '"+zone.getZoneId()+"' required: "+(requireSync ? "YES" : "NO")+BANNER);
                if (requireSync)
                {
                    Query query = new Query(getDtd());
                    query.setSIFVersions(getAgentConfig().getVersion());
            		addToInitialSyncQuery(query, zone); // Add any query conditions you may have
                    zone.query(query);
                    
                    // Now update the Sync info
                    service.markSIFZoneAsSyncedForObject(getDtd().name(), getAgentID(), zone.getZoneId());
                }
            }
        }
        catch (Exception ex)
        {
            logger.error(BANNER+"Sync for SIF Object '"+getDtd().name()+"' in zone '"+zone.getZoneId()+"' failed: " + ex.getMessage()+BANNER);
        }        
    }

}
