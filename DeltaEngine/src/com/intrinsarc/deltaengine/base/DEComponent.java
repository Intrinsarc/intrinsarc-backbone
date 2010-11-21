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
package com.intrinsarc.deltaengine.base;

import java.util.*;

public abstract class DEComponent extends DEElement
{
	public static final String PLACEHOLDER_STEREOTYPE_PROPERTY = "placeholder";
	public static final String FACTORY_STEREOTYPE_PROPERTY     = "factory";
	public static final String LEGACY_BEAN_STEREOTYPE_PROPERTY = "legacy-bean";
	public static final String LIFECYCLE_CALLBACKS_PROPERTY    = "lifecycle-callbacks";
  public static final String PROTOCOLS_PROPERTY              = "protocols";

	private Map<PortPartPerspective, DELinks> cachedLinks;
	private Map<PortPartPerspective, Set<? extends DEInterface>> cachedProvided;
	private Map<PortPartPerspective, Set<? extends DEInterface>> cachedRequired;
	private Map<PortPartPerspective, Set<DELinkEnd>> cachedVisitedTerminals;
	private Map<PortPartPerspective, Set<DELinkEnd>> cachedVisitedIntoParts;
	private Map<PortPartPerspective, Set<DELinkEnd>> cachedVisitedOutOfParts;
	private Map<PortPartPerspective, Set<DEPort>> cachedVisitedPorts;
	private Map<DEStratum, Set<DEConnector>> cachedImproperlyMatched;
	private Map<DEStratum, DELinks> cachedPortLinks = new HashMap<DEStratum, DELinks>();
	private Map<DEStratum, DELinks> cachedCompositeLinks = new HashMap<DEStratum, DELinks>();
	private Map<DEStratum, DELinks> cachedCompositeLinksAll = new HashMap<DEStratum, DELinks>();

	public Set<DELinkEnd> getCachedVisitedIntoParts(DEStratum perspective, DEPort port, DEPart part)
	{
		getInferredLinks(perspective, port, part);
		return cachedVisitedIntoParts.get(new PortPartPerspective(perspective, port, part));
	}

	public Set<DELinkEnd> getCachedVisitedTerminals(DEStratum perspective, DEPort port, DEPart part)
	{
		getInferredLinks(perspective, port, part);
		return cachedVisitedTerminals.get(new PortPartPerspective(perspective, port, part));
	}

	public Set<DEPort> getCachedVisitedPorts(DEStratum perspective, DEPort port, DEPart part)
	{
		getInferredLinks(perspective, port, part);
		return cachedVisitedPorts.get(new PortPartPerspective(perspective, port, part));
	}

	public DEComponent()
	{
		clearCollections();
	}

	@Override
	public void clearCache(DEStratum perspective)
	{
		super.clearCache(perspective);
		clearCollections(perspective);
	}

	private void clearCollections()
	{
		cachedLinks = new HashMap<PortPartPerspective, DELinks>();
		cachedProvided = new HashMap<PortPartPerspective, Set<? extends DEInterface>>();
		cachedRequired = new HashMap<PortPartPerspective, Set<? extends DEInterface>>();
		cachedVisitedTerminals = new HashMap<PortPartPerspective, Set<DELinkEnd>>();
		cachedVisitedIntoParts = new HashMap<PortPartPerspective, Set<DELinkEnd>>();
		cachedVisitedOutOfParts = new HashMap<PortPartPerspective, Set<DELinkEnd>>();
		cachedVisitedPorts = new HashMap<PortPartPerspective, Set<DEPort>>();
		cachedImproperlyMatched = new HashMap<DEStratum, Set<DEConnector>>();
		cachedPortLinks = new HashMap<DEStratum, DELinks>();
		cachedCompositeLinks = new HashMap<DEStratum, DELinks>();
		cachedCompositeLinksAll = new HashMap<DEStratum, DELinks>();
	}

