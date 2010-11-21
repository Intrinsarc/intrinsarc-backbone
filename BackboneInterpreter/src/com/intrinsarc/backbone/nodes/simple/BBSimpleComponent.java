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

import java.lang.reflect.*;
import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.simple.internal.*;
import com.intrinsarc.deltaengine.base.*;


public class BBSimpleComponent extends BBSimpleElement
{
	private String name;
	private String rawName;
	private boolean factory;
	private boolean legacyBean;
	private boolean leaf;
	private List<BBSimplePort>      ports;
	private List<BBSimpleAttribute> attributes;
	private List<BBSimplePart>      parts;
	private List<BBSimpleConnector> connectors;
	private Class<?> implementationClass;
	private boolean resolved;
	private String implementationClassName;
	private boolean lifecycleCallbacks;
	private List<BBSimpleFactory> map = new ArrayList<BBSimpleFactory>();
	private Constructor<?> constructor;
	private String uuid;
	private DEComponent complex;
	
	public BBSimpleComponent(BBSimpleElementRegistry registry, DEComponent complex)
	{
		this.complex = complex;
		rawName = complex.getName(registry.getPerspective());
		name = registry.makeName(rawName);
		uuid = complex.getUuid();
		copyAttributes(registry, complex);
		copyPorts(registry, complex);
		DEStratum perspective = registry.getPerspective(); 
		if (complex.isFactory(perspective))
			factory = true;
		if (complex.isLegacyBean(perspective))
		  legacyBean = true;

		if (!complex.canBeDecomposed(perspective))
		{
			leaf = true;
			// if we are a leaf (and not a placeholder), get the implementation class
			// according to the rules, there is only 1 impl class, as we cannot resemble primitive types
			if (!complex.isPlaceholder(registry.getPerspective()))
				implementationClassName = complex.getImplementationClass(perspective);
			if (complex.hasLifecycleCallbacks(perspective))
				lifecycleCallbacks = true;
		}
		else
		{
			copyParts(registry, complex);
			copyConnectors(registry, complex);
		}
}
	
	public void resolveImplementation(BBSimpleElementRegistry registry) throws BBImplementationInstantiationException
	{
		if (resolved)
			return;
		resolved = true;
		
		if (implementationClassName != null)
			try
			{
				implementationClass = Class.forName(implementationClassName);
				
				// now find a constructor that matches with the instantiated factory parameter
				constructor = implementationClass.getConstructor(BBSimpleInstantiatedFactory.class);
			}
			catch (ClassNotFoundException e)
			{
				throw new BBImplementationInstantiationException("Cannot find class " + implementationClassName + ", referred to by component " + name, this);
			}
			catch (SecurityException e)
			{
				throw new BBImplementationInstantiationException("Security exception getting constructor for " + implementationClassName + ", referred to by component " + name, this);
			}
			catch (NoSuchMethodException e)
			{
				// nothing to do -- this is ok, we will use the default constructor
			}
		
		// the attributes, connectors and parts have their implementations resolved by the simpleinstantiatedfactory
		// which by then is a flattened representation of this.  Note that the slots of the parts in the flattened
		// factory hold the implementation references for the implementation attributes.
		if (ports != null)
			for (BBSimplePort port : ports)
				port.resolveImplementation(registry);
	}

	private void copyAttributes(BBSimpleElementRegistry registry, DEComponent original)
	{
		Set<DeltaPair> pairs = original.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getConstituents(registry.getPerspective());
		List<DEAttribute> attrs = new ArrayList<DEAttribute>();
  	SimpleDirectedGraph<DEAttribute> graph = new SimpleDirectedGraph<DEAttribute>();
		for (DeltaPair pair : pairs)
		{
			DEAttribute attr = pair.getConstituent().asAttribute();
			attrs.add(attr);
			graph.addNode(attr);
		}

		// add edges and do a topological sort to ensure that the attributes are in order
		for (DEAttribute attr : attrs)
		{
			for (DEParameter p : attr.getDefaultValue())
			{
				DEAttribute pAttr = p.getAttribute(registry.getPerspective(), original);
				if (pAttr != null && attrs.contains(pAttr))
					graph.addEdge(attr, pAttr);
			}
		}
		
		List<DEAttribute> sorted = graph.makeTopologicalSort(true);
		if (!sorted.isEmpty())
		{
			// topologically sort the attributes
			attributes = new ArrayList<BBSimpleAttribute>();
			for (DEAttribute complex : sorted)
			{
				BBSimpleAttribute attr = new BBSimpleAttribute(registry, complex, attributes, this);
				attributes.add(attr);
			}
		}
	}
	
