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
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.simple.internal.*;
import com.intrinsarc.deltaengine.base.*;

class PortFieldMap extends HashMap<BBSimpleInterface, ReflectivePort>{}
class ConstructorFieldMap extends HashMap<BBSimpleInterface, Constructor<?>>{}

public class BBSimpleConnector extends BBSimpleObject
{
	private String name;
	private transient String rawName;
	private Boolean delegate;
	private String originalIndex[];
	private Integer index[];
	private boolean takeNext[];
	private BBSimplePort ports[] = new BBSimplePort[2];
	private BBSimplePart parts[] = new BBSimplePart[2];
  private boolean resolved;
  private PortFieldMap provPortFields[] = new PortFieldMap[]{new PortFieldMap(), new PortFieldMap()};
  private PortFieldMap reqPortFields[] = new PortFieldMap[]{new PortFieldMap(), new PortFieldMap()};
  private boolean hyperConnector;
  private DEConnector complex;

  public DEConnector getComplex() { return complex; }
	public BBSimpleConnector(
			BBSimpleElementRegistry registry,
			DEComponent component,
			DEConnector complex,
			List<BBSimplePart> simpleParts,
			List<BBSimplePort> simplePorts,
			BBSimpleComponent owner)
	{
		this.complex = complex;
		rawName = complex.getName();
		name = registry.makeName(rawName);
		if (complex.isDelegate())
			delegate = true;
		
		if (complex.getIndex(0) != null || complex.getIndex(1) != null)
		{
			originalIndex = new String[2];
			for (int lp = 0; lp < 2; lp++)
				originalIndex[lp] = complex.getIndex(lp);
		}
		
		for (int lp = 0; lp < 2; lp++)
		{
			parts[lp] = translatePart(simpleParts, complex.getPart(registry.getPerspective(), component, lp));
			ports[lp] = translatePort(simplePorts, parts[lp], complex.getPort(registry.getPerspective(), component, lp));
		}

		if (takeNext(complex, 0) || takeNext(complex, 1))
		{
			takeNext = new boolean[2];
			for (int lp = 0; lp < 2; lp++)
				takeNext[lp] = takeNext(complex, lp);
		}
		
		// translate originalindex into takenext and index
		if (originalIndex != null)
			for (int lp = 0; lp < 2; lp++)
			{
				int ind = translateOriginalIndex(lp);
				
				if (ind == -1)
				{
					// ensure takenext is set
					if (takeNext == null)
						takeNext = new boolean[2];
					takeNext[lp] = true;
				}
				else
				{
					// ensure index is set
					if (index == null)
						index = new Integer[2];
					index[lp] = ind;
				}
			}
	}

	private int translateOriginalIndex(int ind)
	{
		if (originalIndex == null)
			return -1;
		String str = originalIndex[ind];
		if (str == null)
			return -1;
		
		int size = str.length();
		for (int lp = 0; lp < size; lp++)
			if (!Character.isDigit(str.charAt(lp)))
				return -1;
		return new Integer(str);
	}

	public BBSimpleConnector(BBSimpleElementRegistry registry, BBSimpleConnector original, Map<BBSimplePart, BBSimplePart> partTranslationMap)
	{
		copyFromOther(original);
		
		// translate any parts
		for (int lp = 0; lp < 2; lp++)
			parts[lp] = partTranslationMap.get(original.parts[lp]);
	}

	/**
	 * copy the other connector, and replace the end that attaches to the component (partEnd = false)
	 * or the end that attaches to the part (partEnd = true)
	 * @param newPart
	 * @param newPort
	 * @param factory 
	 * @param other
	 * @param internal
	 */
	public BBSimpleConnector(BBSimpleConnector original, BBSimplePart matchPart, BBSimplePort matchPort, BBSimplePart newPart, BBSimplePort newPort)
	{
		copyFromOther(original);
		
		// change over any ends that match
		for (int lp = 0; lp < 2; lp++)
			if (parts[lp] == matchPart && ports[lp] == matchPort)
			{
				ports[lp] = newPort;
				parts[lp] = newPart;
			}
	}

	/**
	 * create a hyperconnector
	 */
	public BBSimpleConnector(BBSimplePart part1, BBSimplePort port1, BBSimplePart part2, BBSimplePort port2)
	{
		parts[0] = part1;
		ports[0] = port1;
		parts[1] = part2;
		ports[1] = port2;
		hyperConnector = true;
		
		// possibly create takenext
		if (ports[0].isIndexed() || ports[1].isIndexed())
			takeNext = new boolean[]{ports[0].isIndexed(), ports[1].isIndexed()};
	}
	
	private boolean takeNext(DEConnector complex, int ind)
	{
		// allows us to handle and flatten even structures with connector errors...
		if (ports[ind] == null)
			return false;
		if (complex.getTakeNext(ind))
			return true;
		// otherwise, look to see if we have no connector index, and the port has multiplicity
		return indexed(ports[ind]) && (originalIndex == null || originalIndex[ind] == null);
	}

	public boolean isHyperConnector()
	{
		return hyperConnector;
	}