	private void clearCollections(DEStratum perspective)
	{
		clear(cachedLinks, perspective);
		clear(cachedProvided, perspective);
		clear(cachedRequired, perspective);
		clear(cachedVisitedTerminals, perspective);
		clear(cachedVisitedIntoParts, perspective);
		clear(cachedVisitedOutOfParts, perspective);
		clear(cachedVisitedPorts, perspective);
		cachedImproperlyMatched.remove(perspective);
		cachedPortLinks.remove(perspective);
		cachedCompositeLinks.remove(perspective);
		cachedCompositeLinksAll.remove(perspective);
	}

	private void clear(Map<PortPartPerspective, ? extends Object> cached, DEStratum perspective)
	{
		for (PortPartPerspective ppp : new HashSet<PortPartPerspective>(cached.keySet()))
			if (ppp.getPerspective() == perspective)
				cached.remove(ppp);
	}

	@Override
	public DEComponent asComponent()
	{
		return this;
	}

	public abstract ComponentKindEnum getComponentKind();

	/**
	 * override to return actual port links in leaf (composites can have also, but
	 * this will be flagged as an error)
	 */
	public boolean canBeDecomposed(DEStratum perspective)
	{
		return getComponentKind() != ComponentKindEnum.PRIMITIVE && !isLeaf(perspective) && !isPlaceholder(perspective);
	}

	public DELinks getInferredLinks(DEStratum perspective, DEPort port, DEPart part)
	{
		return getInferredLinks(new HashSet<DEComponent>(), perspective, port, part);
	}

	public DELinks getInferredLinks(DEStratum perspective, DEPort port)
	{
		return getInferredLinks(new HashSet<DEComponent>(), perspective, port, null);
	}

	/**
	 * find any inferred links for a leaf, this is just a translation of the
	 * actual links for a composite, this is more complex, relying on getting the
	 * links of the parts and combining with connectors
	 * 
	 * @param perspective
	 * @param port
	 * @return
	 */
	private DELinks getInferredLinks(Set<DEComponent> visited, DEStratum perspective, DEPort port, DEPart part)
	{
		// have we cached this already?
		PortPartPerspective pp = makePortPerspective(perspective, port, part);
		DELinks cached = cachedLinks.get(pp);
		if (cached != null)
			return cached;

		// leaves use links directly, which are copied onto inferred links
		if (!canBeDecomposed(perspective))
		{
			cached = getPortLinks(perspective).filterByStart(new DELinkEnd(port));
			cachedRequired.put(pp, getRequiredInterfaces(perspective, port));
			cachedProvided.put(pp, getProvidedInterfaces(perspective, port));
			cachedLinks.put(pp, cached);
			return cached;
		}

		// follow links wherever they lead
		// if they lead to a terminal port instance which has a provided port, then
		// this port has no links
		// otherwise, return all ports that this port connects to...
		Set<DEPort> visitedPorts = new HashSet<DEPort>();
		Set<DELinkEnd> visitedIntoParts = new HashSet<DELinkEnd>();
		Set<DELinkEnd> visitedOutOfParts = new HashSet<DELinkEnd>();
		Set<DELinkEnd> visitedTerminals = new HashSet<DELinkEnd>();
		boolean taintedOut[] = new boolean[]{ false };

		DELinkEnd current = new DELinkEnd(port, part);
		DELinks connectorLinks = getCompositeConnectorsAsLinks(perspective);

		// collect the ports
		collectPorts(visited, visitedPorts, visitedIntoParts, visitedOutOfParts, visitedTerminals, taintedOut, perspective,
				current, current, connectorLinks, new HashSet<DELink>(), false, false, 0);
		cachedVisitedPorts.put(pp, visitedPorts);
		cachedVisitedIntoParts.put(pp, visitedIntoParts);
		cachedVisitedOutOfParts.put(pp, visitedOutOfParts);
		cachedVisitedTerminals.put(pp, visitedTerminals);

		// is this tainted? harsh bounces from ports back into the structure...
		collectPorts(visited, new HashSet<DEPort>(), new HashSet<DELinkEnd>(), new HashSet<DELinkEnd>(),
				new HashSet<DELinkEnd>(), taintedOut, perspective, current, current, connectorLinks, new HashSet<DELink>(),
				true, false, 0);

		// now make a new DELinks, but only add the ports if this isn't tainted
		cached = new DELinks();
		if (!taintedOut[0])
		{
			for (DEPort other : visitedPorts)
				cached.addLink(current, new DELinkEnd(other));
		}

		// determine the required interfaces
		Set<DEInterface> req = gatherRequired(perspective, visitedIntoParts);

		Set<DEInterface> required = new HashSet<DEInterface>();
		for (DEElement r : extractLowestCommonSubtypes(perspective, req, true))
			required.add(r.asInterface());
		cachedRequired.put(pp, required);

		Set<DEInterface> prov = gatherProvided(perspective, visitedTerminals, visitedPorts);
		Set<DEInterface> provided = new HashSet<DEInterface>();
		for (DEElement p : extractHighestCommonSupertypes(perspective, prov, true))
			provided.add(p.asInterface());
		cachedProvided.put(pp, provided);
		cachedLinks.put(pp, cached);

		return cached;
	}

