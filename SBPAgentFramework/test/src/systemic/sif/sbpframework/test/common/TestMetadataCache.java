/*
 * TestMetadataCache.java
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

package systemic.sif.sbpframework.test.common;

import systemic.sif.sbpframework.common.utils.SIFObjectMetadataCache;

/**
 * @author Joerg Huber
 *
 */
public class TestMetadataCache
{
	
	private void getMedatadataFor(String objectName)
	{
		SIFObjectMetadataCache cache = SIFObjectMetadataCache.getCache();
		if (cache == null)
		{
			System.out.println("Cannot retrieve Cache.");
		}
		else
		{
			System.out.println("Metadata for "+objectName+":\n"+cache.getObjectMetadata(objectName));
		}
	}
	
	private void testDependencies(String objectName)
	{
		SIFObjectMetadataCache cache = SIFObjectMetadataCache.getCache();
		if (cache == null)
		{
			System.out.println("Cannot retrieve Cache.");
		}
		else
		{
			System.out.println("Does "+objectName+" have dependencies: "+cache.hasDependencies(objectName));
		}		
	}
	
	private void displayContent()
	{
		SIFObjectMetadataCache cache = SIFObjectMetadataCache.getCache();
		if (cache == null)
		{
			System.out.println("Cannot retrieve Cache.");
		}
		else
		{
			System.out.println("Complete Cache:\n"+cache.toString());
		}		
	}
	
    public static void main(String[] args)
    {
        try
        {
        	TestMetadataCache tester = new TestMetadataCache();
//        	tester.getMedatadataFor("StudentPersonal");
//        	tester.getMedatadataFor("StaffAssignment");
//        	tester.testDependencies("StaffPersonal");
//        	tester.testDependencies("TimeTableSubject");
//        	tester.testDependencies("StudentContactRelationship");  
        	tester.displayContent();
        	
        	
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
