/*
 * TestDOCService.java
 * Created: 11/10/2011
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

package systemic.sif.sbpframework.test.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.persistence.PersistenceException;

import openadk.library.ADKException;
import openadk.library.SIFDataObject;
import openadk.library.SIFParser;
import systemic.sif.sbpframework.persist.model.DOCObject;
import systemic.sif.sbpframework.persist.model.DOCache;
import systemic.sif.sbpframework.persist.servcie.DOCService;

/**
 * @author Joerg Huber
 *
 */
public class TestDOCService extends ServiceBaseTest
{
	private static final String TEST_DATA_DIR = "C:/Development/GitHubRepositories/SBPAgentFramework/SBPAgentFramework/testData/input/";
	private static final String ZONE_ID = "SIFDemo";
	private static final String APP_ID = "MyApp";
	private static final String AGENT_ID = "SubscribingAgent";

	private DOCService service = new DOCService();
	
	public TestDOCService() throws ADKException
	{
		initADK();
	}

	private SIFDataObject getSIFObjectFromFile(String filename) throws ADKException, IOException
	{
		SIFDataObject sifObj = null;
		String sifXML = loadXMLFileData(TEST_DATA_DIR+filename);
		if (sifXML != null)
		{
			SIFParser parser = SIFParser.newInstance();
			sifObj = (SIFDataObject)parser.parse(sifXML);
		}
		return sifObj;
	}
	
	private void getDependendObjectList(SIFDataObject sifObj) throws Exception
	{
		List<DOCObject> dependencyList = null;
		
		if (sifObj != null)
		{
			dependencyList = service.extractDependentObjectsFromSIFObject(sifObj);
			if (dependencyList == null)
			{
				System.out.println("No dependencies found for Object: "+sifObj.getElementDef().name());
			}
			else
			{
				System.out.println("Found the following dependencies for Object: "+sifObj.getElementDef().name()+"\n");
				for (DOCObject depObj : dependencyList)
				{
					System.out.println(depObj+"\n\n");
				}
			}
		}		
	}
	
	public void testDependencies(String filename) throws Exception
	{
    	SIFDataObject sifObj = getSIFObjectFromFile(filename);
    	
    	if (sifObj == null)
    	{
    		System.out.println("Failed do load SIF Object from File and convert it to a SIF Object. See previous errors.");
    	}
    	else
    	{
    		getDependendObjectList(sifObj);
    	}
	}
	
	private void testRetrieve(String filename) throws Exception
	{
		SIFDataObject sifObj = getSIFObjectFromFile(filename);
		DOCache cachedObject = service.retrieveCachedObject(sifObj, APP_ID, ZONE_ID, true);
		System.out.println("Cached Object:\n"+cachedObject);
	}
	
	private void testExtractFlatKey(String filename) throws Exception
	{
		SIFDataObject sifObj = getSIFObjectFromFile(filename);
		String flatKey = service.extractFlatKey(sifObj);
		System.out.println("Flat Key: "+flatKey);
	}
	
	/*
	 * Simply saves the given object with its dependency list as given in the parameters. No checks are performed.
	 */
	private void cacheObject(SIFDataObject sifObj, List<DOCObject> dependencyList) throws Exception
	{
		String flatKey = service.extractFlatKey(sifObj);	
		DOCache cachedObject = new DOCache();
		cachedObject.setSifObjectName(sifObj.getElementDef().name());
		cachedObject.setIsEvent(Boolean.FALSE);
		cachedObject.setObjectKeyValue(flatKey);
		cachedObject.setObjectXML(sifObj.toXML());
		cachedObject.setDependentObjects(new HashSet<DOCObject>(dependencyList));
		service.cacheObject(cachedObject, AGENT_ID, APP_ID, ZONE_ID);
	}
	
	/*
	 * Test the basic extractDependentObjectsFromSIFObject and saves the object
	 */
	private void testCacheObject(String filename) throws Exception
	{
		SIFDataObject sifObj = getSIFObjectFromFile(filename);
		String flatKey = service.extractFlatKey(sifObj);
		
		DOCache cachedObject = service.retrieveCachedObject(sifObj.getElementDef().name(), flatKey, APP_ID, ZONE_ID, false);
		if (cachedObject == null) // doesn't exist yet
		{
			List<DOCObject> dependencyList = service.extractDependentObjectsFromSIFObject(sifObj);
			System.out.println("Found dependencies:\n"+dependencyList);
			cacheObject(sifObj, dependencyList);
		}
		else
		{
			System.out.println(sifObj.getElementDef().name()+" object with flat key = '"+ flatKey +"' is already cached.");
		}
	}

	/*
	 * Test the markAreadyRequestedObjects method but doesn't save the object.
	 */
	private void testMarkAreadyRequestedObjects(String filename) throws Exception
	{
		SIFDataObject sifObj = getSIFObjectFromFile(filename);
		List<DOCObject> dependencyList = service.extractDependentObjectsFromSIFObject(sifObj);
		System.out.println("Initial Dependency List:\n"+dependencyList);
		
		service.mergeWithCachedDependencies(dependencyList, APP_ID, ZONE_ID);
		System.out.println("Marked Dependency List:\n"+dependencyList);
	}

