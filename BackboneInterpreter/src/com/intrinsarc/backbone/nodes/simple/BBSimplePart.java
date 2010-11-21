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
package com.intrinsarc.backbone.nodes.simple;

import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.deltaengine.base.*;

public class BBSimplePart extends BBSimpleObject
{
	private String name;
	private String rawName;
	private int depth;
	private BBSimpleLevel level;
	private BBSimpleComponent type;
	private List<BBSimpleSlot> slots;
	private boolean resolved;
	private DEPart complex;
	private int factory;
	private BBSimpleComponent owner;
	private List<DEPart> partStack;
	private Map<BBSimplePort, Map<String, Class<?>>> allProvidedToRequired;
	private Map<BBSimplePort, Map<String, String>> allProvidedToRequiredNames;	
	
	public BBSimplePart(BBSimpleElementRegistry registry, BBSimpleComponent component, DEPart complex, BBSimpleComponent owner)
	{
		rawName = complex.getName();
		name = registry.makeName(rawName);
		this.complex = complex;
		this.type = complex.getType() == null ? null : registry.retrieveComponent(complex.getType());
		
		// copy over the slots
		List<DESlot> cslots = complex.getSlots();
		if (!cslots.isEmpty())
		{
			slots = new ArrayList<BBSimpleSlot>();
			for (DESlot slot : cslots)
				slots.add(new BBSimpleSlot(registry, component, complex.getType(), slot));
		}
		this.owner = owner;
		partStack = new ArrayList<DEPart>();
		partStack.add(complex);
	}

	public BBSimplePart(BBSimpleElementRegistry registry, BBSimplePart p, BBSimplePart enclosingPart, Map<BBSimpleAttribute, BBSimpleAttribute> copied, int depth, int factory, int parentFactory, String fullName, DEComponent top)
	{
		partStack = new ArrayList<DEPart>();
		partStack.add(p.complex);
		partStack.addAll(enclosingPart.partStack);
		rawName = p.getRawName();
		name = registry.makeName(fullName == null ? "" : fullName + " :: ") + " " + rawName;
		complex = p.complex;
		type = p.type;
		owner = p.owner;
		this.depth = depth;
		this.factory = p.complex.isPullUp() ? parentFactory : factory;
		
		// make new slots that reference the flattened attributes
		if (!p.getSlots().isEmpty())
		{
			slots = new ArrayList<BBSimpleSlot>();
			for (BBSimpleSlot s : p.slots)
				slots.add(new BBSimpleSlot(registry, s, copied));
		}		
		
		try
		{
			resolveProvidedToRequiredNames(registry, true, false);
		}
		catch (BBImplementationInstantiationException e)
		{
			// not actually thrown for resolving names only
		}
	}

	private void resolveProvidedToRequiredNames(BBSimpleElementRegistry registry, boolean names, boolean classes) throws BBImplementationInstantiationException
	{
		DEStratum perspective = registry.getPerspective();
		
		if (names)
			 allProvidedToRequiredNames = new HashMap<BBSimplePort, Map<String, String>>();
		if (classes)
			 allProvidedToRequired = new HashMap<BBSimplePort, Map<String, Class<?>>>();
		
		// for each port, get the required interfaces that the provided interfaces must satisfy
		if (type.getPorts() != null)
		{
			for (BBSimplePort p : type.getPorts())
			{
				Set<? extends DEInterface> reqs =
					registry.getTopComponent().getPortProvidedInterfacesInCompositionHierarchy(
								perspective,
								p.getComplexPort(),
								partStack);
				
				Map<String, String> providedToRequiredNames = new HashMap<String, String>();
				Map<String, Class<?>> providedToRequired = new HashMap<String, Class<?>>();
				for (BBSimpleInterface prov : p.getProvides())
				{
					DEInterface req = DEComponent.getOneMatch(perspective, prov.getComplex(), reqs);
					if (req != null)
					{
						String provClassName = prov.getImplementationClassName();
						if (names)
						{
							providedToRequiredNames.put(
									provClassName,
											req.getImplementationClass(perspective));
						}
						if (classes)
						{
							providedToRequired.put(
									provClassName,
										getImplementation(
											req.getImplementationClass(perspective), req));
						}
					}
				}
				if (names && !providedToRequiredNames.isEmpty())
					allProvidedToRequiredNames.put(p, providedToRequiredNames);
				if (classes && !providedToRequired.isEmpty())
					allProvidedToRequired.put(p, providedToRequired);
			}
		}
	}
	
