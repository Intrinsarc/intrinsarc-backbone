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
package com.intrinsarc.backbone.nodes.simple.internal;

import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.simple.*;
import com.intrinsarc.backbone.runtime.api.*;
import com.intrinsarc.deltaengine.base.*;


public class BBSimpleInstantiatedFactory
{
	private BBSimpleInstantiatedFactory parent;
	private BBSimpleFactory factory;
	private Map<BBSimpleAttribute, Object> iattributes = new HashMap<BBSimpleAttribute, Object>();
	private Map<BBSimplePart, Object> iparts = new HashMap<BBSimplePart, Object>();
	private Map<CachedConnectorEnd, Map<BBSimpleInterface, Object>> cachedProvides = new HashMap<CachedConnectorEnd, Map<BBSimpleInterface,Object>>();
	private ArrayList<BBSimpleInstantiatedFactory> children;
	private DEStratum perspective;
	
	public BBSimpleInstantiatedFactory(DEStratum perspective, BBSimpleInstantiatedFactory parent, BBSimpleFactory factory)
	{
		this.perspective = perspective;
		if (perspective == null && parent != null)
			this.perspective = parent.perspective;
		this.parent = parent;
		if (parent != null)
			parent.addChild(this);
		this.factory = factory;
	}
	
	private void addChild(BBSimpleInstantiatedFactory child)
	{
		if (children == null)
			children = new ArrayList<BBSimpleInstantiatedFactory>();
		children.add(child);
	}
	
	private void childDestroyed(BBSimpleInstantiatedFactory child)
	{
		children.remove(child);
	}
	
	public void instantiate(Map<String, Object> suppliedParameters, IRuntimeCallback callback) throws BBRuntimeException
	{
		Set<String> setNames = suppliedParameters == null ? null : new HashSet<String>(suppliedParameters.keySet());
		
		// instantiate the attributes, possibly taking the values from the parameters supplied
		List<BBSimpleAttribute> attrs = factory.getAttributes();
		if (attrs != null)
			for (BBSimpleAttribute attr : attrs)
			{
				iattributes.put(attr, attr.instantiate(this, suppliedParameters, setNames));
			}
		
		// we shouldn't have any names left over
		if (setNames != null && setNames.size() != 0)
			throw new BBRuntimeException("Found extra values " + setNames + " when instantiating factory " + factory, null);			
		
		// instantiate the parts in reverse order
		List<BBSimplePart> parts = factory.getParts();

		for (int lp = parts.size() - 1; lp >= 0; lp--)
		{
			BBSimplePart part = parts.get(lp);
			
			// instantiate the part
			Object obj = part.getType().makeNewInstance(this);
			iparts.put(part, obj);
			
			// set up the attributes
			for (BBSimpleSlot s : part.getSlots())
			{
				Object simple = null;
				if (iattributes.containsKey(s.getEnvironmentAlias()))
				{
					simple = iattributes.get(s.getEnvironmentAlias());
				}
				else
					simple = parent.resolveAttributeValue(s.getEnvironmentAlias());
				s.setValue(obj, simple, part.getType());
			}
		}
		
		if (callback != null)
		callback.beforeConnections(this);
		
		// connect up all the parts
		// must connect this in a way which allows numbered indices first, on both sides
		// this means breaking apart the provided and required sides

		// cache the provided for each port
		List<BBSimpleConnectorEnd> ends = factory.getInternalSortedConnectorEnds();
		for (BBSimpleConnectorEnd end : ends)
		{
			Map<BBSimpleInterface, Object> cached = end.getConnector().getProvides(this, end.getSide());
			if (cached != null)
			{
				cachedProvides.put(new CachedConnectorEnd(end.getConnector(), end.getSide()), cached);
			}
		}
		
		// set the required for each port
		for (BBSimpleConnectorEnd end : ends)
		{
			Map<BBSimpleInterface, Object> cache = cachedProvides.get(new CachedConnectorEnd(end.getConnector(), 1 - end.getSide()));
			if (cache != null && !cache.keySet().isEmpty())
			{
				end.getConnector().setRequires(this, cache, end.getSide());
			}
		}
			
		// inform any lifecycle parts that we have initialized
		for (BBSimplePart p : iparts.keySet())
			if (p.getType().hasLifecycleCallbacks())
				((ILifecycle) iparts.get(p)).afterInit();
	}
	
	public Object resolveAttributeValue(BBSimpleAttribute attr) throws BBRuntimeException
	{
		Object value = iattributes.get(attr);
		if (value != null)
			return value;
		if (parent == null)
			throw new BBRuntimeException("Cannot find attribute value for " + attr, attr.getOwner());
		return parent.resolveAttributeValue(attr);
	}

