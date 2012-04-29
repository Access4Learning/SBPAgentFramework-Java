/*
 * SIFSyncService.java
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

package systemic.sif.sbpframework.persist.servcie;

import java.util.Date;

import javax.persistence.PersistenceException;

import systemic.sif.sbpframework.persist.common.BasicTransaction;
import systemic.sif.sbpframework.persist.dao.BaseDAO;
import systemic.sif.sbpframework.persist.dao.ObjectSyncInfoDAO;
import systemic.sif.sbpframework.persist.model.ObjectZoneSync;

/**
 * @author Joerg Huber
 *
 */
public class SIFSyncService extends DBService
{
    private ObjectSyncInfoDAO objectSyncInfoDAO = new ObjectSyncInfoDAO();

   
    @Override
    public BaseDAO getDAO()
    {
	    return objectSyncInfoDAO;
    }

    /**This method will try to retrieve the Sync Info for the given SIF Object and Zone. If it exists if will be
     * updated with the latest date. If id doesn't exist it will insert it.
     * 
     * @param sifObjectName The name of the SIF Object for which the ObjectZoneSync info shall be updated.
     * @param zoneID The Zone ID for which the ObjectZoneSync info shall be updated.
     * 
	 * @throws IllegalArgumentException: sifObjectName or zoneID empty or null.
	 * @throws PersistenceException: There is an issue with the underlying database. An error is logged.
     */
    public void markSIFZoneAsSyncedForObject(String sifObjectName, String agentId, String zoneID) throws IllegalArgumentException, PersistenceException
    {
    	BasicTransaction tx = startTransaction();
		ObjectZoneSync row = objectSyncInfoDAO.retrieve(tx, sifObjectName, agentId, zoneID);

		// doesn't exist => must create object
		if (row == null)
		{
			row = new ObjectZoneSync(sifObjectName, zoneID, agentId);
		}
		row.setLastRequested(new Date());

		objectSyncInfoDAO.save(tx, row);
		
		tx.commit();
    }

    /**
     * This method check if the Sync table holds an entry for the SIF Object for the given Zone. If it doesn't hold
     * an entry then TRUE (sync required) is returned otherwise FALSE (no sync required) is returned.
     * 
     * @param sifObjectName The name of the SIF Object for which the ObjectZoneSync info shall be checked.
     * @param zoneID The Zone ID for which the ObjectZoneSync info shall be checked.
     * 
     * @return See description.
     * 
	 * @throws IllegalArgumentException: sifObjectName or zoneID empty or null.
	 * @throws PersistenceException: There is an issue with the underlying database. An error is logged.
     */
    public boolean requiresSyncForObjectInZone(String sifObjectName, String agentId, String zoneID) throws IllegalArgumentException, PersistenceException
    {
    	BasicTransaction tx = startTransaction();

		ObjectZoneSync row = objectSyncInfoDAO.retrieve(tx, sifObjectName, agentId, zoneID);
		
		tx.commit();

		return row == null;
     }

}