	/**
	 * parts contains the types from the initial enclosing component to the
	 * highest in the composition hierarchy. it includes the starting part.
	 * components is the components that contain these parts. i.e. to find which
	 * component contained parts[5], look at components[5].
	 * 
	 * @param perspective
	 * @param startPort
	 * @param startPart
	 * @param lowestToHighest
	 * @return
	 */
	public Set<? extends DEInterface> getPortProvidedInterfacesInCompositionHierarchy(DEStratum perspective, DEPort startPort, List<DEPart> parts)
	{
		Set<DEElement> required = new HashSet<DEElement>();
		collectRequiredFollowingPort(required, perspective, startPort, 0, parts);
		Set<DEInterface> ifaces = new HashSet<DEInterface>();
		for (DEElement elem : extractLowestCommonSubtypes(perspective, required, true))
			ifaces.add(elem.asInterface());
		return ifaces;
	}

	private void collectRequiredFollowingPort(
			Set<DEElement> required,
			DEStratum perspective,
			DEPort port,
			int index,
			List<DEPart> parts)
	{
		DEPart part = parts.get(index);
		DEComponent component = index < parts.size() - 1 ? parts.get(index + 1).getType() : this;

		// add in the port itself 
		required.addAll(port.getSetProvidedInterfaces());
		
		// get all the possible terminals
		for (DELinkEnd end : component.getCachedVisitedTerminals(perspective, port, part))
			required.addAll(end.getPart().getType().getRequiredInterfaces(perspective, end.getPort()));

		// treat the ports a bit like a terminal, but only if we are at the top level
		if (index == parts.size() - 1)
		{
			for (DEPort nextPort : component.getCachedVisitedPorts(perspective, port, part))
				required.addAll(component.getProvidedInterfaces(perspective, nextPort));
		}

		if (index < parts.size() - 1)
			for (DEPort nextPort : component.getCachedVisitedPorts(perspective, port, part))
				collectRequiredFollowingPort(required, perspective, nextPort, index + 1, parts);
	}

	private Set<DEInterface> gatherRequired(DEStratum perspective, Set<DELinkEnd> partEnds)
	{
		Set<DEInterface> req = new HashSet<DEInterface>();
		for (DELinkEnd end : partEnds)
			req.addAll(end.getPart().getType().getRequiredInterfaces(perspective, end.getPort()));
		return req;
	}

	private Set<DEInterface> gatherProvided(DEStratum perspective, Set<DELinkEnd> partEnds, Set<DEPort> componentEnds)
	{
		Set<DEInterface> prov = new HashSet<DEInterface>();
		for (DELinkEnd end : partEnds)
			prov.addAll(end.getPart().getType().getProvidedInterfaces(perspective, end.getPort()));
		for (DEPort port : componentEnds)
			prov.addAll(getRequiredInterfaces(perspective, port));
		return prov;
	}

