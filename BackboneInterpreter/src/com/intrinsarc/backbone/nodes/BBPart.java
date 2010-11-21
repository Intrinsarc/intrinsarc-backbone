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

import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class BBPart extends DEPart implements INode, Serializable
{
  private DEObject parent;
  private String name;
  private String uuid = BBUidGenerator.newUuid(getClass());
  private LazyObject<DEComponent> type = new LazyObject<DEComponent>(DEComponent.class);
  private List<DESlot> slots;
  private List<BBPortRemap> remaps;
	private List<? extends DEAppliedStereotype> appliedStereotypes;
	private transient boolean synthetic;
	private transient boolean pullUp;

  public BBPart(String uuid)
  {
  	this.uuid = uuid;
  	this.name = uuid;
  	GlobalNodeRegistry.registry.addNode(this);
  }

  public BBPart(UuidReference reference)
  {
  	this(reference.getUuid());
  	name = reference.getName();
  }

  public BBPart(String uuid, boolean synthetic, boolean pullUp)
  {
  	this.uuid = uuid;
		this.synthetic = synthetic;
		this.pullUp = pullUp;
  	GlobalNodeRegistry.registry.addNode(this);
  }

  @Override
  public DEPort asPort()
  {
    return null;
  }

  @Override
  public String getName()
  {
    return name;
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
    return uuid;
  }

  public void setParent(DEObject parent)
  {
    this.parent = parent;
  }

  public DEComponent getType()
  {
    return type.getObject();
  }

  public void setType(DEComponent type)
  {
    this.type.setObject(type);
  }

  public void setType(UuidReference reference)
  {
    this.type.setReference(reference);
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  public void setName(String name)
  {
    this.name = name;
  }
  
  public List<DESlot> getSlots()
  {
  	if (slots == null)
  		return new ArrayList<DESlot>();
  	return slots;
  }

  public List<DESlot> settable_getSlots()
  {
    if (slots == null)
      slots = new ArrayList<DESlot>();
    return slots;
  }

	@Override
	public Set<DeltaPair> getPortRemaps()
	{
		if (remaps == null)
			return new HashSet<DeltaPair>();
		Set<DeltaPair> pairs = new HashSet<DeltaPair>();
		for (BBPortRemap remap : remaps)
			pairs.add(new DeltaPair(remap.getUuid(), remap.getPort()));
		return pairs;
	}

	public List<BBPortRemap> settable_getPortRemaps()
	{
		if (remaps == null)
			remaps = new ArrayList<BBPortRemap>();
		return remaps;
	}
	
	@Override
	public boolean isSynthetic()
	{
		return synthetic;
	}
	
	@Override
	public boolean isPullUp()
	{
		return pullUp;
	}

	public void setAppliedStereotypes(List<? extends DEAppliedStereotype> appliedStereotypes)
	{
		this.appliedStereotypes = appliedStereotypes.isEmpty() ? null : appliedStereotypes;
	}

	@Override
	public List<? extends DEAppliedStereotype> getAppliedStereotypes()
	{
		return appliedStereotypes == null ? new ArrayList<DEAppliedStereotype>() : appliedStereotypes;
	}

	@Override
	public void resolveLazyReferences()
	{
		type.resolve();
		resolve(slots);
		resolve(appliedStereotypes);
		if (remaps != null)
			for (BBPortRemap remap : remaps)
				remap.resolveLazyReferences();
	}
	
	private void resolve(List<? extends DEObject> objects)
	{
		if (objects != null)
			for (DEObject obj : objects)
				obj.resolveLazyReferences();
	}
}