	/*
	 * Test  the getDependeniesFromSIFObjectAndCache method and saves the resulting object.
	 */
	private void testGetDependenciesFromSIFObjectAndCache(String filename) throws Exception
	{
		SIFDataObject sifObj = getSIFObjectFromFile(filename);
		
		String flatKey = service.extractFlatKey(sifObj);
		
		DOCache cachedObject = service.retrieveCachedObject(sifObj.getElementDef().name(), flatKey, APP_ID, ZONE_ID, false);
		if (cachedObject == null) // doesn't exist yet
		{
			List<DOCObject> dependencyList = service.getDependenciesFromSIFObjectAndCache(sifObj, APP_ID, ZONE_ID);
			System.out.println("Dependency List:\n"+dependencyList);
			cacheObject(sifObj, dependencyList);
		}
		else
		{
			System.out.println(sifObj.getElementDef().name()+" object with flat key = '"+ flatKey +"' is already cached.");
		}
	}

	public void testCheckAndRemoveDependency(String filename) throws Exception
	{
		SIFDataObject sifObj = getSIFObjectFromFile(filename);
		service.checkAndRemoveDependency(sifObj, APP_ID, ZONE_ID);
		System.out.println("Dependencies removed.");
	}
	
	public void testGetNotYetRequestedObjects() throws Exception
	{
		List<DOCObject> objectsToRequest = service.getNotYetRequestedObjects("StudentContactPersonal", APP_ID, ZONE_ID);
		System.out.println("List of not requested object:\n"+objectsToRequest);
	}
	
	
    public void testGetObjectsWithoutDependencies()
    {
    	List<DOCache> objects = service.getObjectsWithoutDependencies("Identity", APP_ID, AGENT_ID);
    	System.out.println("List of Objects without Dependencies:");
    	for (DOCache obj : objects) 
    	{
        	System.out.println("Object Name/Key/NumDep: "+obj.getSifObjectName()+"/"+obj.getObjectKeyValue()+"/"+obj.getRemainingDependencies());
    	}
    }
    
    public void testRemoveObjectsWithoutDependencies()
    {
    	List<DOCache> objects = service.getObjectsWithoutDependencies("Identity", APP_ID, AGENT_ID);
    	System.out.println("List of Objects without Dependencies:");
    	for (DOCache obj : objects) 
    	{
        	System.out.println("Remove Object Name/Key/NumDep: "+obj.getSifObjectName()+"/"+obj.getObjectKeyValue()+"/"+obj.getRemainingDependencies());
        	service.removeCachedObject(obj);
        	System.out.println("Object Removed.");
    	}
    }
    	
    public void testGetExpiredObjects()
    {
    	List<DOCache> objects = service.getExpiredObjects(APP_ID, AGENT_ID);
		System.out.println("List of Expired Objects:");
    	for (DOCache obj : objects) 
    	{
        	System.out.println("Object Name/Key/ExpDate: "+obj.getSifObjectName()+"/"+obj.getObjectKeyValue()+"/"+obj.getExpiryDate());
    	}
    }
    
    public void testRemoveCachedObject()
    {
    	DOCache object = service.retrieveCachedObject("StudentContactRelationship", "02834EA9EDA12090347F83297E1C290D|6472B2610947583A463DBB345291B001", APP_ID, ZONE_ID, false);
    	service.removeCachedObject(object);
    	System.out.println("Objects Removed.");
    }
    
    public void testUpdateExpiredObjects()
    {
    	service.updateExpiredObjects(APP_ID, AGENT_ID);
    	System.out.println("Expired Objects Updated.");
    }
    
    public static void main(String[] args)
    {
		System.out.println("================================== Start TestDOCService ===============================");
		try
        {
        	TestDOCService tester = new TestDOCService();
//        	tester.testDependencies("SingleStudentPersonal.xml");
//        	tester.testDependencies("SingleStudentSchoolEnrollment.xml");
//        	tester.testDependencies("SingleTeachingGroup.xml");
//        	tester.testDependencies("IdentityStaffPersonal.xml");
//        	tester.testRetrieve("SingleTeachingGroup.xml");      	
//        	tester.testRetrieve("IdentityStaffPersonal.xml");      	
//        	tester.testExtractFlatKey("SingleStudentRelationship.xml");
//        	tester.testExtractFlatKey("IdentityStaffPersonal.xml");
//        	tester.testDependencies("SingleStudentRelationship.xml");
//        	tester.testCacheObject("SingleStudentRelationship.xml");
//        	tester.testCacheObject("IdentityStaffPersonal.xml");
//        	tester.testCacheObject("SingleTeachingGroup.xml");
//        	tester.testMarkAreadyRequestedObjects("SingleStudentRelationship.xml");
//        	tester.testGetDependenciesFromSIFObjectAndCache("SingleTeachingGroup.xml");
//        	tester.testGetDependenciesFromSIFObjectAndCache("SingleStudentRelationship.xml");
//        	tester.testCheckAndRemoveDependency("SingleStudentContact.xml");
//        	tester.testCheckAndRemoveDependency("SingleStudentPersonal.xml");
//        	tester.testCheckAndRemoveDependency("StaffPersonal.xml");
//        	tester.testGetNotYetRequestedObjects();
//        	tester.testGetObjectsWithoutDependencies();
//        	tester.testGetExpiredObjects();
//        	tester.testRemoveCachedObject();
//        	tester.testUpdateExpiredObjects();
        	tester.testRemoveObjectsWithoutDependencies();
       
        	tester.shutdown();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		System.out.println("================================== End TestDOCService ===============================");
    }
}