	private void collectPorts(Set<DEComponent> visited, Set<DEPort> visitedPortsOut, Set<DELinkEnd> intoPartOut,
			Set<DELinkEnd> outOfPartOut, Set<DELinkEnd> terminalOut, boolean taintedOut[], DEStratum perspective,
			DELinkEnd start, DELinkEnd current, DELinks connectorLinks, Set<DELink> visitedLinks, boolean harsh,
			boolean fromInternal, int count)
	{
		boolean isPort = current.isComponentLinkEnd();
		boolean startIsPortInstance = !start.isComponentLinkEnd();

		// protect ourselves against self-containment causing recursion
		visited.add(this);

		// is this a port to add?
		DEPort currentPort = current.getPort();

		// look from here via connectors
		Set<DELinkEnd> viaConnectors = connectorLinks.getEnds(current);
		// don't go directly into a port instance via a connector and then out via
		// another connector
		if (isPort || startIsPortInstance && current.equals(start) || harsh || fromInternal)
			for (DELinkEnd end : viaConnectors)
			{
				DELink link = new DELink(current, end);
				if (!visitedLinks.contains(link))
				{
					visitedLinks.add(link);

					if (end.isComponentLinkEnd())
						visitedPortsOut.add(end.getPort());

					// only follow up if this is a part link end or we are in "harsh" mode
					if (!end.isComponentLinkEnd() || harsh)
						collectPorts(visited, visitedPortsOut, intoPartOut, outOfPartOut, terminalOut, taintedOut, perspective,
								start, end, connectorLinks, visitedLinks, harsh, false, count + 1);

					// if a link is a loopback, then add the start port -- we have a cycle
					if (current.equals(end) && start.isComponentLinkEnd())
						visitedPortsOut.add(start.getPort());
				}
			}

		// if this is a port instance, look from here via inferred links of the
		// component
		if (!isPort && !fromInternal && !(startIsPortInstance && count == 0))
		{
			DEComponent type = current.getPart().getType();
			if (!visited.contains(type))
			{
				DELinks internal = type.getInferredLinks(new HashSet<DEComponent>(visited), perspective, current.getPort(), null);
				Set<DELinkEnd> internalEnds = internal.getEnds(new DELinkEnd(currentPort));

				// is this a terminal port instance?
				if (internalEnds.size() == 0 || nothingFurther(current, internalEnds, connectorLinks))
				{
					terminalOut.add(current);

					// is this tainted? if it touches an a terminal port instance which
					// provides, then it cannot have inferred links
					Set<? extends DEInterface> provides = current.getPart().getType().getProvidedInterfaces(perspective,
							current.getPort());
					if (provides.size() > 0)
						taintedOut[0] = true;
				}

				// as long as this is not from an internal transition, add it to the
				// collection
				if (!fromInternal)
					intoPartOut.add(current);
				else
					outOfPartOut.add(current);

				for (DELinkEnd end : internalEnds)
				{
					// should only have component link ends
					DELinkEnd other = new DELinkEnd(end.getPort(), current.getPart());
					DELink link = new DELink(current, other);
					if (!visitedLinks.contains(link))
					{
						visitedLinks.add(link);
						collectPorts(visited, visitedPortsOut, intoPartOut, outOfPartOut, terminalOut, taintedOut, perspective,
								start, other, connectorLinks, visitedLinks, harsh, true, count + 1);
						if (other.isComponentLinkEnd())
							visitedPortsOut.add(other.getPort());
					}

					// if a link is a loopback, then add the start port -- we have a cycle
					if (current.equals(other) && start.isComponentLinkEnd())
						visitedPortsOut.add(start.getPort());
				}
			}
		}
	}

	private boolean nothingFurther(DELinkEnd current, Set<DELinkEnd> internalEnds, DELinks connectorLinks)
	{
		// return true if the internal ends link to ports which are not connected
		// further
		for (DELinkEnd end : internalEnds)
			if (!connectorLinks.getEnds(new DELinkEnd(end.getPort(), current.getPart())).isEmpty())
				return false;
		return true;
	}

