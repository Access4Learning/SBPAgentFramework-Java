/*
 * SIFObjectMetadataService.java
 * Created: 10/10/2011
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

import java.util.List;

import javax.persistence.PersistenceException;

import systemic.sif.sbpframework.persist.dao.BaseDAO;
import systemic.sif.sbpframework.persist.dao.SIFObjectMetadataDAO;
import systemic.sif.sbpframework.persist.model.SIFObject;

/**
 * @author Joerg Huber
 *
 */
public class SIFObjectMetadataService extends DBService
{
    private SIFObjectMetadataDAO metadataDAO = new SIFObjectMetadataDAO();

    @Override
    public BaseDAO getDAO()
    {
	    return metadataDAO;
    }


	/**
	 * This method returns all Data related to all SIF Objects configured in the SCF_OBJECT and related tables. This
	 * includes information about keys, depended objects and the keys in those objects but no data in relation to
	 * cached objects.
	 * 
	 * @return See description
	 * 	 
	 * @throws PersistenceException: There is an issue with the underlying database. An error is logged.

	 */
    public List<SIFObject> getAllSIFObjectMetadata() throws PersistenceException
    {
    	List<SIFObject> metadata = null; 	
    	
		startTransaction();
    	//startNewTransactionScope();
		metadata = metadataDAO.getAllSIFObjectData();
		commit();   
		
		return metadata;
    }
    
}
