/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
///////////////////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) 2001, HP Bluestone Arjuna.
//
// File        : Client13.javatmpl (AITResources02)
//
// Description : Memory Test version of Client11 (ping pong test with no client transaction).
//
//               Client performs a specified number of remote calls before
//               the memory growth is checked. If client or server memory growth
//               exceeds specified parameters then the test fails and "Failed" is output.
//               Otherwise "Passed" is output.
//
// Author      : Stewart Wheater
//
// History     : 1.0   25 Feb 2000  S Wheater       Creation.
//               1.1   07 Jul 2001  M Buckingham    Added facility to use client/server
//                                                  thresholds in config file
//                                                  MemoryTestProfile.
//
///////////////////////////////////////////////////////////////////////////////////////////

package org.jboss.jbossts.qa.AITResources02Clients;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client13.java,v 1.2 2003/06/26 11:43:11 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client13.java,v 1.2 2003/06/26 11:43:11 rbegg Exp $
 */


import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.Utils.*;

public class Client13
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String pingerIOR = ServerIORStore.loadIOR(args[args.length - 5]);
			PingPong pinger = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pingerIOR));

			String pongerIOR = ServerIORStore.loadIOR(args[args.length - 4]);
			PingPong ponger = PingPongHelper.narrow(ORBInterface.orb().string_to_object(pongerIOR));

			int numberOfCalls = Integer.parseInt(args[args.length - 3]);

			float clientIncreaseThreshold;
			float serverIncreaseThreshold;

			// If no threshold value then use default.
			if (MemoryTestProfileStore.getNoThresholdValue().equals(args[args.length - 2]))
			{
				clientIncreaseThreshold = Float.parseFloat(MemoryTestProfileStore.getDefaultClientIncreaseThreshold());
			}
			else // Use passed threshold
			{
				clientIncreaseThreshold = Float.parseFloat(args[args.length - 2]);
			}

			// If no threshold value then use default.
			if (MemoryTestProfileStore.getNoThresholdValue().equals(args[args.length - 1]))
			{
				serverIncreaseThreshold = Float.parseFloat(MemoryTestProfileStore.getDefaultServerIncreaseThreshold());
			}
			else // Use passed threshold
			{
				serverIncreaseThreshold = Float.parseFloat(args[args.length - 1]);
			}

			pinger.bad_hit(numberOfCalls, 0, pinger, pinger, null);
			ponger.bad_hit(numberOfCalls, 0, ponger, ponger, null);

			int clientMemory0 = (int) JVMStats.getMemory();
			int server1Memory0 = pinger.getMemory();
			int server2Memory0 = ponger.getMemory();

			for (int index0 = 0; index0 < numberOfCalls; index0++)
			{
				for (int index1 = 0; index1 <= index0; index1++)
				{
					pinger.bad_hit(index0, index1, ponger, pinger, null);
				}
			}

			int clientMemory1 = (int) JVMStats.getMemory();
			int server1Memory1 = pinger.getMemory();
			int server2Memory1 = ponger.getMemory();

			float clientMemoryIncrease = ((float) (clientMemory1 - clientMemory0)) / ((float) clientMemory0);
			float server1MemoryIncrease = ((float) (server1Memory1 - server1Memory0)) / ((float) server1Memory0);
			float server2MemoryIncrease = ((float) (server2Memory1 - server2Memory0)) / ((float) server2Memory0);

			System.err.println("Client memory increase threshold : " + (float) (100.0 * clientIncreaseThreshold) + "%");
			System.err.println("Server memory increase threshold : " + (float) (100.0 * serverIncreaseThreshold) + "%");

			System.err.println("Client   percentage memory increase: " + (float) (100.0 * clientMemoryIncrease) + "%");
			System.err.println("Client   memory increase per call  : " + (clientMemory1 - clientMemory0) / numberOfCalls);
			System.err.println("Server 1 percentage memory increase: " + (float) (100.0 * server1MemoryIncrease) + "%");
			System.err.println("Server 1 memory increase per call  : " + (server1Memory1 - server1Memory0) / numberOfCalls);
			System.err.println("Server 2 percentage memory increase: " + (float) (100.0 * server2MemoryIncrease) + "%");
			System.err.println("Server 2 memory increase per call  : " + (server2Memory1 - server2Memory0) / numberOfCalls);

			if ((clientMemoryIncrease < clientIncreaseThreshold) && (server1MemoryIncrease < serverIncreaseThreshold) && (server2MemoryIncrease < serverIncreaseThreshold))
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Failed");
			}
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client13.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client13.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