	public Set<DEConnector> determineImproperlyMatchedPortInstances(DEStratum perspective)
	{
		Set<DEConnector> connectors = cachedImproperlyMatched.get(perspective);

		if (connectors != null)
			return connectors;

		Set<DELinkEnd> ends = new HashSet<DELinkEnd>();
		connectors = new HashSet<DEConnector>();

		DELinks connectorLinks = getCompositeConnectorsAsLinks(perspective);
		for (DeltaPair pair : getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective))
		{
			DEPart part = pair.getConstituent().asPart();
			// try each port instance
			if (part.getType() != null) // is null when part is first created
				for (DeltaPair pair2 : part.getType().getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
				{
					DEPort port = pair2.getConstituent().asPort();

					// don't bother if we clearly have no connector
					DELinkEnd quickEnd = new DELinkEnd(port, part);
					if (connectorLinks.filterByStart(quickEnd).isEmpty())
						continue;

					// make the relevant perspective
					PortPartPerspective pp = makePortPerspective(perspective, port, part);

					// trigger the computation
					getInferredLinks(perspective, port, part);

					Set<? extends DEInterface> provided = part.getType().getProvidedInterfaces(perspective, port);
					Set<? extends DEInterface> required = part.getType().getRequiredInterfaces(perspective, port);

					// try other parts that we link to (going into the part)
					for (DELinkEnd end : cachedVisitedIntoParts.get(pp))
					{
						// must have a 1:1 link
						if (!oneToOneMappingExists(perspective, provided, required, end.getPart().getType().getProvidedInterfaces(
								perspective, end.getPort()), end.getPart().getType().getRequiredInterfaces(perspective, end.getPort())))
						{
							ends.add(new DELinkEnd(port, part));
						}
					}

					// look for any terminals
					DELinks internal = part.getType().getInferredLinks(perspective, port);
					Set<DELinkEnd> internalEnds = internal.getEnds(new DELinkEnd(port));

					// is this a terminal port instance?
					if (internalEnds.isEmpty() || nothingFurther(new DELinkEnd(port, part), internalEnds, connectorLinks))
					{
						// look for provided interfaces of this compared with provided of
						// others
						Set<? extends DEElement> lowest = getShouldBeProvided(pp);
						if (!providesEnough(perspective, provided, lowest))
						{
							ends.add(new DELinkEnd(port, part));
						}
					}

					// try any ports we link to
					for (DEPort vPort : cachedVisitedPorts.get(pp))
					{
						// must have a 1:1 link
						if (!oneToOneMappingExists(perspective, provided, required, getRequiredInterfaces(perspective, vPort),
								getProvidedInterfaces(perspective, vPort)))
						{
							ends.add(new DELinkEnd(vPort));
						}
					}
				}
		}

		// turn the ends into connectors
		for (DELinkEnd end : ends)
			for (DeltaPair pair : getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getConstituents(perspective))
			{
				DEConnector conn = pair.getConstituent().asConnector();
				if (end.getPart() != null)
				{
					for (int lp = 0; lp < 2; lp++)
						if (conn.getPart(perspective, this, lp) == end.getPart()
								&& conn.getPort(perspective, end.getPart().getType(), lp) == end.getPort())
						{
							connectors.add(conn);
						}
				} else
				{
					for (int lp = 0; lp < 2; lp++)
						if (conn.getPart(perspective, this, lp) == null && conn.getPort(perspective, this, lp) == end.getPort())
						{
							connectors.add(conn);
						}
				}
			}

		cachedImproperlyMatched.put(perspective, connectors);
		return connectors;
	}

	private Set<? extends DEElement> getShouldBeProvided(PortPartPerspective pp)
	{
		DEStratum perspective = pp.getPerspective();
		Set<DEInterface> providedUnion = new HashSet<DEInterface>();
		for (DELinkEnd end : cachedVisitedTerminals.get(pp))
			providedUnion.addAll(end.getPart().getType().getRequiredInterfaces(perspective, end.getPort()));
		for (DEPort vPort : cachedVisitedPorts.get(pp))
			providedUnion.addAll(getProvidedInterfaces(perspective, vPort));
		Set<? extends DEElement> lowest = extractLowestCommonSubtypes(perspective, providedUnion, true);
		return lowest;
	}

