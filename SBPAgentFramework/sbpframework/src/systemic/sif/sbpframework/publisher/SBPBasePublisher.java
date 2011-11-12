/*
 * SBPBasePublisher.java
 * Created: 02/11/2011
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

package systemic.sif.sbpframework.publisher;

import java.util.List;

import openadk.library.ADKException;
import openadk.library.ComparisonOperators;
import openadk.library.Condition;
import openadk.library.ConditionGroup;
import openadk.library.ElementDef;
import openadk.library.Query;
import openadk.library.SIFDataObject;
import openadk.library.SIFErrorCategory;
import openadk.library.SIFErrorCodes;
import openadk.library.SIFException;
import openadk.library.Zone;
import systemic.sif.sbpframework.common.utils.SIFObjectMetadataCache;
import systemic.sif.sbpframework.persist.model.SIFObject;
import systemic.sif.sbpframework.persist.model.SIFObjectKey;
import systemic.sif.sbpframework.publisher.iterator.SingleObjectIterator;
import systemic.sif.sifcommon.publisher.BasePublisher;
import systemic.sif.sifcommon.publisher.SIFResponseIterator;

/**
 * <b>Note:</b><p>
 * Publishers that are part of the SBP must extend this SBPBasePublisher. If your agent deals with other SIF objects 
 * than the ones defined in the SBP then you should extends the NoneSBPBasePublisher class for these objects and not this
 * class. If you use this class for any other SIF objects than defined in the SBP the behaviour is unknown, most likely
 * will cause your agent to crash.
 *
 * @author Joerg Huber
 *
 */
public abstract class SBPBasePublisher extends BasePublisher
{
	/* There are some spots this metadata cache is required. */
	protected SIFObjectMetadataCache metadataCache = SIFObjectMetadataCache.getCache();
	
	/**
	 * This method is called if an unbound query is received by this publisher. According to the SIF Specification
	 * and the SBP Specification this should return all objects for the SIF Object Type of this class.
	 * 
	 * @param query Note this query will have no conditions but it may have field restrictions and therefore the
	 *              need to have it available in this method.
	 * @param zone The Zone from which the query has been received. Maybe there is a need to know what that is.
	 * 
	 * @return SIFResponseIterator for ALL objects from your system that are managed by this publisher (i.e StudentPersonal)
	 * 
     * @throws ADKException If there is an error with retrieving data.
     * @throws SIFException If the query cannot be dealt with (ie not supported). In this case the following 
     *                      fields must be set:<br />
     *                         Error Category of SIFErrorCategory.REQUEST_RESPONSE and<br />
     *                         An Error Code of SIFErrorCodes.REQRSP_UNSUPPORTED_QUERY_9
	 */
	public abstract SIFResponseIterator getAllSIFObjects(Query query, Zone zone) throws ADKException, SIFException;

	/**
	 * This method is called if the SIF Query has conditions for the Primary Key of the object this publisher
	 * deals with. Since this method is called for an object based on its primary key it only returns one
	 * SIF Object instead of a list. The query is given as a parameter but there is no need to inspect the 
	 * condition because this is done for you and the key values are given by the 'keyValues' parameter. The
	 * query object is only required to determine the field restrictions. The 'keyValues' are given in xpath
	 * notation.<p><p>
	 * 
	 * Example:<p>
	 * keyValues[0].xpathToKey=@RefId, keyValues[0].keyValue=D3E34B359D75101A8C3D00AA001A1652<p><p>
	 * 
	 * Note:<p>
	 * Some objects such as the StudentContactRelationship are made up of a compound key and therefore the 
	 * example above could look like this:
	 * keyValues[0].xpathToKey=@StudentPersonalRefId, keyValues[0].keyValue=02834EA9EDA12090347F83297E1C290D<p>
	 * keyValues[1].xpathToKey=@StudentContactPersonalRefId, keyValues[1].keyValue=6472B2610947583A463DBB345291B001<p><p>
	 * 
	 * @param keyValues The key names (xpath notation) and values of this objects primary key.
	 * @param query Query condition and Field restrictions.
	 * @param zone The Zone from which the query has been received. Maybe there is a need to know what that is.
	 * 
	 * @return The SIF Object that matches the primary key. Null if no such object exists.
	 * 
     * @throws ADKException If there is an error with retrieving data.
     * @throws SIFException If the query cannot be dealt with (ie not supported). In this case the following 
     *                      fields must be set:<br />
     *                         Error Category of SIFErrorCategory.REQUEST_RESPONSE and<br />
     *                         An Error Code of SIFErrorCodes.REQRSP_UNSUPPORTED_QUERY_9
	 */
	public abstract SIFDataObject getSIFObjectByPrimaryKey(List<SIFObjectKey> keyValues, Query query, Zone zone) throws ADKException, SIFException;

