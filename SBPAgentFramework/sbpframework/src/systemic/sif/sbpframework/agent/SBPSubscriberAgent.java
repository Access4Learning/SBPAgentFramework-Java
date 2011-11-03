/*
 * SBPSubscriberAgent.java
 * Created: 19/10/2011
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

package systemic.sif.sbpframework.agent;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.PersistenceException;

import systemic.sif.sbpframework.common.utils.DOCacheProperties;
import systemic.sif.sbpframework.common.utils.SIFObjectMetadataCache;
import systemic.sif.sbpframework.persist.common.HibernateUtil;
import systemic.sif.sbpframework.persist.servcie.DOCService;
import systemic.sif.sifcommon.agent.SIFBaseAgent;
import au.com.systemic.framework.utils.StringUtils;

/**
 * This is an actual implementation of a subscribing agent for the SBP. In most cases one can use this agent out of the 
 * box. The main restriction it has is that it cannot deal with custom objects. If that should be required one must
 * write a new agent like this and implement the initCustomObjects() method of the SIFBaseAgent class.<p>
 * 
 * To start this agent the following command line statement is used:<p>
 * 
 * <code>
 * &lt;JAVA_HOME&gt;/bin/java &lt;JVM_SETTINGS&gt; -cp &lt;classpath&gt; systemic.sif.sbpframework.agent.SBPSubscriberAgent  &lt;agentID&gt; [ &lt;agent.properties&gt;]
 * <br><br>
 * 
 * &lt;agentID&gt;: Required. Must be an ID of an agent used in the &lt;agen&gt;.properties file.<br>
 * &lt;agent.properties&gt;: Optional. The name of the agent properties file. If not provided it is 
 *                           assumed to be called SIFAgent.properties. The directory of this file must be on 
 *                           the classpath.
 * </code>
 * 
 * @author Joerg Huber
 *
 */
public class SBPSubscriberAgent extends SIFBaseAgent
{
	private static final int MILISEC = 1000;
	private static final String BANNER = "\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";

	private static DOCacheProperties cacheProperties = null;
	
	private static void usage(String[] args)
	{
		System.out.println("Usage <JAVA_HOME>/bin/java <JVM_SETTINGS> -cp <classpath> systemic.sif.sbpframework.agent.SBPSubscriberAgent <agentID> [<agent.properties>]");
		System.out.println("   <agentID>         : Required. Must be an ID of an agent used in the <agent>.properties file.");
		System.out.println("   <agent.properties>: Optional. The name of the agent properties file. If not provided it is assumed to be called SIFAgent.properties. This directory of this file must be on the classpath.");

		System.out.println("\nProvided Values:");
		for (int i = 0; i<args.length; i++)
		{
			System.out.println("Argument["+i+"]: "+args[i]);
		}
	}
	
	private static String getAgentIDParam(String[] args)
	{
		return args[0].trim();
	}

	private static String getPropertyFileNameParam(String[] args)
	{
		if (args.length>=2)
		{
			return args[1].trim();
		}
		else
		{
			return "SIFAgent";
		}
	}

	public String getApplicationID()
	{
		return getFrameworkProperties().getApplicationID(getAgentID(), null);
	}
	
	public SBPSubscriberAgent(String agentID, String propertyFileName) throws Exception
	{
		super(agentID, propertyFileName);
	}
		
	/**
	 * Default implementation does nothing. If custom objects are required then it is advised to
	 * write a new agent that extends this agent and then override this method to the need of the agent.
	 */
	@Override
    public void initCustomObjects(){}
	
	/**
	 * Override the base agent's shutdown method. This is mainly required to free up resources used by the
	 * SBP Agent such as Hibernate etc. Ensure that the super().shutdown is called first.
	 */
	@Override
	public void stopAgent()
	{
		super.stopAgent();
		HibernateUtil.shutdown();
	}
	