	public static boolean providesEnough(DEStratum perspective, Set<? extends DEElement> provided,
			Set<? extends DEElement> required)
	{
		// if the lowest common subtypes aren't the same as the provided set, this
		// is not correct
		Set<DEElement> all = new HashSet<DEElement>(provided);
		all.addAll(required);
		return extractLowestCommonSubtypes(perspective, all, true).equals(provided);
	}

	public Set<? extends DEInterface> getProvidedInterfaces(DEStratum perspective, DEPort port)
	{
		// if this is a leaf, or it is a composite and it has setrequireds or
		// provides...
		if (!canBeDecomposed(perspective))
			return port.getSetProvidedInterfaces();

		PortPartPerspective pp = makePortPerspective(perspective, port, null);
		Set<? extends DEInterface> cached = cachedProvided.get(pp);
		if (cached != null)
			return cached;

		getInferredLinks(perspective, port);
		return cachedProvided.get(pp);
	}

	public Set<? extends DEInterface> getRequiredInterfaces(DEStratum perspective, DEPort port)
	{
		// if this is a leaf, or it is a composite and it has setrequireds or
		// provides...
		if (!canBeDecomposed(perspective))
			return port.getSetRequiredInterfaces();

		PortPartPerspective pp = makePortPerspective(perspective, port, null);
		Set<? extends DEInterface> cached = cachedRequired.get(pp);
		if (cached != null)
			return cached;

		getInferredLinks(perspective, port);
		return cachedRequired.get(pp);
	}

	public Set<DEPort> getVisitedPorts(DEStratum perspective, DEPort port)
	{
		getInferredLinks(perspective, port);
		return cachedVisitedPorts.get(makePortPerspective(perspective, port, null));
	}

	public Set<DELinkEnd> getVisitedIntoParts(DEStratum perspective, DEPort port)
	{
		getInferredLinks(perspective, port);
		return cachedVisitedIntoParts.get(makePortPerspective(perspective, port, null));
	}

	public Set<DELinkEnd> getVisitedOutOfParts(DEStratum perspective, DEPort port)
	{
		getInferredLinks(perspective, port);
		return cachedVisitedOutOfParts.get(makePortPerspective(perspective, port, null));
	}

	public Set<DELinkEnd> getVisitedTerminals(DEStratum perspective, DEPort port)
	{
		getInferredLinks(perspective, port);
		return cachedVisitedTerminals.get(makePortPerspective(perspective, port, null));
	}

	private PortPartPerspective makePortPerspective(DEStratum perspective, DEPort port, DEPart part)
	{
		return new PortPartPerspective(canBeDecomposed(perspective) ? perspective : null, port, part);
	}

	/**
	 * should have only 1 interface in each prov that matches 1 interface in each
	 * req and vice versa
	 */
	public static boolean oneToOneMappingExists(DEStratum perspective, Set<? extends DEInterface> provStart,
			Set<? extends DEInterface> reqStart, Set<? extends DEInterface> provEnd, Set<? extends DEInterface> reqEnd)
	{
		for (DEInterface prov : provStart)
			if (!hasOneMatch(perspective, prov, reqEnd))
				return false;
		// now ensure it works the other way also
		for (DEInterface req : reqEnd)
			if (!hasOneMatch(perspective, req, provStart))
				return false;

		for (DEInterface req : reqStart)
			if (!hasOneMatch(perspective, req, provEnd))
				return false;

		// now ensure it works the other way also
		for (DEInterface prov : provEnd)
			if (!hasOneMatch(perspective, prov, reqStart))
				return false;
		return true;
	}

	private static boolean hasOneMatch(DEStratum perspective, DEInterface prov, Set<? extends DEInterface> reqs)
	{
		int count = 0;

		for (DEInterface req : reqs)
		{
			if (req == prov)
				count++;
			if (prov.getSuperElementClosure(perspective, true).contains(req))
				count++;
			if (req.getSuperElementClosure(perspective, true).contains(prov))
				count++;
		}

		return count == 1;
	}

