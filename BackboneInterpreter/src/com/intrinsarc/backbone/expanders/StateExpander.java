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
package com.intrinsarc.backbone.expanders;

import java.util.*;

import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.deltaengine.base.*;

public class StateExpander
{
	public static final String STATE_STEREOTYPE_UUID = "state";
	public static final String START_COMPONENT_UUID = "Start";
	public static final String START_TERMINAL_UUID = "start";
	public static final String END_COMPONENT_UUID = "End";
	public static final String STATE_COMPONENT_UUID = "State";
	public static final String END_TERMINAL_UUID = "end";
	public static final String ITRANSITION_UUID = "ITransition";
	public static final String STATE_DISPATCHER_UUID = "StateDispatcher";
	public static final String ITERMINAL_UUID = "ITerminal";
	public static final String IEVENT_UUID = "IEvent";
	private static final String DSTART_UUID = "dStart";
	private static final String DEND_UUID = "dEnd";
	
	public void expand(DEStratum perspective, DEElement element)
	{
		DEComponent c = element.asComponent();
		if (c == null)
			return;
		DEAppliedStereotype stereo = c.getAppliedStereotype(perspective);
		if (stereo == null || !stereo.getStereotype().getUuid().equals(STATE_STEREOTYPE_UUID))
			return;
		// must have some parts
		if (c.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective).isEmpty())
			return;

		// add in some state dispatchers
		Map<DEInterface, DEPart> dispatchers = new HashMap<DEInterface, DEPart>();
		DEComponent dispatcherType = GlobalDeltaEngine.engine.locateObjectForStereotype(STATE_DISPATCHER_UUID).asComponent(); 
		List<DeltaPair> dispatcherEventProvPorts = findPorts(perspective, dispatcherType, IEVENT_UUID, null);
		for (DeltaPair pair : findRawPorts(perspective, c, IEVENT_UUID, null))
		{
			BBPart dispatcher = new BBPart(newUUID(), true, false);
			dispatcher.setName("dispatcher");
			dispatcher.setType(dispatcherType);
			DeltaPair dispatcherPair = new DeltaPair(dispatcher.getUuid(), dispatcher);
			element.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective).add(dispatcherPair);
			
