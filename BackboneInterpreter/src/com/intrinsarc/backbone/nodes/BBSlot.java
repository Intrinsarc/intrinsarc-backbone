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
package com.intrinsarc.backbone.nodes;

import java.io.*;
import java.util.*;

import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class BBSlot extends DESlot implements INode, Serializable
{
	private DEObject parent;
	private LazyObject<DEAttribute> attribute = new LazyObject<DEAttribute>(DEAttribute.class);
	private List<DEParameter> value;
	private List<DEAppliedStereotype> appliedStereotypes;

	public BBSlot(UuidReference attribute, List<DEParameter> value)
	{
		this.attribute.setReference(attribute);
		this.value = value;
	}
	
	public BBSlot(DEAttribute attribute, List<DEParameter> value)
	{
		this.attribute.setObject(attribute);
		this.value = value;
	}
	
  @Override
	public DEAttribute getAttribute()
	{
		return attribute.getObject();
	}

	@Override
	public List<DEParameter> getValue()
	{
		return value;
	}

	@Override
	public String getName()
	{
		return "";
	}

	@Override
	public DEObject getParent()
	{
		return parent;
	}

	@Override
	public Object getRepositoryObject()
	{
		return this;
	}

	@Override
	public String getUuid()
	{
		return "";
	}

	public void setParent(DEObject parent)
	{
		this.parent = parent;
	}

	public void setAppliedStereotypes(List<DEAppliedStereotype> appliedStereotypes)
	{
		this.appliedStereotypes = appliedStereotypes.isEmpty() ? null : appliedStereotypes;
	}

	@Override
	public List<DEAppliedStereotype> getAppliedStereotypes()
	{
		return appliedStereotypes == null ? new ArrayList<DEAppliedStereotype>() : appliedStereotypes;
	}

	@Override
	public void resolveLazyReferences()
	{
		if (appliedStereotypes != null)
			for (DEAppliedStereotype app : appliedStereotypes)
				app.resolveLazyReferences();
		attribute.resolve();
		if (value != null)
			for (DEParameter p : value)
				p.resolveLazyReferences();
	}	
}
