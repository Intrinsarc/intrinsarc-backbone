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

import java.util.*;

import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.deltaengine.base.*;

public class BBAttribute extends DEAttribute implements INode
{
  private DEObject parent;
  private String name;
  private String uuid = BBUidGenerator.newUuid(getClass());
  private LazyObject<DEElement> type = new LazyObject<DEElement>(DEElement.class);
  private List<DEParameter> defaultValue;
  private List<BBAppliedStereotype> appliedStereotypes;
	private Boolean readOnly;
	private Boolean writeOnly;
	private Boolean suppressGeneration;
	private boolean synthetic;
	private boolean pullUp;
  
	public BBAttribute() {}
	
  public BBAttribute(UuidReference reference)
  {
  	this(reference.getUuid(), false, false);
  	this.name = reference.getName();
  	GlobalNodeRegistry.registry.addNode(this);
  }

  public BBAttribute(String uuid, boolean synthetic, boolean pullUp)
  {
  	this.uuid = uuid;
  	this.synthetic = synthetic;
  	this.pullUp = pullUp;
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

  public DEElement getType()
  {
    return type.getObject();
  }

  public void setType(DEElement type)
  {
		this.type.setObject(type);
  }

  public void setType(UuidReference reference)
  {
		this.type.setReference(reference);
  }

	public List<DEParameter> getDefaultValue()
  {
  	if (defaultValue == null)
  		return new ArrayList<DEParameter>();
    return defaultValue;
  }

  public void setDefaultValue(List<DEParameter> parameters)
  {
    this.defaultValue = parameters;
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  public void setName(String name)
  {
    this.name = name;
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

	public boolean isReadOnly()
	{
		return readOnly != null && readOnly;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly ? true : null;
	}

	public boolean isWriteOnly()
	{
		return writeOnly != null && writeOnly;
	}

	public void setWriteOnly(boolean writeOnly)
	{
		this.writeOnly = writeOnly ? true : null;
	}

	public void setAppliedStereotypes(List<BBAppliedStereotype> appliedStereotypes)
	{
		this.appliedStereotypes = appliedStereotypes.isEmpty() ? null : appliedStereotypes;
	}

	@Override
	public List<? extends DEAppliedStereotype> getAppliedStereotypes()
	{
		return appliedStereotypes == null ? new ArrayList<DEAppliedStereotype>() : appliedStereotypes;
	}

	@Override
	public boolean isSuppressGeneration()
	{
		return suppressGeneration != null && suppressGeneration;
	}	
	
	public void setSuppressGeneration(boolean suppress)
	{
		this.suppressGeneration = suppress ? true : null;
	}
	
	@Override
	public void resolveLazyReferences()
	{
		type.resolve();
		resolve(appliedStereotypes);
		if (defaultValue != null)
			for (DEParameter p : defaultValue)
				p.resolveLazyReferences();
	}
	
	private void resolve(List<? extends DEObject> objects)
	{
		if (objects != null)
			for (DEObject obj : objects)
				obj.resolveLazyReferences();
	}
}