	/*
	 * Default constructor
	 */
    public SBPBasePublisher(String publisherID, ElementDef dtd)
	{
        super(publisherID);
		setDtd(dtd);		
	}

    /**
     * This is the default implementation for all SIF Queries that are not based on either the unbounded query
     * or primary key query. By default it returns a Query Not Supported message to the ZIS but it is expected
     * if this publisher can deal with any other type of query that this method is overridden.
     * 
     * @param query The query.
     * @param zone The zone from which the qury has been received
     * 
	 * @return SIFResponseIterator returning SIF Objects that match the given query.
     * 
     * @throws ADKException If there is an error with retrieving data.
     * @throws SIFException Default implementation of this method
     *                      If the query cannot be dealt with (ie not supported). In this case the following 
     *                      fields must be set:<br />
     *                         Error Category of SIFErrorCategory.REQUEST_RESPONSE and<br />
     *                         An Error Code of SIFErrorCodes.REQRSP_UNSUPPORTED_QUERY_9
     */
    public SIFResponseIterator getObjectsByGenericQuery(Query query, Zone zone) throws ADKException, SIFException
    {
    	throw new SIFException(SIFErrorCategory.REQUEST_RESPONSE, SIFErrorCodes.REQRSP_UNSUPPORTED_QUERY_9, "Not supported Query.", zone);
    }
    
    /*
     * This method interrogates the query and calls appropriate methods that are specific to the query. In the
     * SBP there are two types of queries each publisher must respond to. They are:<p><p>
     * 
     * - Unbounded  : No Query Condition => Return all objects<p>
     * - Primary Key: Query Condition is only made up of primary key elements and their values => Return One Object
     * 
     * For all other queries the default implementation will return a Query Not Supported error to the ZIS. Of
     * course the method for all other queries can be overridden to cater for custom behaviour.
     * 
     * @see systemic.sif.sifcommon.publisher.BasePublisher#getRequestedSIFObjects(openadk.library.Query, openadk.library.Zone)
     */
	@Override
    public SIFResponseIterator getRequestedSIFObjects(Query query, Zone zone) throws ADKException, SIFException
    {
		if (query != null)
		{
			if (query.hasConditions())
			{
				List<SIFObjectKey> keyValues = extractPrimaryKey(query);
				if (keyValues != null) //primary key found and valid
				{
					return new SingleObjectIterator(getSIFObjectByPrimaryKey(keyValues, query, zone));
				}
				else
				{
					return getObjectsByGenericQuery(query, zone);
				}
			}
			else // No conditions => Unbound Query
			{
				return getAllSIFObjects(query, zone);
			}
		}
		else // no query. Something is wrong!
		{
			return null; 
		}
    }

	/*---------------------*/
	/*-- Private Methods --*/
	/*---------------------*/
	
	/*
	 * Returns list of key names and values as defined in the metadata store for this object if the condition
	 * of the query matches the primary key of the object. The condition matches if it is:
	 * - Made up of the number of conditions equal to the number of elements that make up the primary key
	 * - The comparison operator is EQ for all conditions.
	 * If any of the above checks fails then null is returned indicating that the query conditions is not based
	 * on the primary key for that object.
	 */
	private List<SIFObjectKey> extractPrimaryKey(Query query)
	{
		SIFObject sifObj = metadataCache.getObjectMetadata(getDtd().name());
		if (sifObj == null) // We have a problem. This object is not known to the metadata store
		{
			return null;
		}
		
		if (query.getConditions().length > 1) // complex query. Not primary key query!
		{
			return null;
		}
		
		// There is only one condition group.
		ConditionGroup conditions = (query.getConditions())[0];
		
		// If we get here the object is known to the metadata cache. Let's get the key info
		List<SIFObjectKey> keys = sifObj.getOrderedKeyList();
		if (keys.size() == conditions.size()) // number of conditions match number of keys
		{
			// Check if all conditions are of the primary key fields and if they are of type EQ
			for (SIFObjectKey key : keys)
			{
				Condition condition = conditions.hasCondition(key.getXpath());
				
				if ((conditions != null) && (ComparisonOperators.EQ.equals(condition.getOperator())))
				{
					key.setValue(condition.getValue());
				}
				else // condition or comparison operator don't match
				{
					return null;
				}
			}
			
			// If we get here all has matched and is good => we have primary key condition
			return keys;
		}
		else // number of conditions don't match number of keys
		{
			return null;
		}
	}
}