	/**
	 * Overrides the default start up method. Before this happens we want to ensure that the environment
	 * for the SBP Agent is ready. At this stage this means we want to load the Object Metadata Cache before
	 * we even attempt to do anything else. Most SBP Operations and behaviours rely on this cache. If it is not
	 * available then the startup of this agent should not continue (i.e should fail).
	 */
	@Override
    public void startAgent() throws Exception
    {
		// Try to read the DOCache.properties file. If that fails then stop the agent.
		cacheProperties = DOCacheProperties.getDOCacheProperties();
		if (cacheProperties == null)
		{
			throw new IllegalArgumentException("Cannot load DOCacheProperties from file for agent "+getAgentID()+". Agent cannot start.");						
		}	
		
		SIFObjectMetadataCache metadataCache = SIFObjectMetadataCache.getCache();
		if (metadataCache == null)
		{
			throw new PersistenceException("Cannot Initialise the SBP Metadata Cache. Agent cannot start.");
		}
		
		// Test that this agent has an application ID assigned otherwise the SBP and its cache won't work.
		if (StringUtils.isEmpty(getApplicationID()))
		{
			throw new IllegalArgumentException("No Application is defined for agent "+getAgentID()+". Set application id in SIFAgent.properties file. Agent cannot start.");			
		}
		
		startupExpiredObjectManager();
		
		// If we get here then the metadata cache is initialised successfully and we can continue with the standard
		// startup procedure.
		super.startAgent();
    }

	/**
	 * This method schedules the Expired Cache Object Cleanup task to be run at given intervals.
	 */
	public void startupExpiredObjectManager()
	{
		int delay = cacheProperties.getExpiryCheckStartupDelayInSec(60) * MILISEC;   // delay for 30 sec.
		int period = cacheProperties.getExpiryCheckFreqMinutes(60) * 60 * MILISEC;  // repeat every hour (60sec*60min = 3600 sec)).
        logger.info(BANNER+getClass().getSimpleName()+".startupExpiredObjectManager() for agent = '" + getAgentID() + "'. Startup Delay/Frequency in Millisec: "+delay+"/"+period+BANNER);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(
			new TimerTask() 
			{
				public void run() 
				{
					cleanUpExpiredObjects();
				}
			}, delay, period);
	}
	
	/*--------------------------------------*/
	/*-- Private methods to run in a Task --*/
	/*--------------------------------------*/
	private void cleanUpExpiredObjects()
	{
		try
		{
			logger.debug(BANNER+getAgentID()+" attempts to update/remove expired object in DOC: "+new Date()+BANNER);
			DOCService service = new DOCService();
			service.updateExpiredObjects(getApplicationID(), getAgentID());
		}
		catch (Exception ex)
		{
			logger.error("Failed to update/remove expired cached objects. See prvious error log entry for details.", ex);
		}
	}

	/*---------------------*/
	/*-- Main Executable --*/
	/*---------------------*/
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("ERROR Starting Agent: Agent ID missing.");

			usage(args);
			System.exit(0);
		}
		else
		{
			SBPSubscriberAgent agent = null;
			try
			{
				agent = new SBPSubscriberAgent(getAgentIDParam(args), getPropertyFileNameParam(args));
				agent.startAgent(); // this will block until CTRL-C is pressed.
				// Put this agent to a blocking wait.....
				try
				{
					Object semaphore = new Object();
		            synchronized (semaphore)
		            {
		                semaphore.wait();
		            }
				}
				catch (Exception ex)
				{
					System.out.println("Blocking wait in SBPSubscriberAgent interrupted: "+ex.getMessage());
					ex.printStackTrace();
				}
			}
			catch (Exception ex)
			{
				System.out.println("ERROR Starting Agent: SBPSubscriberAgent could not be started. See previous output for details.");
				ex.printStackTrace();
			}
			finally
			// If startup is successful then this will never be reached.
			{
				System.out.println("Exit SBPSubscriberAgent...");
				if (agent != null)
				{
					agent.stopAgent();
				}
			}
		}
	}		
}