	/**
	 * only call if sure that there is one match...
	 * 
	 * @param perspective
	 * @param prov
	 * @param reqs
	 * @return
	 */
	public static DEInterface getOneMatch(DEStratum perspective, DEInterface prov, Set<? extends DEInterface> reqs)
	{
		for (DEInterface req : reqs)
		{
			if (req == prov)
				return req;
			if (prov.getInheritanceTree(perspective).contains(req))
				return req;
		}

		return null;
	}

	public List<DEAttribute> getToplogicallySortedAttributes(DEStratum perspective)
	{
		List<DEAttribute> sorted = new ArrayList<DEAttribute>();
		Set<DeltaPair> pairs = getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getConstituents(perspective);
		List<DEAttribute> attrs = new ArrayList<DEAttribute>();
		SimpleDirectedGraph<DEAttribute> graph = new SimpleDirectedGraph<DEAttribute>();
		for (DeltaPair pair : pairs)
		{
			DEAttribute attr = pair.getConstituent().asAttribute();
			attrs.add(attr);
			graph.addNode(attr);
		}

		// add edges and do a topological sort to ensure it is possible
		for (DEAttribute attr : attrs)
			for (DEParameter p : attr.getDefaultValue())
			{
				DEAttribute pAttr = p.getAttribute(perspective, this);
				if (pAttr != null && attrs.contains(pAttr))
				{
					// cannot refer to ourselves
					if (attr == pAttr)
						return sorted;  // will be empty
					graph.addEdge(attr, pAttr);
				}
			}

		return graph.makeTopologicalSort(true);
	}
	
	public boolean defaultValuesCanBeTopologicallySorted(DEStratum perspective)
	{
		return getToplogicallySortedAttributes(perspective).size() == getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getConstituents(perspective).size();
	}