	private void copyFromOther(BBSimpleConnector original)
	{
		// copy from the original connector
		complex = original.complex;
		takeNext = original.takeNext;
		originalIndex = original.originalIndex;
		index = original.index;
		name = original.name;
		delegate = original.delegate;

		for (int lp = 0; lp < 2; lp++)
		{
			parts[lp] = original.parts[lp];
			ports[lp] = original.ports[lp];
		}
	}

	private BBSimplePort translatePort(List<BBSimplePort> simplePorts, BBSimplePart part, DEPort port)
	{
		if (port == null)
			return null;
		if (part == null)
		{
			for (BBSimplePort p : simplePorts)
				if (p.getComplexPort() == port)
					return p;
		}
		else
		{
			for (BBSimplePort p : part.getType().getPorts())
				if (p.getComplexPort() == port)
					return p;
		}
		throw new IllegalStateException("Couldn't find the correct port");
	}

	private BBSimplePart translatePart(List<BBSimplePart> simpleParts, DEPart part)
	{
		if (part == null)
			return null;
		for (BBSimplePart p : simpleParts)
			if (p.getComplexPart() == part)
				return p;
		throw new IllegalStateException("Couldn't find the correct part");
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

	/** connects 2 parts */
	public boolean isInternal()
	{
		if (parts == null)
			return false;
		return parts[0] != null && parts[1] != null;
	}

	public int getEndNumber(BBSimplePart part, BBSimplePort port)
	{
		for (int lp = 0; lp < 2; lp++)
			if (parts[lp] == part && ports[lp] == port)
				return lp;
		return -1;
	}

	public Integer getIndex(int end)
	{
		if (index == null)
			return null;
		return index[end];
	}

	public boolean isDelegate()
	{
		if (delegate == null)
			return false;
		return delegate;
	}

	public BBSimpleConnectorEnd makeSimpleConnectorEnd(int end)
	{
		Integer ind = index == null ? null : index[end];
		String orig = originalIndex == null ? null : originalIndex[end];
		boolean tn = takeNext == null ? false : takeNext[end];
		return new BBSimpleConnectorEnd(this, end, parts[end], ports[end], ind, orig, tn);
	}

	public int getFactory()
	{
		if (parts[0]  == null || parts[1] == null)
			return 0;
		return Math.max(parts[0].getFactory(), parts[1].getFactory());
	}

	public void resolveImplementation(BBSimpleElementRegistry registry) throws BBImplementationInstantiationException
	{
		if (resolved)
			return;
		resolved = true;
		
		
		// resolve the port fields on either side
		for (int lp = 0; lp < 2; lp++)
		{
			if (parts[lp] == null)
				return;  // fix later

			for (BBSimpleInterface i : ports[lp].getRequires())
			{
				ReflectivePort field = getPortField(registry.getPerspective(), parts[lp].getType(), ports[lp], i.getImplementationClass(), false);
				reqPortFields[lp].put(i, field);
			}
			for (BBSimpleInterface i : ports[lp].getProvides())
			{
				ReflectivePort field = getPortField(registry.getPerspective(), parts[lp].getType(), ports[lp], i.getImplementationClass(), true);
				provPortFields[lp].put(i, field);
			}
		}
	}

	public static ReflectivePort getPortField(
			DEStratum perspective, BBSimpleComponent type, BBSimplePort simplePort, Class<?> ifaceClass, boolean provided) throws BBImplementationInstantiationException
	{
	  return 
	    new ReflectivePort(
	    	perspective,
	    	type,
	      simplePort,
	      ifaceClass,
	      !provided);
	}

	public Map<BBSimpleInterface, Object> getProvides(BBSimpleInstantiatedFactory context, int provSide) throws BBRuntimeException
	{
		// don't bother if we have no provided interfaces on this side
		if (ports[provSide].getProvides().isEmpty())
			return null;
		
		// get the 2 objects involved
		Object partObjects[] = new Object[2];
		for (int lp = 0; lp < 2; lp++)
			partObjects[lp] = context.getPartObject(parts[lp]);
		
		// get the mapping
		Map<BBSimpleInterface, Object> map = new HashMap<BBSimpleInterface, Object>();
		for (BBSimpleInterface i : provPortFields[provSide].keySet())
		{
			// get the appropriate required interface that the provides must support
			map.put(i,
					getProvided(
							parts[provSide].getRequiredImplementationForProvided(ports[provSide], i),
							i,
							provPortFields[provSide].get(i),
							partObjects[provSide],
							provSide));
		}
		return map;
	}

	/**
	 * returns true if all provides were available, otherwise returns false
	 * @param context
	 * @param cachedProvides
	 * @param reqSide
	 * @return
	 * @throws BBRuntimeException
	 */
	public boolean setRequires(BBSimpleInstantiatedFactory context, Map<BBSimpleInterface, Object> cachedProvides, int reqSide) throws BBRuntimeException
	{
		// get the 2 objects involved
		Object partObjects[] = new Object[2];
		for (int lp = 0; lp < 2; lp++)
			partObjects[lp] = context.getPartObject(parts[lp]);

		for (BBSimpleInterface i : provPortFields[1 - reqSide].keySet())
			setRequired(i, getPossibleSubInterfaceField(reqPortFields[reqSide], i), partObjects[reqSide], reqSide, cachedProvides.get(i));
		return true;
	}

	public void clearRequires(BBSimpleInstantiatedFactory context, Map<BBSimpleInterface, Object> cachedProvides, int reqSide) throws BBRuntimeException
	{
		// get the 2 objects involved
		Object partObjects[] = new Object[2];
		for (int lp = 0; lp < 2; lp++)
			partObjects[lp] = context.getPartObject(parts[lp]);

		for (BBSimpleInterface i : provPortFields[1 - reqSide].keySet())
			clearRequired(i, getPossibleSubInterfaceField(reqPortFields[reqSide], i), partObjects[reqSide], reqSide, cachedProvides.get(i));
	}
	
	public void clearProvides(BBSimpleInstantiatedFactory context, Map<BBSimpleInterface, Object> cachedProvides, int provSide) throws BBRuntimeException
	{
		// get the 2 objects involved
		Object partObjects[] = new Object[2];
		for (int lp = 0; lp < 2; lp++)
			partObjects[lp] = context.getPartObject(parts[lp]);

		for (BBSimpleInterface i : provPortFields[provSide].keySet())
			clearProvided(i, provPortFields[provSide].get(i), partObjects[provSide], provSide, cachedProvides.get(i));
	}
	
	private ReflectivePort getPossibleSubInterfaceField(PortFieldMap reqFieldMap, BBSimpleInterface provided)
	{
		for (BBSimpleInterface i : provided.getInheritanceTree())
			if (reqFieldMap.get(i) != null)
				return reqFieldMap.get(i);

		// do this the other way also -- we need to in case of type inference the provides may not be a subclass
		for (BBSimpleInterface i : reqFieldMap.keySet())
			if (i.getInheritanceTree().contains(provided))
				return reqFieldMap.get(i);
		throw new IllegalStateException("Cannot find suitable match for provided interface " + provided.getName() + " in connector " + name);
	}

	public boolean isRunConnector()
	{
		for (int lp = 0; lp < 2; lp++)
		{
			if (parts[lp] == null)
				// ignore this -- this is the connector to the run port
				return true;
		}
		return false;
	}

	private void clearRequired(BBSimpleInterface iface, ReflectivePort reqField, Object required, int reqIndex, Object provided) throws BBRuntimeException
	{
		BBSimplePort port = ports[reqIndex];
		if (!indexed(port))
		  reqField.setSingle(required, null);
		else
			reqField.remove(required, provided);
	}

	private void clearProvided(BBSimpleInterface iface, ReflectivePort provField, Object provided, int provIndex, Object providedObject) throws BBRuntimeException
	{
		BBSimplePort port = ports[provIndex];
		if (indexed(port))
			provField.remove(provided, providedObject);
	}

	private void setRequired(BBSimpleInterface iface, ReflectivePort reqField, Object required, int reqIndex, Object provided) throws BBRuntimeException
	{
		BBSimplePort port = ports[reqIndex];
		if (!indexed(port))
		  reqField.setSingle(required, provided);
		else
		{
			if (takeNext != null && takeNext[reqIndex])
				reqField.addIndexed(required, provided, -1);
			else
				reqField.addIndexed(required, provided, index[reqIndex]);
		}
	}

	private Object getProvided(Class<?> required, BBSimpleInterface iface, ReflectivePort provField, Object provided, int provIndex) throws BBRuntimeException
	{
		BBSimplePort port = ports[provIndex];
		// if this port isn't indexed, it's easy
		if (!indexed(port))
		  return provField.getSingle(required, provided);
		else
		{
			if (takeNext != null && takeNext[provIndex])
				return provField.getIndexed(required, provided, -1);
		  return provField.getIndexed(required, provided, index[provIndex]);
		}
	}

	private static boolean indexed(BBSimplePort port)
	{
		// -1 signifies unlimited upper bound
		return port.getUpperBound() > 1 || port.getUpperBound() == -1;
	}
	
	@Override
	public Map<String, List<? extends BBSimpleObject>> getChildren(boolean top)
	{
		Map<String, List<? extends BBSimpleObject>> children = new LinkedHashMap<String, List<? extends BBSimpleObject>>();
		children.put("from", makeList(ports[0], parts[0]));
		children.put("to", makeList(ports[1], parts[1]));
		return children;
	}

	private List<? extends BBSimpleObject> makeList(BBSimpleObject obj1, BBSimpleObject obj2)
	{
		List<BBSimpleObject> t = new ArrayList<BBSimpleObject>();
		t.add(obj1);
		t.add(obj2);
		return t;
	}
	
	@Override
	public String getTreeDescription()
	{
		String desc =
			"Connector " + name;
		if (hyperConnector)
			desc += ", hyper";
		if (index != null)
			desc += ", from index = " + index[0] + ", to index = " + index[1];
		if (takeNext != null)
			desc += ", from take next = " + takeNext[0] + ", to take next = " + takeNext[1];
		return desc;
	}
}
