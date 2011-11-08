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

import systemic.sif.sbpframework.common.utils.DOCacheProperties;


/**
 * @author Joerg Huber
 *
 */
public class TestDOCacheProperties
{
		
    public static void main(String[] args)
    {
        try
        {
        	DOCacheProperties cacheProperties = DOCacheProperties.getDOCacheProperties();
    		int delay = cacheProperties.getExpiryCheckStartupDelayInSec(60);
    		int period = cacheProperties.getExpiryCheckFreqMinutes(60);
            System.out.println("Startup Delay/Frequency for getExpiryCheckStartupDelayInSec/getExpiryCheckFreqMinutes: "+delay+"/"+period);

    		delay = cacheProperties.getRequestStartupDelayInSec(60);
    		period = cacheProperties.getRequestFreqInSec(60);
            System.out.println("Startup Delay/Frequency for getRequestStartupDelayInSec/getRequestFreqInSec for object StudentPersonal: "+delay+"/"+period);

    		delay = cacheProperties.getResolvedStartupDelayInSec(60);
    		period = cacheProperties.getResolvedFreqInSec(60);
            System.out.println("Startup Delay/Frequency for getResolvedStartupDelayInSec/getResolvedFreqInSec for object StudentPersonal: "+delay+"/"+period);

            System.out.println("Ignore Dependency between TeachingGroup and StudentPersonal: "+cacheProperties.getIgnoreDependency("TeachingGroup", "StudentPersonal"));
            System.out.println("Ignore Dependency between TeachingGroup and StaffPersonal: "+cacheProperties.getIgnoreDependency("TeachingGroup", "StaffPersonal"));
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
