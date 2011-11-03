/*
 * TestSIFMetadataService.java
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

package systemic.sif.sbpframework.test.service;

import systemic.sif.sbpframework.persist.servcie.SIFObjectMetadataService;

/**
 * @author Joerg Huber
 *
 */
public class TestSIFMetadataService extends ServiceBaseTest
{
	private SIFObjectMetadataService service = new SIFObjectMetadataService();
	
	private void getAllSIFObjectMetadata()
	{
		System.out.println("Metadata for All Sif Objects:\n "+service.getAllSIFObjectMetadata());
	}

    public static void main(String[] args)
    {
        try
        {
        	TestSIFMetadataService tester = new TestSIFMetadataService();
        	tester.getAllSIFObjectMetadata();
        	
        	tester.shutdown();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