	private void copyParts(BBSimpleElementRegistry registry, DEComponent original)
	{
		Set<DeltaPair> pairs = original.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(registry.getPerspective());
		if (!pairs.isEmpty())
		{
			parts = new ArrayList<BBSimplePart>();
			for (DeltaPair pair : pairs)
			{
				DEConstituent complex = pair.getConstituent();
				
				// if we have a part that is a placeholder type, we have a problem
				DEComponent type = complex.asPart().getType();
				if (type != null)
				{
					if (type.isPlaceholder(registry.getPerspective()))
						registry.addHasPlaceholderParts(this);
	
					BBSimplePart part = new BBSimplePart(registry, this, complex.asPart(), this);				
					parts.add(part);
				}
			}
		}
	}	

	private void copyPorts(BBSimpleElementRegistry registry, DEComponent original)
	{
		Set<DeltaPair> pairs = original.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(registry.getPerspective());
		if (!pairs.isEmpty())
		{
			ports = new ArrayList<BBSimplePort>();
			for (DeltaPair pair : pairs)
			{
				DEConstituent complex = pair.getConstituent();
				BBSimplePort port = new BBSimplePort(registry, original, complex.asPort(), this);
				ports.add(port);
			}
		}
	}

	private void copyConnectors(BBSimpleElementRegistry registry, DEComponent original)
	{
		Set<DeltaPair> pairs = original.getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getConstituents(registry.getPerspective());
		if (!pairs.isEmpty())
		{
			connectors = new ArrayList<BBSimpleConnector>();
			for (DeltaPair pair : pairs)
			{
				DEConstituent complex = pair.getConstituent();
				BBSimpleConnector connector = new BBSimpleConnector(registry, original, complex.asConnector(), parts, ports, this);
				connectors.add(connector);
			}
		}
	}	

	@Override
	public Class<?> getImplementationClass()
	{
		return implementationClass;
	}
	
	@Override
	public String getImplementationClassName()
	{
		return implementationClassName;
	}

	public BBSimpleInstantiatedFactory instantiate(BBSimpleElementRegistry registry, IRuntimeCallback callback) throws BBImplementationInstantiationException, BBRuntimeException
	{
		flatten(registry);

		// see if we have any placeholder parts
		if (!registry.getHasPlaceholderParts().isEmpty())
		{
			String places = "";
			BBSimpleElement first = null;
			for (BBSimpleComponent comp : registry.getHasPlaceholderParts())
			{
				first = comp;
				places += (places.length() > 0 ? ", " : "") + comp.getName();
			}
			throw new BBImplementationInstantiationException("Found components with placeholder parts: " + places, first);
		}
		
		// see if we have any complex connectors
		if (!registry.getHasComplexConnectors().isEmpty())
		{
			String places = "";
			BBSimpleElement first = null;
			for (BBSimpleComponent comp : registry.getHasComplexConnectors())
			{
				first = comp;
				places += (places.length() > 0 ? ", " : "") + comp.getName();
			}
			throw new BBImplementationInstantiationException("Found components with connectors too complex to currently handle: " + places, first);
		}
		
		// ask each factory to resolve itself
		for (BBSimpleFactory factory : map)
			factory.resolveImplementation(registry);
		
		// instantiate the first factory, giving
		BBSimpleInstantiatedFactory instance = new BBSimpleInstantiatedFactory(registry.getPerspective(), null, map.get(0));
		instance.instantiate(null, callback);
		return instance;
	}

