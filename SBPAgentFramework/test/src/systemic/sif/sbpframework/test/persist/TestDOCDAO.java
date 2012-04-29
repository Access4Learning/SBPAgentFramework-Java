/*
 * TestDOCDAO.java
 * Created: 15/10/2011
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

import systemic.sif.sbpframework.persist.dao.DOCacheDAO;
import systemic.sif.sbpframework.persist.model.DOCObject;
import systemic.sif.sbpframework.persist.model.DOCache;

/**
 * @author Joerg Huber
 *
 */
public class TestDOCDAO extends DAOBaseTest
{
	private DOCacheDAO docCacheDAO = new DOCacheDAO();

	private void testRetrieveCachedObject()
	{
		startTransaction(docCacheDAO);
        DOCache cachedObj = docCacheDAO.retrieveCachedObject(getTransaction(), "StudentPersonal", "7C834EA9EDA12090347F83297E1C290C", "MyApp", "TestZone", false);
		commit();
		System.out.println("Cached Object = "+cachedObj);
	}
	
	private void testRequestedObject()
	{
		startTransaction(docCacheDAO);
		DOCObject objectToTest = new DOCObject();
		objectToTest.setSifObjectName("StudentPersonal");
		objectToTest.setObjectKeyValue("02834EA9EDA12090347F83297E1C290D");
		objectToTest.setApplicationId("MyApp");
		objectToTest.setZoneId("TestZone");
		
		DOCObject cachedObj = docCacheDAO.getCachedDependentObject(getTransaction(), objectToTest);
		commit();
		System.out.println("objectToTest:\n"+cachedObj);
	}
	
	private void testRemoveDependency()
	{
		startTransaction(docCacheDAO);
		DOCObject objectToTest = new DOCObject();
		objectToTest.setSifObjectName("StudentPersonal");
		objectToTest.setObjectKeyValue("02834EA9EDA12090347F83297E1C290D");
		objectToTest.setApplicationId("MyApp");
		objectToTest.setZoneId("TestZone");
		
		docCacheDAO.removeDependency(getTransaction(), objectToTest);
		commit();
		System.out.println("Dependencies Removed");
	}
	
	
    public static void main(String[] args)
    {
        try
        {
        	TestDOCDAO tester = new TestDOCDAO();
        	//tester.testRetrieveCachedObject();
        	//tester.testRequestedObject();
        	tester.testRemoveDependency();
        	tester.shutdown();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
