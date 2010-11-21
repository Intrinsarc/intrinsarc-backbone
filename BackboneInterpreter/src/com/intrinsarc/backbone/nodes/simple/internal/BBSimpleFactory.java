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
import com.intrinsarc.backbone.nodes.simple.*;

public class BBSimpleFactory extends BBSimpleObject
{
	private int factory;
	private transient BBSimpleComponent component;
	private List<BBSimpleAttribute> attributes;
	private List<BBSimplePart> parts;
	private List<BBSimpleConnector> connectors;
	private transient BBSimpleComponent top;
	
	public BBSimpleFactory(BBSimpleComponent top, int factory)
	{
		this.top = top;
		this.factory = factory;
	}
	
	public int getFactoryNumber()
	{
		return factory;
	}
	
	public void addAttribute(BBSimpleAttribute attr)
	{
		if (attributes == null)
			attributes = new ArrayList<BBSimpleAttribute>();
		attributes.add(attr);
	}
	
	public void addPart(BBSimplePart part)
	{
		if (parts == null)
			parts = new ArrayList<BBSimplePart>();
		parts.add(part);
	}
	
	public void addConnector(BBSimpleConnector conn)
	{
		if (connectors == null)
			connectors = new ArrayList<BBSimpleConnector>();
		connectors.add(0, conn);
	}

	public List<BBSimplePart> getParts()
	{
		return parts;
	}
	
	public List<BBSimpleAttribute> getAttributes()
	{
		return attributes;
	}
	
	public List<BBSimpleConnector> getConnectors()
	{
		return connectors;
	}

	public void resolveImplementation(BBSimpleElementRegistry registry) throws BBImplementationInstantiationException
	{
		if (attributes != null)
			for (BBSimpleAttribute attr : attributes)
				attr.resolveImplementation(registry);
		if (parts != null)
		{
			for (BBSimplePart part : parts)
				part.resolveImplementation(registry);
		}
		if (connectors != null)
			for (BBSimpleConnector conn : connectors)
				conn.resolveImplementation(registry);
	}

	public BBSimpleFactory getFactory(int factoryNumber)
	{
		return top.getFactory(factoryNumber);
	}

	public void setComponent(BBSimpleComponent component)
	{
		this.component = component;
	}
	
	public BBSimpleComponent getComponent()
	{
		return component;
	}
	
	/** sort the ends by their possible indices
	 *  NOTE: only returns internal ends -- i.e. not to the port of the component
	 **/
	public List<BBSimpleConnectorEnd> getInternalSortedConnectorEnds()
	{
		List<BBSimpleConnectorEnd> ends = new ArrayList<BBSimpleConnectorEnd>();
		
		for (BBSimpleConnector conn : connectors)
			for (int side = 0; side < 2; side++)
			{
				if (conn.makeSimpleConnectorEnd(side).getPart() != null && conn.makeSimpleConnectorEnd(1-side).getPart() != null)
					ends.add(conn.makeSimpleConnectorEnd(side));
			}

		Collections.sort(ends);		
		return ends;
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public String getRawName()
	{
		return null;
	}
	
	@Override
	public Map<String, List<? extends BBSimpleObject>> getChildren(boolean top)
	{
		Map<String, List<? extends BBSimpleObject>> children = new LinkedHashMap<String, List<? extends BBSimpleObject>>();
		List<BBSimpleObject> comp = new ArrayList<BBSimpleObject>();
		comp.add(component);
		children.put("attributes", attributes);
		children.put("parts", parts);
		children.put("connectors", connectors);
		return children;
	}

	@Override
	public String getTreeDescription()
	{
		return "SimpleFactory, id = " + factory;
	}
}