	public List<BBSimplePort> getPorts()
	{
		return ports;
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
	
	public boolean isLeaf()
	{
		return leaf;
	}
	
	public List<BBSimpleAttribute> getAttributes()
	{
		if (attributes == null)
			return new ArrayList<BBSimpleAttribute>();
		return attributes;
	}

	public void flatten(BBSimpleElementRegistry registry)
	{
		// repeatedly ask any parts to flatten themselves into the structure of this component
		BBSimpleLevel level = new BBSimpleLevel(null);
		if (parts != null)
			for (BBSimplePart part : parts)
			{
				part.setLevel(level);
				level.addPart(part);
			}
		
		int factoryId[] = new int[]{0};
		for (;;)
		{
			boolean flattenedSomething = false;
			if (parts != null)
				for (BBSimplePart part : parts)
				{
					if (!part.getType().isLeaf())
					{
						flattenDown(registry, part, factoryId);
						flattenedSomething = true;
						break;
					}
				}

			// if we haven't anything left to flatten, exit
			if (!flattenedSomething)
				break;
		}
		
		// name the top
		getMap(0).setComponent(this);
		
		// normalize any slots into attributes
		normalizeSlots(registry);
		
		// remove any redundant attributes and compress
		removeRedundantAttributes();
		
		// make the mapped structures
		if (attributes != null)
			for (BBSimpleAttribute a : attributes)
				getMap(a.getFactory()).addAttribute(a);

		if (parts != null)
			for (BBSimplePart p : parts)
				getMap(p.getFactory()).addPart(p);
		if (connectors != null)
			for (BBSimpleConnector c : connectors)
				getMap(c.getFactory()).addConnector(c);
		
		// set up any hyperconnectors required
		if (parts != null)
			for (BBSimplePart p : parts)
				processHyperConnections(registry.getPerspective(), p);
	}

	private void processHyperConnections(DEStratum perspective, BBSimplePart part)
	{
		// see if there are any hyper-port ends which are not connected
		if (part.getType().getPorts() != null)
			for (BBSimplePort port : part.getType().getPorts())
			{
				if (!connected(part, port))
				{
					// now look through all the factories to find a suitable match
					BBSimplePart matchedPart[] = new BBSimplePart[1];
					BBSimplePort matchedPort[] = new BBSimplePort[1];
					int moveUp = 0;
					while (matchedPort[0] == null)
					{
						List<BBSimplePart> parts = part.getLevel().getParts(moveUp++);
						if (parts == null)
							break;
						findClosestHyperMatch(perspective, part, port, parts, matchedPart, matchedPort);
					}
					
					// make a new connector if we have a match
					if (matchedPort[0] != null)
					{
						BBSimpleConnector hyperConn = new BBSimpleConnector(
								matchedPart[0],
								matchedPort[0],
								part,
								port);
						map.get(part.getFactory()).addConnector(hyperConn);						
					}
				}
			}
	}
	
	private void findClosestHyperMatch(
			DEStratum perspective,
			BBSimplePart originalPart,
			BBSimplePort originalPort,
			List<BBSimplePart> parts,
			BBSimplePart[] matchedPart,
			BBSimplePort[] matchedPort)
	{
		Set<? extends DEInterface> provided = getProvided(perspective, originalPort);
		Set<? extends DEInterface> required = getRequired(perspective, originalPort);

		List<BBSimplePart> matchedParts = new ArrayList<BBSimplePart>();
		List<BBSimplePort> matchedPorts = new ArrayList<BBSimplePort>();
		
		// keep going until we have a match
		for (BBSimplePart part : parts)
		{
			if (part != originalPart && part.getType().getPorts() != null)
				for (BBSimplePort port : part.getType().getPorts())
				{
					if (port.isHyperport() && !connected(part, port))
					{
						if (DEComponent.oneToOneMappingExists(
									perspective,
									getProvided(perspective, port),
									getRequired(perspective, port),
									provided,
									required) &&
								DEComponent.providesEnough(
										perspective,
										getProvided(perspective, port),
										required) &&
								DEComponent.providesEnough(
										perspective,
										provided,
										getRequired(perspective, port)))
						{
							matchedParts.add(part);
							matchedPorts.add(port);
						}
					}
				}
		}
			
		// if we have just one match, send it back immediately; otherwise face each off against each other
		// we want the requires from the end to connect to the lowest provision from the start
		// i.e. we don't want a requirement for JComponent to connect to JLabel provided if JComponent exists
		// (so we want the lowest provided from the start)
		// we want the provides from the end to connect to the highest requires from the start
		// i.e. we don't want a provides for JLabel to connect to a JComponent required if JLabel exists
		// (so we want the highest requires from the start)
		if (matchedParts.size() >= 1)
		{
			// start by choosing the 1st
			matchedPart[0] = matchedParts.get(0);
			matchedPort[0] = matchedPorts.get(0);
			int size = matchedParts.size();
			if (size == 1)
				return;
			
			for (int lp = 0; lp < size; lp++)
			{
				BBSimplePort port = matchedPorts.get(lp);

				boolean allOk = true; 
				for (int lp2 = 0; lp2 < size; lp2++)
				{
					if (lp == lp2)
						continue;  // no point
					
					BBSimplePort port2 = matchedPorts.get(lp2);
					Set<DEElement> req = new HashSet<DEElement>(getRequired(perspective, port));
					Set<DEElement> prov = new HashSet<DEElement>(getProvided(perspective, port2));

					// ensure we have the highest required from the start
					for (DEElement ifaceO : new HashSet<DEElement>(req))
					{
						Set<DEElement> supers = ifaceO.getSuperElementClosure(perspective, true);
						for (DEInterface iface : getRequired(perspective, port2))
							if (ifaceO == iface || supers.contains(iface))
								req.remove(ifaceO);									
					}
					// ensure we have the lowest provided from the start
					for (DEElement iface : new HashSet<DEElement>(prov))
					{
						Set<DEElement> supers = iface.getSuperElementClosure(perspective, true);
						for (DEElement ifaceO : getProvided(perspective, port))
							if (ifaceO == iface || supers.contains(ifaceO))
								prov.remove(iface);
					}
					
					if (!req.isEmpty() || !prov.isEmpty())
					{
						allOk = false;
						break;  // no point going on...
					}
				}
				
				// if everything matched ok, choose this one
				if (allOk)
				{
					matchedPart[0] = matchedParts.get(lp);
					matchedPort[0] = port;
					return;
				}
			}
			// we've found no ideal match so just take the first
		}
	}

	private Set<? extends DEInterface> getRequired(DEStratum perspective, BBSimplePort port)
	{
		return port.getOwner().complex.getRequiredInterfaces(perspective, port.getComplexPort());
	}

	private Set<? extends DEInterface> getProvided(DEStratum perspective, BBSimplePort port)
	{
		return port.getOwner().complex.getProvidedInterfaces(perspective, port.getComplexPort());
	}

	private boolean connected(BBSimplePart part, BBSimplePort port)
	{
		for (BBSimpleConnector c : connectors)
			if (c.getEndNumber(part, port) != -1 && !c.isHyperConnector())
				return true;
		return false;
	}

	private void normalizeSlots(BBSimpleElementRegistry registry)
	{
		// copy across the attributes and default values, or slots, into the factory frame
		if (parts != null)
		{
			for (BBSimplePart part : parts)
			{
				List<BBSimpleSlot> oldSlots = part.getSlots();
				List<BBSimpleSlot> newSlots = new ArrayList<BBSimpleSlot>();
				for (BBSimpleAttribute attr : part.getType().getAttributes())
				{
					// we can't set a slot for a read-only attribute
					if (attr.isReadOnly())
						continue;
					
					// if the part is a bean, only push up the attribute if it is associated with a slot
  			  BBSimpleSlot slot = null;
  			    for (BBSimpleSlot s : oldSlots)
  			      if (attr == s.getAttribute())
  			      {
  			        slot = s;
  			        break;
  			      }
					if (part.getType().isLegacyBean() && slot == null)
					  continue;
					
					BBSimpleAttribute pushedAttr =
						new BBSimpleAttribute(
								registry,
								attr,
								oldSlots,
								part.getFactory(),
								part.getFactory(),
								null,
								part.getRawName(),
								PositionEnum.BOTTOM);
					
					// possibly lazily create attributes
					if (attributes == null)
			     attributes = new ArrayList<BBSimpleAttribute>();

					attributes.add(pushedAttr);
					newSlots.add(new BBSimpleSlot(registry, attr, pushedAttr));
				}
				part.setSlots(newSlots.isEmpty() ? null : newSlots);
			}
		}
	}

	private void removeRedundantAttributes()
	{
		if (attributes == null)
			return;
		
		// remove any aliases that are effectively redundant
		Map<BBSimpleAttribute, BBSimpleAttribute> redundant = new HashMap<BBSimpleAttribute, BBSimpleAttribute>();
		if (attributes != null)
		for (Iterator<BBSimpleAttribute> iter = attributes.iterator(); iter.hasNext();)
		{
			BBSimpleAttribute attr = iter.next();
			if (isRedundant(attr) && attr.getPosition() != PositionEnum.TOP)
			{
				iter.remove();
				redundant.put(attr, getVariableParameter(attr));
			}
		}
		
		// handle replacements which have now been invalidated
		for (BBSimpleAttribute attr : redundant.keySet())
		{
			BBSimpleAttribute replace = redundant.get(attr);
			while (replace != null && redundant.containsKey(replace))
				replace = redundant.get(replace);
			redundant.put(attr, replace);
		}
	
		// ask everything to remap attributes
		remapAttributes(redundant);

		// remove anything which doesn't need to be there
		Set<BBSimpleAttribute> all = new HashSet<BBSimpleAttribute>();
		for (BBSimpleAttribute attr : attributes)
			if (attr.getPosition() == PositionEnum.BOTTOM)
				attr.addAllDependencies(all);
		if (parts != null)
			for (BBSimplePart part : parts)
			{
				if (part.getSlots() != null)
					for (BBSimpleSlot slot : part.getSlots())
						if (slot.getEnvironmentAlias() != null)
							all.add(slot.getEnvironmentAlias());
			}		
		int size;
		do
		{
			size = all.size();
			for (BBSimpleAttribute attr : attributes)
				if (attr.getPosition() == PositionEnum.MIDDLE && all.contains(attr))
					attr.addAllDependencies(all);
		} while (all.size() != size);

		for (Iterator<BBSimpleAttribute> iter = attributes.iterator(); iter.hasNext();)
		{
			BBSimpleAttribute attr = iter.next();
			if (attr.getPosition() == PositionEnum.MIDDLE && !all.contains(attr))
				iter.remove();
		}
	}

	private BBSimpleAttribute getVariableParameter(BBSimpleAttribute attr)
	{
		return attr.getDefaultValue().get(0).getAttribute();
	}

	private void remapAttributes(Map<BBSimpleAttribute, BBSimpleAttribute> redundant)
	{
		if (attributes != null)
			for (BBSimpleAttribute a : attributes)
				a.remapAttributes(redundant);
		if (parts != null)
			for (BBSimplePart p : parts)
				p.remapAttributes(redundant);
	}

	// an attribute is redundant if it is a direct copy, but it isn't at the top...
	private boolean isRedundant(BBSimpleAttribute attr)
	{
		return
			attr.getDefaultValue() != null && attr.getDefaultValue().size() == 1 && attr.getDefaultValue().get(0).getAttribute() != null;
	}

	private BBSimpleFactory getMap(int factory)
	{
		while (map.size() < factory + 1)
			map.add(new BBSimpleFactory(this, map.size()));
		return map.get(factory);
	}

	private void flattenDown(BBSimpleElementRegistry registry, BBSimplePart part, int factoryId[])
	{
		String full = part.getRawName();
		BBSimpleLevel next = new BBSimpleLevel(part.getLevel());
		
		// remove from the list of parts
		parts.remove(part);

		// remake all the attributes and push them up to this level
		if (attributes == null)
			attributes = new ArrayList<BBSimpleAttribute>();

		BBSimpleComponent type = part.getType();
		PositionEnum position = PositionEnum.classifyPart(part);
		int factory = type.isFactory() ? ++factoryId[0] : part.getFactory();
		getMap(factoryId[0]).setComponent(type);
		Map<BBSimpleAttribute, BBSimpleAttribute> copiedFromFrame = new HashMap<BBSimpleAttribute, BBSimpleAttribute>();
		for (BBSimpleAttribute old : part.getType().getAttributes())
		{
			// look for a slot to see if this is aliased or set to a different value
			BBSimpleSlot slot = part.findSlot(old);
			// don't copy over if this isn't a bean and it isn't set
			if (type.isLegacyBean() && slot == null)
			  continue;

			BBSimpleAttribute newAttr =
				slot != null ?
						new BBSimpleAttribute(registry, slot, factory, part.getFactory(), full, position, copiedFromFrame) :
						new BBSimpleAttribute(registry, old, factory, part.getFactory(), full, position, copiedFromFrame);
			attributes.add(newAttr);
			copiedFromFrame.put(old, newAttr);
		}
		
		// add the subparts, giving them a map to allow repointing of attributes
		Map<BBSimplePart, BBSimplePart> partTranslationMap = new HashMap<BBSimplePart, BBSimplePart>();			
		for (BBSimplePart oldPart : part.getType().parts)
		{				
			BBSimplePart newPart = new BBSimplePart(registry, oldPart, part, copiedFromFrame, part.getDepth() + 1, factory, part.getFactory(), full, complex);
			partTranslationMap.put(oldPart, newPart);
			parts.add(newPart);
			newPart.setLevel(next);
			next.addPart(newPart);
		}
		
		// zero out the attributes if needed
		if (attributes.isEmpty())
			attributes = null;

		// now handle connectors from a part to the component edge
		if (type.ports != null)
			for (BBSimplePort port : type.getPorts())
			{
				List<BBSimpleConnector> internal = findConnectors(type.connectors, null, port);
				List<BBSimpleConnector> external = findConnectors(connectors, part, port);
				
				if (internal.size() == 1)
				{
					BBSimpleConnectorEnd internalEnd = getAliasEnd(internal.get(0), null, port);
					remapConnectors(external, part, port, partTranslationMap.get(internalEnd.getPart()), internalEnd.getPort(), factory);
				}
				else
				if (external.size() == 1)
				{
					for (BBSimpleConnector intern : internal)
					{
						BBSimpleConnectorEnd internalEnd = getAliasEnd(intern, null, port);
						remapConnectors(external, part, port, partTranslationMap.get(internalEnd.getPart()), internalEnd.getPort(), factory);
					}
				}
				else
				{
					// must place a connector component in the way
					registry.getHasComplexConnectors().add(this);
				}
			}
		
		// merge the connectors of the subcomponent with the parent's
		// handle internal connectors by just copying these up to a higher level
		if (type.connectors != null)
		for (BBSimpleConnector conn : type.connectors)
			if (conn.isInternal())
				connectors.add(new BBSimpleConnector(registry, conn, partTranslationMap));
	}

	public boolean isFactory()
	{
		return factory;
	}

	private void remapConnectors(List<BBSimpleConnector> conns, BBSimplePart matchPart, BBSimplePort matchEnd, BBSimplePart newPart, BBSimplePort newPort, int factory)
	{
		// remove all connectors from the current component
		connectors.removeAll(conns);
		
		// make new ones
		for (BBSimpleConnector conn : conns)
		{
			BBSimpleConnector c = new BBSimpleConnector(conn, matchPart, matchEnd, newPart, newPort);
			connectors.add(c);
		}
	}

	private BBSimpleConnectorEnd getAliasEnd(BBSimpleConnector conn, BBSimplePart part, BBSimplePort port)
	{
		int num = conn.getEndNumber(part, port);
		if (conn.isDelegate() || conn.getIndex(1 - num) == null)
			return conn.makeSimpleConnectorEnd(1 - num);
		return null;
	}

	private List<BBSimpleConnector> findConnectors(List<BBSimpleConnector> connectors, BBSimplePart part, BBSimplePort port)
	{
		List<BBSimpleConnector> filtered = new ArrayList<BBSimpleConnector>();
		if (connectors != null)
			for (BBSimpleConnector conn : connectors)
				if (conn.getEndNumber(part, port) != -1)
					filtered.add(conn);
		return filtered;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public Object makeNewInstance(BBSimpleInstantiatedFactory simpleInstantiatedFactory) throws BBRuntimeException
	{
		Class<?> impl = getImplementationClass();
		try
		{
			if (constructor != null)
				return constructor.newInstance(simpleInstantiatedFactory);
			return getImplementationClass().newInstance();
		}
		catch (Exception ex)
		{
			throw new BBRuntimeException("Problem instantiating leaf class: " + impl, this, ex);
		}
	}

	public BBSimpleFactory getFactory(int factoryNumber)
	{
		return map.get(factoryNumber);
	}
	
	public int getNumberOfFactories()
	{
		return map.size();
	}

	public String getUuid()
	{
		return uuid;
	}

  public boolean isLegacyBean()
  {
    return legacyBean;
  }
  
  public void setLifecycleCallbacks(boolean callbacks)
  {
  	lifecycleCallbacks = callbacks;
  }
  
  public boolean hasLifecycleCallbacks()
  {
  	return lifecycleCallbacks;
  }

	public DEElement getComplex()
	{
		return complex;
	}
	
	@Override
	public Map<String, List<? extends BBSimpleObject>> getChildren(boolean top)
	{
		Map<String, List<? extends BBSimpleObject>> children = new LinkedHashMap<String, List<? extends BBSimpleObject>>();
		if (top)
		{
			children.put("factories", map);
			return children;
		}
		
		children.put("attributes", attributes);
		children.put("ports", ports);
		children.put("parts", parts);
		children.put("connectors", connectors);
		return children;
	}

	@Override
	public String getTreeDescription()
	{
		String desc = "Component " + name;
		if (implementationClassName != null)
			desc += ", implementation = " + implementationClassName;
		return desc;
	}
}
