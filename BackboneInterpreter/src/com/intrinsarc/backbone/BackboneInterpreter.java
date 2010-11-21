/*******************************************************************************
 * Copyright 2005-2010, Andrew McVeigh.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.intrinsarc.backbone;

import java.io.*;
import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.nodes.simple.*;
import com.intrinsarc.backbone.nodes.simple.internal.*;
import com.intrinsarc.backbone.readers.*;
import com.intrinsarc.backbone.runtime.api.*;
import com.intrinsarc.deltaengine.base.*;
import com.intrinsarc.deltaengine.errorchecking.*;

public class BackboneInterpreter
{
	private static final String ARROW = "      |";
	
	public static void main(String args[])
	{
		System.out.println("Backbone v2.0");
		
		// ensure we have enough arguments
		if (args.length < 4)
		{
			System.err.println("Usage: backbone [-nocheck] load-list-file stratum component port");
			System.exit(-1);
		}		
		run(args);
	}

	private static void run(String[] args)
	{
		boolean nocheck = args[0].equals("-nocheck");
		int offset = nocheck ? 1 : 0;
		String loadListFile = args[0 + offset];
		String stratum = args[1 + offset];
		String component = args[2 + offset];
		// port may be null
		String port = args[3 + offset];
		if (port.equals("-none-"))
			port = null;
		args = truncateInterpreterArgs(args, offset);
		new BackboneInterpreter().runBackbone(loadListFile, stratum, component, port, nocheck, args, null);		
	}

	public void runBackbone(
			String loadListFile,
			String stratum,
			String component,
			String port,
			boolean noCheck,
			String[] args,
			IRuntimeCallback beforeConnections)
	{
		boolean displayTreeOnly = false;
		String tab = ARROW + "  ";
		System.out.println(tab + "loading system from " + loadListFile);
		
		try
		{
			List<BBStratum> system = loadSystem(new File(loadListFile), tab);
			
			BBStratum root = GlobalNodeRegistry.registry.getRoot();

			// perform the error check
			if (noCheck)
				System.out.println("Skipping checking phase");
			else
			{
				ErrorRegister errors = new ErrorRegister();
				StratumErrorDetector detector = new StratumErrorDetector(errors);
				system.add(root);
				
				int size = system.size();
	    	final int totalPermutations = detector.calculatePerspectivePermutations(system, -1, true);
	    	int extraSize = size > 1 ? size - 2 : 0;
				System.out.println(tab + "total strata and combinations to check is " + totalPermutations + " + " + (extraSize + 1) + " at home");
	    	long start = System.currentTimeMillis();
	    	
	    	// check all at home
	    	if (size > 1)
	    		detector.checkAllAtHome(system, 0, extraSize, null);
	      
	    	// now check at the top level perspective
	      detector.checkAllInOrder(system, -1, true, null);
	      
				long end = System.currentTimeMillis();
				if (errors.countErrors() > 0)
				{
					System.out.println(tab + "found " + errors.countErrors() + " errors in " + (end - start) + "ms");
					for (ErrorLocation loc : errors.getAllErrors().keySet())
					{
						for (ErrorDescription desc : errors.getAllErrors().get(loc))
							System.err.println("Error: " + loc + " | " + desc + " at " + loc.getFirstUuid() + " |");
					}
					System.exit(-1);
				}
				else
					System.out.println(tab + "check took " + (end - start) + "ms; found no errors");
			}
			
			String fullName = stratum + "::" + component + (port == null ? "" : "." + port);
			System.out.println(ARROW + " running " + fullName);
			
			// find the correct stratum
			DEStratum runStratum = findNamedStratum(fullName, new StringTokenizer(stratum, "::"), root);
			DEComponent runComponent = findNamedComponent(fullName, runStratum, component);
			
			// should we just display a nice tree?
			if (displayTreeOnly)
			{
				new FlattenedTreeMaker().makeTreeWindow(root, runComponent);
				return;
			}

			// flatten out
			BBSimpleElementRegistry registry = new BBSimpleElementRegistry(root, runComponent);
			BBSimpleComponent top = new BBSimpleComponent(registry, runComponent);

			// cannot have any non-optional required ports
			if (top.getPorts() != null)
				for (BBSimplePort p : top.getPorts())
					if (p.getRequires().size() != 0 && p.getLowerBound() != 0)
						throw new BBImplementationInstantiationException("Top component must not have mandatory required ports", top);

			BBSimplePort provider = null;
			if (port != null && top.getPorts() != null)
				for (BBSimplePort p : top.getPorts())
				{
					if (p.getRawName().equals(port))
						provider = p;
				}
			if (provider != null)
			{
				if (provider.getRequires().size() != 0 || provider.getProvides().size() != 1)
					throw new BBImplementationInstantiationException("Top component run port must provide only IRun", top);
				BBSimpleInterface iface = provider.getProvides().get(0);
				if (iface.getImplementationClassName() == null || !iface.getImplementationClassName().equals(IRun.class.getName()))
					throw new BBImplementationInstantiationException("Top component run port must provide only IRun", top);
			}
			else
				if (port != null)
					throw new BBImplementationInstantiationException("Cannot locate port " + port, top);

			BBSimpleInstantiatedFactory instance = top.instantiate(registry, beforeConnections);
			if (provider != null)
				instance.runViaPort(provider, args);
		}
		catch (BBNodeNotFoundException e)
		{
			System.err.println("Problem finding reference: " + e.getMessage());
			System.err.println("Location of error: " + e.getLocation());
			System.exit(-1);
		}
		catch (StratumLoadingException e)
		{
			System.err.println("Problem loading stratum: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		catch (BBVariableNotFoundException e)
		{
			System.err.println("Problem loading stratum: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		catch (BBBadRunPointException e)
		{
			System.err.println("Bad run point specified: " + e.getMessage());
			System.exit(-1);
		}
		catch (BBImplementationInstantiationException e)
		{
			if (e.getElementUuid() != null)
				System.err.println(e.getElementName() + " | " + e.getMessage() + " at " + e.getElementUuid() + " |");
			e.printStackTrace(System.err);
			System.exit(-1);
		}
		catch (BBRuntimeException e)
		{
			if (e.getElement() == null)
			{
				System.err.println("Runtime problem: " + e.getMessage());
			}
			else
				System.err.println(e.getElement().getName() + " | " + e.getMessage() + " at " + e.getElement().getUuid() + " |");
			if (e.getCause() != null)
				e.getCause().printStackTrace(System.err);

			System.exit(-1);
		}
	}
	
	private static String[] truncateInterpreterArgs(String[] args, int offset)
	{
		int displace = 4 /* loadlist, stratum, component, port */ + offset /* possible nocheck */;
		String[] newArgs = new String[args.length - displace];
		for (int lp = displace; lp < args.length; lp++)
			newArgs[lp - displace] = args[lp];
		return newArgs;
	}

	public static List<BBStratum> loadSystem(File loadListFile, String tab) throws BBNodeNotFoundException, BBVariableNotFoundException, StratumLoadingException
	{
		LoadListReader reader = new LoadListReader(loadListFile);			
		// install the delta engine
		GlobalDeltaEngine.engine = new BBDeltaEngine();
		long start = System.currentTimeMillis();
		List<BBStratum> system = reader.readSystem();
		long end = System.currentTimeMillis();
		System.out.println(tab + "loaded " + system.size() + " packages succesfully in " + (end - start) + "ms");
		
		// tell each package about their parent
		// anything that doesn't have a loaded parent gets the model
		NodeRegistry reg = GlobalNodeRegistry.registry;
		BBStratum root = GlobalNodeRegistry.registry.getRoot();
		for (BBStratum pkg : system)
		{
			String parentUUID = pkg.getParentUuid();
			if (reg.hasNode(new UuidReference(parentUUID)))
					pkg.setParentAndTellChildren((BBStratum) reg.getNode(parentUUID, DEStratum.class));
			else
				pkg.setParentAndTellChildren(root);
		}

		return system;
	}
	
	private static DEComponent findNamedComponent(String fullName, DEStratum runStratum, String componentName) throws BBBadRunPointException
	{
		for (DEElement elem : runStratum.getChildElements())
		{
			if (elem.asComponent() != null && elem.asComponent().getName().equals(componentName))
				return elem.asComponent(); 
		}
		throw new BBBadRunPointException("Cannot find component: " + fullName);
	}

	private static DEStratum findNamedStratum(String fullName, StringTokenizer tokens, DEStratum start) throws BBBadRunPointException
	{
		// are we the match?
		if (!tokens.hasMoreTokens())
			return start;

		String next = tokens.nextToken();
		for (DEStratum child : start.getDirectlyNestedPackages())
		{
			if (matches(child.getName(), next))
			{
				DEStratum match = findNamedStratum(fullName, tokens, child);
				if (match != null)
					return match;
			}
		}
		// if we got here, we couldn't find a named match
		throw new BBBadRunPointException("Cannot find stratum: " + fullName);
	}

	private static boolean matches(String name1, String name2)
	{
		// replace any carriage returns with a space
		return replace(name1).equals(replace(name2));
	}

	private static String replace(String name)
	{
		return name.replace('\n', ' ').replace('\r', ' ');
	}
}
