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
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.deltaengine.base.*;

public class BBSimpleSlot extends BBSimpleObject
{
	private BBSimpleAttribute attribute;
	private BBSimpleAttribute environmentAlias;
	private List<BBSimpleParameter> value;
	private boolean resolved;
	private ReflectiveAttribute reflectiveField;
	
	public BBSimpleSlot(BBSimpleElementRegistry registry, BBSimpleComponent component, DEComponent partType, DESlot complex)
	{
		value = new ArrayList<BBSimpleParameter>();
		for (DEParameter p : complex.getValue())
			if (p.getAttribute() != null)
			{
				BBSimpleAttribute simple =
					translateAttribute(component.getAttributes(), p.getAttribute(registry.getPerspective(), component.getComplex()));
				if (simple != null)
					value.add(new BBSimpleParameter(simple));
			}
			else
			{
				value.add(new BBSimpleParameter(p.getLiteral()));
			}

		// the attribute must be in the type of the part
		BBSimpleComponent partComp = registry.retrieveComponent(partType);
		DEAttribute complexAttr = complex.getAttribute(registry.getPerspective(), partComp.getComplex());
		for (BBSimpleAttribute attr : partComp.getAttributes())
		{
			if (attr.getComplex() == complexAttr)
			{
				attribute = attr;
				break;
			}
		}
	}

	public BBSimpleSlot(BBSimpleElementRegistry registry, BBSimpleAttribute attribute, BBSimpleAttribute environmentAlias)
	{
		this.attribute = attribute;
		this.environmentAlias = environmentAlias;
	}

	public BBSimpleSlot(BBSimpleElementRegistry registry, BBSimpleSlot slot, Map<BBSimpleAttribute, BBSimpleAttribute> copied)
	{
		attribute = slot.attribute;
		if (slot.environmentAlias != null)
			environmentAlias = copied.get(slot.environmentAlias);
		if (slot.value != null)
		{
			value = new ArrayList<BBSimpleParameter>();
			for (BBSimpleParameter p : slot.value)
			{
				if (p.getLiteral() != null)
					value.add(new BBSimpleParameter(p.getLiteral()));
				else
					value.add(new BBSimpleParameter(copied.get(p.getAttribute())));
			}
		}
	}

	private BBSimpleAttribute translateAttribute(List<BBSimpleAttribute> attributes, DEAttribute attribute)
	{
		for (BBSimpleAttribute attr : attributes)
			if (attr.getComplex() == attribute)
				return attr;
		return null;
	}
	
	public void resolveImplementation(BBSimpleElementRegistry registry, Class<?> implClass, BBSimpleComponent element) throws BBImplementationInstantiationException
	{
		if (resolved)
			return;
		resolved = true;

		// get the "attribute" of the implementation class via reflection
		reflectiveField = new ReflectiveAttribute(attribute);
	}

	@Override
	public String getName()
	{
		return "";
	}

	@Override
	public String getRawName()
	{
		return "";
	}

	public BBSimpleAttribute getAttribute()
	{
		return attribute;
	}

	public List<BBSimpleParameter> getValue()
	{
		return value;
	}

	public BBSimpleAttribute getEnvironmentAlias()
	{
		return environmentAlias;
	}

	public void remapAttributes(Map<BBSimpleAttribute, BBSimpleAttribute> redundant)
	{
		// only need to do this for the environment alias, as by now the values would have been pushed
		// into attributes at the top level
		if (environmentAlias != null && redundant.containsKey(environmentAlias))
			environmentAlias = redundant.get(environmentAlias);
	}

	public void setValue(Object obj, Object value, BBSimpleElement element) throws BBRuntimeException
	{
	  reflectiveField.set(obj, value);
	}
	
	@Override
	public Map<String, List<? extends BBSimpleObject>> getChildren(boolean top)
	{
		Map<String, List<? extends BBSimpleObject>> children = new LinkedHashMap<String, List<? extends BBSimpleObject>>();
		List<BBSimpleObject> t = new ArrayList<BBSimpleObject>();
		t.add(attribute);
		children.put("attribute", t);
		children.put("value", value);		
		List<BBSimpleObject> a = new ArrayList<BBSimpleObject>();
		a.add(environmentAlias);
		children.put("alias", a);
		
		return children;
	}

	@Override
	public String getTreeDescription()
	{
		return
			"slot";
	}
}
