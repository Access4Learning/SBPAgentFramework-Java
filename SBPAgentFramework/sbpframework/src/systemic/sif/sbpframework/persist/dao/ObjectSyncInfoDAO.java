/*
 * ObjectSyncInfoDAO.java
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

package systemic.sif.sbpframework.persist.dao;

import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

import systemic.sif.sbpframework.persist.model.ObjectZoneSync;
import au.com.systemic.framework.utils.StringUtils;

/**
 * @author Joerg Huber
 *
 */
public class ObjectSyncInfoDAO extends BaseDAO
{
	  
    /**
	 * This method attempts to retrieve the ObjectZoneSync element for the given SIF Object and Zone ID. If it 
	 * does not exist then null is returned.
     * 
     * @param sifObjectName The name of the SIF Object for which the ObjectZoneSync info shall be returned.
     * @param zoneID The Zone ID for which the ObjectZoneSync info shall be returned.
     * 
     * @return The Object for the given SIF Object and Zone ID if it exists or null if it does not exist.
	 * 
	 * @throws IllegalArgumentException: id is null.
	 * @throws PersistenceException: There is an issue with the underlying database. An error is logged.
     */
    @SuppressWarnings("unchecked")
    public ObjectZoneSync retrieve(String sifObjectName, String agentId, String zoneID) throws IllegalArgumentException, PersistenceException
    {
        if (StringUtils.isEmpty(sifObjectName) || StringUtils.isEmpty(zoneID))
        {
            throw new IllegalArgumentException("sifObjectName or zoneID is empty or null.");
        }

        try
        {
            Criteria criteria = getCurrentSession().createCriteria(ObjectZoneSync.class)
               .add(Restrictions.eq("objectName", sifObjectName))
               .add(Restrictions.eq("agentId", agentId))
               .add(Restrictions.eq("zoneId", zoneID));

             List<ObjectZoneSync> zoneObjectList = criteria.list();
            
            // There can only be a maximum of one
            if (zoneObjectList.isEmpty())
            {
                return null;
            }
            else
            {
                return zoneObjectList.get(0);
            }
        }
        catch (HibernateException e)
        {
            throw new PersistenceException("Unable to retrieve ObjectZoneSync for agent = '"+ agentId + "', zone = '" + zoneID + "' and Sif Object = '"+sifObjectName+"'.", e);
        }
    }

    
    /**
     * Saves the given ObjectZoneSync to the DB. If it exists it is updated otherwise it is created.
     * 
     * @param objectZoneSync Object to persist to the DB.
     * 
     * @throws IllegalArgumentException  objectZoneSync parameter is null.
     * @throws PersistenceException      A database error occurred.
     */
    public void save(ObjectZoneSync objectZoneSync) throws IllegalArgumentException, PersistenceException
    {
        if (objectZoneSync == null)
        {
            throw new IllegalArgumentException("objectZoneSync is null.");
        }

        try
        {
        	getCurrentSession().saveOrUpdate(objectZoneSync);
        }
        catch (HibernateException e)
        {
            throw new PersistenceException("Unable to save objectZoneSync " + objectZoneSync + ".", e);
        }    	
    }
}