	public void resolveImplementation(BBSimpleElementRegistry registry) throws BBImplementationInstantiationException
	{
		if (resolved)
			return;
		resolved = true;
		
		type.resolveImplementation(registry);
		if (slots != null)
			for (BBSimpleSlot slot : slots)
				slot.resolveImplementation(registry, type.getImplementationClass(), owner);
		
		// for each port, get the required interfaces that the provided interfaces must satisfy
		resolveProvidedToRequiredNames(registry, false, true);
	}

	private Class<?> getImplementation(String className, DEInterface iface) throws BBImplementationInstantiationException
	{
		try
		{
			return Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			throw new BBImplementationInstantiationException(
					"Cannot locate class " + className + " for interface " + iface.getName(), owner);
		} 
	}

	public DEPart getComplexPart()
	{
		return complex;
	}

	public BBSimpleComponent getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getRawName()
	{
		return rawName;
	}

	public int getDepth()
	{
		return depth;
	}

	public int getFactory()
	{
		return factory;
	}

	public BBSimpleSlot findSlot(BBSimpleAttribute attr)
	{
		if (slots == null)
			return null;
		for (BBSimpleSlot slot : slots)
			if (slot.getAttribute() == attr)
				return slot;
		return null;
	}

	public List<BBSimpleSlot> getSlots()
	{
		if (slots == null)
			return new ArrayList<BBSimpleSlot>();
		return slots;
	}
	
	public void remapAttributes(Map<BBSimpleAttribute, BBSimpleAttribute> redundant)
	{
		if (slots != null)
			for (BBSimpleSlot s : slots)
				s.remapAttributes(redundant);
	}

	public void setSlots(List<BBSimpleSlot> slots)
	{
		this.slots = slots;
	}

	public String getRequiredImplementationNameForProvided(BBSimpleElementRegistry registry, BBSimplePort simplePort, BBSimpleInterface provided)
	{
		if (allProvidedToRequiredNames == null)
		{
			try
			{
				resolveProvidedToRequiredNames(registry, true, false);
			}
			catch (BBImplementationInstantiationException e)
			{
				// not actually thrown for resolving names only
			}
		}
		
		if (!allProvidedToRequiredNames.containsKey(simplePort))
			return null;
		return allProvidedToRequiredNames.get(simplePort).get(provided.getImplementationClassName());
	}

	public Class<?> getRequiredImplementationForProvided(BBSimplePort simplePort, BBSimpleInterface provided)
	{
		if (!allProvidedToRequired.containsKey(simplePort))
			return null;
		return allProvidedToRequired.get(simplePort).get(provided.getImplementationClassName());
	}

	public void setLevel(BBSimpleLevel level)
	{
		this.level = level;
	}
	
	public BBSimpleLevel getLevel()
	{
		return level;
	}
	
	@Override
	public Map<String, List<? extends BBSimpleObject>> getChildren(boolean top)
	{
		Map<String, List<? extends BBSimpleObject>> children = new LinkedHashMap<String, List<? extends BBSimpleObject>>();
		List<BBSimpleObject> t = new ArrayList<BBSimpleObject>();
		t.add(type);
		children.put("type", t);
		children.put("slots", slots);
		return children;
	}

	@Override
	public String getTreeDescription()
	{
		return "Part " + name;
	}
}
