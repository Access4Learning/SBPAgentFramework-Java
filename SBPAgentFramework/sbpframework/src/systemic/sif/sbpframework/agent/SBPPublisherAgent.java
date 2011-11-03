/*
 * SBPPublisherAgent.java
 * Created: 02/11/2011
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

import systemic.sif.sifcommon.agent.SIFBaseAgent;

/**
 * This is an actual implementation of a publishing agent for the SBP. In most cases one can use this agent out of the 
 * box. The main restriction it has is that it cannot deal with custom objects. If that should be required one must
 * write a new agent like this and implement the initCustomObjects() method of the SIFBaseAgent class.<p>
 * 
 * To start this agent the following command line statement is used:<p>
 * 
 * <code>
 * &lt;JAVA_HOME&gt;/bin/java &lt;JVM_SETTINGS&gt; -cp &lt;classpath&gt; systemic.sif.sbpframework.agent.SBPPublisherAgent  &lt;agentID&gt; [ &lt;agent.properties&gt;]
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
public class SBPPublisherAgent extends SIFBaseAgent
{
	private static void usage(String[] args)
	{
		System.out.println("Usage <JAVA_HOME>/bin/java <JVM_SETTINGS> -cp <classpath> systemic.sif.sbpframework.agent.SBPPublisherAgent <agentID> [<agent.properties>]");
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

	public SBPPublisherAgent(String agentID, String propertyFileName) throws Exception
	{
		super(agentID, propertyFileName);
	}
		
	/**
	 * Default implementation does nothing. If custom objects are required then it is advised to
	 * write a new agent that extends this agent and then override this method to the need of the agent.
	 */
	@Override
    public void initCustomObjects()
    {
//        logger.debug("Load Objects created with ADKGen...");
//        CustomObjectDTD customObjects = new CustomObjectDTD();
//        customObjects.load();
//        customObjects.addElementMappings(SIFDTD.sElementDefs);
//        logger.debug("Objects loaded.");

    }

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
			SBPPublisherAgent agent = null;
			try
			{
				agent = new SBPPublisherAgent(getAgentIDParam(args), getPropertyFileNameParam(args));
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
					System.out.println("Blocking wait in SIFCommonAgent interrupted: "+ex.getMessage());
					ex.printStackTrace();
				}
			}
			catch (Exception ex)
			{
				System.out.println("SIFCommonAgent could not be started. See previous log entries for details.");
				ex.printStackTrace();
			}
			finally
			// If startup is successful then this will never be reached.
			{
				System.out.println("Exit PublishingAgent...");
				if (agent != null)
				{
					agent.stopAgent();
				}
			}
		}
	}		
}
