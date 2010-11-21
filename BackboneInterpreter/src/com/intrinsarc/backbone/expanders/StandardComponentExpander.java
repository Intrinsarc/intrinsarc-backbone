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

public class StandardComponentExpander
{
	public void expand(DEStratum perspective, DEElement element)
	{
		DEComponent c = element.asComponent();
		if (c == null)
			return;

		// must be composite
		if (!c.isComposite(perspective))
			return;
		
		// get any unconnected autoconnect ports
		DELinks links = c.getCompositeConnectorsAsLinks(perspective);
		Set<DeltaPair> autos = new HashSet<DeltaPair>();
		for (DeltaPair pair : c.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
		{
			DEPort port = pair.getConstituent().asPort();
			if (port.getPortKind() == PortKindEnum.AUTOCONNECT && links.getEnds(new DELinkEnd(port)).isEmpty())
				autos.add(pair);
		}
		if (autos.isEmpty())
			return;
		
		// find any non-bean parts that have unconnected ports
		Set<DeltaPair> deltas = c.getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getConstituents(perspective);
		for (DeltaPair pair : c.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective))
		{
			DEPart part = pair.getConstituent().asPart();
			DEComponent type = part.getType();
			if (type != null && !type.isLegacyBean(perspective))
				for (DeltaPair pPair : part.getType().getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
					// getConstituent() here should ideally be getOriginal()
					if (links.getEnds(new DELinkEnd(pPair.getConstituent().asPort(), pair.getOriginal().asPart())).isEmpty())
					{
						Set<? extends DEInterface> required = type.getRequiredInterfaces(perspective, pPair.getConstituent().asPort());
						Set<? extends DEInterface> provided = type.getProvidedInterfaces(perspective, pPair.getConstituent().asPort());
						
						// connect up to the best autoconnect port
						DeltaPair best = null;
						for (DeltaPair auto : autos)
						{
							DEPort autoPort = auto.getConstituent().asPort();
							
							// must match the current port and be better than the last one
							if (DEComponent.oneToOneMappingExists(
									perspective,
									autoPort.getSetProvidedInterfaces(),
									autoPort.getSetRequiredInterfaces(),
									required,
									provided) &&
									DEComponent.providesEnough(
											perspective,
											c.getProvidedInterfaces(perspective, autoPort),
											provided) &&
									DEComponent.providesEnough(
											perspective,
											required,
											c.getRequiredInterfaces(perspective, autoPort)))
							{
								// this may be a match
								if (best == null)
								{
									best = auto;
									break;
								}
								else
								{
									if (isBetterThan(perspective, auto.getConstituent().asPort(), best.getConstituent().asPort()))
										best = auto;
								}
							}
						}

						if (best != null)
						{
							BBConnector conn = new BBConnector(newUUID(), true);
							conn.setFromPort(best.getOriginal().asPort());
							conn.setFromTakeNext(best.getConstituent().asPort().getUpperBound() == -1);
							conn.setToPort(pPair.getOriginal().asPort());
							conn.setToPart(pair.getOriginal().asPart());
							conn.setToTakeNext(pPair.getConstituent().asPort().getUpperBound() == -1);
							deltas.add(new DeltaPair(conn.getUuid(), conn));
						}
					}
		}
	}
	
	private boolean isBetterThan(DEStratum perspective, DEPort next, DEPort last)
	{
		Set<DEElement> req = new HashSet<DEElement>(last.getSetRequiredInterfaces());
		Set<DEElement> prov = new HashSet<DEElement>(next.getSetProvidedInterfaces());

		// ensure we have the highest required from the start
		for (DEElement ifaceO : new HashSet<DEElement>(req))
		{
			Set<DEElement> supers = ifaceO.getSuperElementClosure(perspective, true);
			for (DEInterface iface : next.getSetRequiredInterfaces())
				if (ifaceO == iface || supers.contains(iface))
					req.remove(ifaceO);									
		}
		// ensure we have the lowest provided from the start
		for (DEElement iface : new HashSet<DEElement>(prov))
		{
			Set<DEElement> supers = iface.getSuperElementClosure(perspective, true);
			for (DEElement ifaceO : last.getSetProvidedInterfaces())
				if (ifaceO == iface || supers.contains(ifaceO))
					prov.remove(iface);
		}
		
		return req.isEmpty() && !prov.isEmpty();
	}

	private String newUUID()
	{
		return "synthetic-" + UUID.randomUUID();
	}
}
