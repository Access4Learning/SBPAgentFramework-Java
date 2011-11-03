/*
 * TestSIFSyncService.java
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

package systemic.sif.sbpframework.test.service;

import systemic.sif.sbpframework.persist.servcie.SIFSyncService;

/**
 * @author Joerg Huber
 *
 */
public class TestSIFSyncService extends ServiceBaseTest
{
	private SIFSyncService service = new SIFSyncService();
	
	private boolean requireSIFSync()
	{
		boolean syncRequired = service.requiresSyncForObjectInZone("StudentPersonal", "SubscriberAgent", "SIFDemo");
		System.out.println("Requrie Sync for StudentPersonal/SIFDemo: "+syncRequired);
		
		return syncRequired;
	}

	private void markAsSynced()
	{
		service.markSIFZoneAsSyncedForObject("StudentPersonal", "SubscriberAgent", "SIFDemo");
	}

    public static void main(String[] args)
    {
        try
        {
        	TestSIFSyncService tester = new TestSIFSyncService();
         	if (tester.requireSIFSync())
         	{
         		tester.markAsSynced();
         	}
        	
        	tester.shutdown();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
