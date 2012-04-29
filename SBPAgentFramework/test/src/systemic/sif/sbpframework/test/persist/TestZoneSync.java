/*
 * TestZoneSync.java
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

package systemic.sif.sbpframework.test.persist;

import java.util.Date;

import systemic.sif.sbpframework.persist.dao.ObjectSyncInfoDAO;
import systemic.sif.sbpframework.persist.model.ObjectZoneSync;


/**
 * @author Joerg Huber
 *
 */
public class TestZoneSync extends DAOBaseTest
{
	private ObjectSyncInfoDAO dao = new ObjectSyncInfoDAO();
	
	private ObjectZoneSync insert(ObjectZoneSync row)
	{
		if (row == null)
		{
			row = new ObjectZoneSync("StudentPersonal", "SIFDemo", "SubscribingAgent");
		}
		row.setLastRequested(new Date());
		
		startTransaction(dao);

		dao.save(getTransaction(), row);
		
		commit();
		System.out.println("row = "+row);

		return row;
	}
	
	private ObjectZoneSync getInfo()
	{
		startTransaction(dao);

		ObjectZoneSync row = dao.retrieve(getTransaction(), "StudentPersonal", "SubscribingAgent", "SIFDemo");
		
		commit();
		System.out.println("row = "+row);

		return row;
	}	
	
    public static void main(String[] args)
    {
        try
        {
        	TestZoneSync tester = new TestZoneSync();
        	ObjectZoneSync row = tester.getInfo();
        	tester.insert(row);
        	
        	tester.shutdown();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