			// connect the dispatcher up to the port
			List<DeltaPair> ports = new ArrayList<DeltaPair>();
			ports.add(pair);
			connectUp(perspective,
					c,
					ports,
					null,
					dispatcherEventProvPorts,
					dispatcher);
			DEInterface iface = pair.getConstituent().asPort().getSetProvidedInterfaces().iterator().next();
			dispatchers.put(iface, dispatcher);
		}
		
		// get the relevant ports
		List<DeltaPair> inPorts = findRawPorts(perspective, c, ITRANSITION_UUID, null);
		List<DeltaPair> outPorts = findRawPorts(perspective, c, null, ITRANSITION_UUID);
		
		// connect up the in port of any start to the in port of the composite
		// do the same with the ends
		List<DeltaPair> dispatcherEventReqPorts = findPorts(perspective, dispatcherType, null, IEVENT_UUID);
		for (DeltaPair pair : element.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective))		
		{
			DEPart part = pair.getConstituent().asPart();
			DEPart original = pair.getOriginal().asPart();
			DEComponent type = part.getType();
			if (type == null)
				continue;
			
			if (descendsFrom(perspective, type, START_COMPONENT_UUID))
			{
				List<DeltaPair> startInPartPorts = findPorts(perspective, type, ITRANSITION_UUID, null);
				connectUp(perspective, c, startInPartPorts, original, inPorts, null);
				
				// connect this back to the dispatchers
				for (DEPart dispatcher : dispatchers.values())
					connectUp(perspective,
							c,
							findPorts(perspective, type, ITERMINAL_UUID, null),
							original,
							findPortsByUUID(perspective, dispatcherType, DSTART_UUID),
							dispatcher);
			}
			else
			if (descendsFrom(perspective, type, END_COMPONENT_UUID))
			{
				List<DeltaPair> endOutPartPorts = findPorts(perspective, type, null, ITRANSITION_UUID);
				endOutPartPorts = weedOutAlreadyConnected(perspective, c, part, endOutPartPorts);
				connectUp(perspective, c, endOutPartPorts, original, outPorts, null);

				// connect this back to the dispatchers
				for (DEPart dispatcher : dispatchers.values())
					connectUp(perspective,
							c,
							findPorts(perspective, type,  ITERMINAL_UUID, null),
							original,
							findPortsByUUID(perspective, dispatcherType, DEND_UUID),
							dispatcher);
			}
			else
			if (descendsFrom(perspective, type, STATE_COMPONENT_UUID))
			{
				
				// now connect each state to the appropriate dispatchers
				List<DeltaPair> stateEventPorts = findPorts(perspective, type, IEVENT_UUID, null);
				List<DeltaPair> realStateEventPorts = findPorts(perspective, type, IEVENT_UUID, null);
				for (DeltaPair s : realStateEventPorts)
				{
					DEInterface provided = s.getConstituent().asPort().getSetProvidedInterfaces().iterator().next();
					DEPart dispatcher = dispatchers.get(provided);
					if (dispatcher != null)
					{
						connectUp(perspective,
								c,
								stateEventPorts,
								original,
								dispatcherEventReqPorts,
								dispatcher);
					}
				}
			}
		}
	}
	
	private List<DeltaPair> weedOutAlreadyConnected(DEStratum perspective, DEComponent c, DEPart part, List<DeltaPair> ports)
	{
		List<DeltaPair> retPorts = new ArrayList<DeltaPair>();
		DELinks links = c.getPortLinks(perspective);
		for (DeltaPair p : ports)
			if (links.filterByStart(new DELinkEnd(p.getOriginal().asPort(), part)).isEmpty())
				retPorts.add(p);
		return retPorts;
	}

	private void connectUp(DEStratum perspective, DEComponent c, List<DeltaPair> one, DEPart onePart, List<DeltaPair> two, DEPart twoPart)
	{
		Set<DeltaPair> deltas = c.getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getConstituents(perspective);
		for (DeltaPair p1 : one)
			for (DeltaPair p2 : two)
			{
				BBConnector conn = new BBConnector(newUUID(), true);
				conn.setFromPart(onePart);
				conn.setFromPort(p1.getOriginal().asPort());
				conn.setFromTakeNext(p1.getConstituent().asPort().getUpperBound() == -1);
				conn.setToPort(p2.getOriginal().asPort());
				conn.setToPart(twoPart);
				conn.setToTakeNext(p2.getConstituent().asPort().getUpperBound() == -1);
				deltas.add(new DeltaPair(conn.getUuid(), conn));
			}
	}

	private List<DeltaPair> findRawPorts(DEStratum perspective, DEComponent c, String provInterfaceUuid, String reqInterfaceUuid)
	{
		List<DeltaPair> ports = new ArrayList<DeltaPair>();
		if (c == null)
			return ports;
		for (DeltaPair pair : c.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
		{
			DEPort port = pair.getConstituent().asPort();
			
			// check the prov first
			if (matchUp(perspective, provInterfaceUuid, port.getSetProvidedInterfaces()) &&
					matchUp(perspective, reqInterfaceUuid, port.getSetRequiredInterfaces()))
			{
				ports.add(pair);
			}
		}
		return ports;
	}

	private List<DeltaPair> findPortsByUUID(DEStratum perspective, DEComponent c, String uuid)
	{
		List<DeltaPair> ports = new ArrayList<DeltaPair>();
		if (c == null)
			return ports;
		for (DeltaPair pair : c.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
		{
			if (pair.getUuid().equals(uuid))
				ports.add(pair);
		}
		return ports;
	}

	private List<DeltaPair> findPorts(DEStratum perspective, DEComponent c, String provInterfaceUuid, String reqInterfaceUuid)
	{
		List<DeltaPair> ports = new ArrayList<DeltaPair>();
		if (c == null)
			return ports;
		for (DeltaPair pair : c.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
		{
			DEPort port = pair.getConstituent().asPort();
			// check the prov first
			if (matchUp(perspective, provInterfaceUuid, c.getProvidedInterfaces(perspective, port)) &&
					matchUp(perspective, reqInterfaceUuid, c.getRequiredInterfaces(perspective, port)))
				ports.add(pair);
		}
		return ports;
	}

	private boolean matchUp(DEStratum perspective, String interfaceUUID, Set<? extends DEInterface> ifaces)
	{
		if (ifaces.size() == 0)
			return interfaceUUID == null;
		
		for (DEInterface i : ifaces)
			if (!descendsFrom(perspective, i, interfaceUUID))
				return false;
		return true;
	}

	/**
	 * does this descend from, via resemblance, the component with the given uuid
	 * @param perspective
	 * @param element
	 * @param uuid
	 * @return
	 */
	private boolean descendsFrom(DEStratum perspective, DEElement element, String uuid)
	{
		if (element.getUuid().equals(uuid))
			return true;
		for (DEElement e : element.getFilteredResemblance_eClosure(perspective))
			if (e.getUuid().equals(uuid))
				return true;
		return false;
	}

	private String newUUID()
	{
		return "synthetic-" + UUID.randomUUID();
	}
}