	public boolean isLeaf(DEStratum perspective)
	{
		return getComponentKind() != ComponentKindEnum.NONE
				&& getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective).isEmpty();
	}

	public boolean isComposite(DEStratum perspective)
	{
		return (getComponentKind() == ComponentKindEnum.NORMAL || getComponentKind() == ComponentKindEnum.STEREOTYPE)
				&& !isLeaf(perspective);
	}

	public boolean isFactory(DEStratum perspective)
	{
		DEAppliedStereotype stereo = getAppliedStereotype(perspective);
		if (stereo == null)
			return false;
		return (getComponentKind() == ComponentKindEnum.NORMAL || getComponentKind() == ComponentKindEnum.STEREOTYPE)
				&& stereo.getBooleanProperty(FACTORY_STEREOTYPE_PROPERTY);
	}

	public boolean isLegacyBean(DEStratum perspective)
	{
		DEAppliedStereotype stereo = getAppliedStereotype(perspective);
		if (stereo == null)
			return false;
		return 
				(getComponentKind() == ComponentKindEnum.NORMAL || getComponentKind() == ComponentKindEnum.STEREOTYPE)
				&& stereo.getBooleanProperty(LEGACY_BEAN_STEREOTYPE_PROPERTY);
	}

	public boolean isPlaceholder(DEStratum perspective)
	{
		DEAppliedStereotype stereo = getAppliedStereotype(perspective);
		if (stereo == null)
			return false;
		return (getComponentKind() == ComponentKindEnum.NORMAL || getComponentKind() == ComponentKindEnum.STEREOTYPE)
				&& stereo.getBooleanProperty(PLACEHOLDER_STEREOTYPE_PROPERTY);
	}

	public DELinks getCompositeConnectorsAsLinks(DEStratum perspective)
	{
		return getCompositeConnectorsAsLinks(perspective, false);
	}

	public DELinks getCompositeConnectorsAsLinks(DEStratum perspective, boolean omitSynthetics)
	{
		DELinks cached = omitSynthetics ? cachedCompositeLinks.get(perspective) : cachedCompositeLinksAll.get(perspective);
		if (cached != null)
			return cached;

		// if this is a leaf, don't bother
		if (!canBeDecomposed(perspective))
			return new DELinks();

		// turn each connector into a port link
		cached = new DELinks();
		Set<DeltaPair> pairs = getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getConstituents(perspective, omitSynthetics);
		for (DeltaPair pair : pairs)
		{
			DEConnector conn = pair.getConstituent().asConnector();
			DEPort ports[] = new DEPort[2];
			DEPart parts[] = new DEPart[2];

			boolean haveAllPorts = true;
			for (int lp = 0; lp < 2; lp++)
			{
				ports[lp] = conn.getPort(perspective, this, lp);
				if (ports[lp] == null)
					haveAllPorts = false;
				parts[lp] = conn.getPart(perspective, this, lp);
			}
			if (haveAllPorts)
				cached.addLink(new DELinkEnd(ports[0], parts[0]), new DELinkEnd(ports[1], parts[1]));
		}
		if (omitSynthetics)
			cachedCompositeLinks.put(perspective, cached);
		else
			cachedCompositeLinksAll.put(perspective, cached);

		return cached;
	}

	public DELinks getPortLinks(DEStratum perspective)
	{
		DELinks cached = cachedPortLinks.get(perspective);
		if (cached != null)
			return cached;

		// turn each connector into a port link
		cached = new DELinks();
		Set<DeltaPair> pairs = getDeltas(ConstituentTypeEnum.DELTA_PORT_LINK).getConstituents(perspective);
		for (DeltaPair pair : pairs)
		{
			DEConnector conn = pair.getConstituent().asConnector();
			DEPort ports[] = new DEPort[2];

			for (int lp = 0; lp < 2; lp++)
				ports[lp] = conn.getPort(perspective, this, lp);
			cached.addLink(new DELinkEnd(ports[0], null), new DELinkEnd(ports[1], null));
		}
		cachedPortLinks.put(perspective, cached);

		return cached;
	}

	public boolean hasLifecycleCallbacks(DEStratum perspective)
	{
		DEAppliedStereotype stereo = getAppliedStereotype(perspective);
		if (stereo == null)
			return false;
		return stereo.getBooleanProperty(LIFECYCLE_CALLBACKS_PROPERTY);
	}

	public String getLeafProtocol(DEStratum perspective, DEComponent type)
	{
		DEAppliedStereotype stereo = getAppliedStereotype(perspective);
		if (stereo == null)
			return null;
		String protocol = stereo.getStringProperty(PROTOCOLS_PROPERTY);
		if (protocol == null || protocol.trim().length() == 0)
			return null;
		return protocol;
	}
	
	/**
	 * special port method for bean compatibility
	 * if this has only provideds, and is not indexed, then it is a main port
	 * only one port can be this, as it determines the interfaces implemented by the bean
	 * -- if there are more than one, a single  one can be forced by indicating it is the bean-main port
	 */
	public List<DEPort> getBeanMainPorts(DEStratum perspective)
	{
		List<DEPort> mains = new ArrayList<DEPort>();
		
		// see if any port is forced
		for (DeltaPair pair : getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
		{
			DEPort port = pair.getConstituent().asPort();
			
			if (port.isForceBeanMain() && !port.isForceNotBeanMain())
				mains.add(port);
		}
		
		// if we have a forced main, then just return now
		if (mains.size() > 0)
			return mains;
		
		// otherwise, look for any port with just provides
		for (DeltaPair pair : getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
		{
			DEPort port = pair.getConstituent().asPort();
			
			if (!port.isForceNotBeanMain() && getProvidedInterfaces(perspective, port).size() > 0 && !port.isMany())
				mains.add(port);
		}
		
		return mains;
	}
	
	/**
	 * special port method for bean compatibility
	 * if this has only requires, and it is indexed, then it can be a noname port, so called because it will turn into an add() method
	 * only one port can be this, it is an error to have more than one noname port
	 * -- if there are more than one, a single one can be forced by indicating it is the bean-no-name port
	 */
	public List<DEPort> getBeanNoNamePorts(DEStratum perspective)
	{
		List<DEPort> nonames = new ArrayList<DEPort>();
		
		// see if any port is forced
		for (DeltaPair pair : getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
		{
			DEPort port = pair.getConstituent().asPort();
			
			if (port.isForceBeanNoName())
				nonames.add(port);
		}

		// never force a noname port, they are ugly
		return nonames;
	}
}