	public Object getPartObject(BBSimplePart part) throws BBRuntimeException
	{
		Object actual = iparts.get(part);
		if (actual != null)
			return actual;
		if (parent == null)
			throw new BBRuntimeException("Cannot find part for " + part, part.getType());
		return parent.getPartObject(part);
	}

	public Object getPartObject(DEPart runPart) throws BBRuntimeException
	{
		for (BBSimplePart part : iparts.keySet())
			if (part.getComplexPart() == runPart)
				return iparts.get(part);
		throw new BBRuntimeException("Cannot find part for " + runPart, null);
	}

	public BBSimpleFactory getFactory(int factoryNumber)
	{
		return factory.getFactory(factoryNumber);
	}

	public <T> boolean setRequiredPort(String portName, Class<T> requiredInterface, T provided) throws BBRuntimeException
	{
		// locate the connector that is connected to this port
		for (BBSimpleConnector conn : factory.getConnectors())
		{
			for (int lp = 0; lp < 2; lp++)
			{
				BBSimpleConnectorEnd end = conn.makeSimpleConnectorEnd(lp);
				if (end.getPart() == null && end.getPort().getRawName().equals(portName))
				{
					BBSimpleConnectorEnd other = conn.makeSimpleConnectorEnd(1 - lp);
					BBSimplePart part = other.getPart();
					Object object = iparts.get(part);
					ReflectivePort field = null;
					try
					{
						field = BBSimpleConnector.getPortField(
							perspective, part.getType(), other.getPort(), requiredInterface, false);
					}
					catch (BBImplementationInstantiationException ex)
					{
						throw new BBRuntimeException("Instantiation problem finding interface " + requiredInterface + " on port " + portName, null, ex);						
					}
					if (field != null)
					{
						field.setSingle(object, provided);
						return true;
					}
				}
			}
		}
		// if we got here, we couldn't find the port
		throw new BBRuntimeException("Problem finding interface " + requiredInterface + " on port " + portName, null);
	}
	
	public void runViaPort(BBSimplePort provider, String[] args) throws BBRuntimeException, BBImplementationInstantiationException
	{
		// locate the connector that is connected to this port
		for (BBSimpleConnector conn : factory.getConnectors())
		{
			int end = conn.getEndNumber(null, provider);
			if (end != -1)
			{
				BBSimpleConnectorEnd connEnd = conn.makeSimpleConnectorEnd(1 - end);
				BBSimplePart part = connEnd.getPart();
				Object object = iparts.get(part);
				ReflectivePort field = BBSimpleConnector.getPortField(
						perspective, part.getType(), connEnd.getPort(), IRun.class, true);
				if (((IRun) field.getSingle(IRun.class, object)).run(args))
					destroy();
			}
		}
	}

	public void destroy() throws BBRuntimeException
	{
		// tell the parent
		if (parent != null)
			parent.childDestroyed(this);
		
		// clear any children in reverse order
		if (children != null)
		{
			int size = children.size();
			List<BBSimpleInstantiatedFactory> copy = new ArrayList<BBSimpleInstantiatedFactory>(children);
			for (int lp = size - 1; lp >= 0; lp--)
				copy.get(0).destroy();
		}
		
		// inform any lifecycle parts that we are about to delete them
		for (BBSimplePart p : iparts.keySet())
			if (p.getType().hasLifecycleCallbacks())
				((ILifecycle) iparts.get(p)).beforeDelete();		
		
		// clear out any connectors to detach this from the rest of the network and effectively destroy it
		// set the required for each port
		Set<BBSimplePort> ports = new HashSet<BBSimplePort>();
		for (BBSimpleConnector c : factory.getConnectors())
			for (int lp = 0; lp < 2; lp++)
				ports.add(c.makeSimpleConnectorEnd(lp).getPort());

		for (int pass = 0; pass < 2; pass++)
			for (BBSimplePort port : ports)
				for (int side = 0; side < 2; side++)
				{
					for (BBSimpleConnector conn : factory.getConnectors())
					{
						if (!conn.isRunConnector())
						{
							BBSimpleConnectorEnd end = conn.makeSimpleConnectorEnd(side);
							BBSimpleConnectorEnd otherEnd = conn.makeSimpleConnectorEnd(1 - side);
							
							Map<BBSimpleInterface, Object> cache = cachedProvides.get(new CachedConnectorEnd(end.getConnector(), 1 - end.getSide()));
							if (end.getPort() == port && (pass == 0 && !end.isTakeNext() || pass == 1 && end.isTakeNext()))
							{
								end.getConnector().clearRequires(this, cache, end.getSide());
								otherEnd.getConnector().clearProvides(this, cache, otherEnd.getSide());
							}
						}
					}
				}
	}
}
