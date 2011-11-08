/*
 * SBPNoDOCSubscriber.java
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

import openadk.library.ElementDef;
import systemic.sif.sbpframework.persist.model.SIFObject;

/**
 * Subscribers for Objects that are not defined as part of the SBP must extend this class rather than the 
 * SBPBaseSubscriber. This class still supports the functionality of controlling start-up sequencing and management
 * through a database, much like the SBP Subscribers, but subscribers extending this class do not have the DOC behind
 * the scenes. This means objects received by this subscriber will be processed immediately rather then checking
 * dependencies and attempting them to be resolved. Only SIF Objects that are not part of the SBP should use this
 * class to implement subscribers.
 * 
 * @author Joerg Huber
 *
 */
public abstract class SBPNoDOCSubscriber extends SyncSubscriber
{

	/**
     * Default Constructor.<p>
     * This class also checks if the SIF Object type it is initialised for would better be initialised with the 
     * SBPBaseSubscriber. This is only applicable if the SIF Object type is known to the metadata cache and therefore
     * should be handled by the DOC and mentioned class. In this case a warning will be logged and the subscriber is
     * still initialised.
     */
    public SBPNoDOCSubscriber(String subscriberID, ElementDef dtd)
    {
	    super(subscriberID, dtd);
	    
        // Check if the given SIF Object is should be using the SBPBaseSubscriber class.
        SIFObject obj = metadataCache.getObjectMetadata(getDtd().name());
        if (obj != null) // known to DOC
        {
            logger.warn("\n################################# WARNING ###################################\n"+getDtd().name()+" is a SIF Object Type that should be using the SBPBaseSubscriber class.\n#############################################################################\n");
        }

        logger.debug(BANNER+getClass().getSimpleName()+" Subscriber created for object = '"+getDtd().name()+"'."+BANNER);
    }

}
