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
import com.intrinsarc.deltaengine.base.*;

public class BBPort extends DEPort implements INode, Serializable
{
  private DEObject parent;
  private String name;
  private String uuid = BBUidGenerator.newUuid(getClass());
  private Integer lowerBound;
  private Integer upperBound;
  private LazyObjects<DEInterface> setLazyProvides;
  private LazyObjects<DEInterface> setLazyRequires;
  private Set<DEInterface> setProvides;
  private Set<DEInterface> setRequires;
  private Boolean suppressGeneration;
	private List<DEAppliedStereotype> appliedStereotypes;
	private boolean forceBeanMain;
	private boolean forceNotBeanMain;
	private boolean forceBeanNoName;
	private boolean wantsRequiredWhenProviding;
	private PortKindEnum portKind = PortKindEnum.NORMAL;
	private Boolean ordered;
 
  public BBPort(UuidReference reference)
  {
  	this.uuid = reference.getUuid();
  	this.name = reference.getName();
  	GlobalNodeRegistry.registry.addNode(this);
  }
  
  public BBPort(String uuid, String name)
  {
  	this.uuid = uuid;
  	this.name = name;
  	GlobalNodeRegistry.registry.addNode(this);
  }
  
  @Override
  public int getLowerBound()
  {
    return lowerBound == null ? 1 : lowerBound;
  }
  
	public void setLowerBound(int bound)
	{
		lowerBound = bound;
	}

	@Override
  public int getUpperBound()
  {
    return upperBound == null ? 1 : upperBound;
  }

	public void setUpperBound(int bound)
	{
		upperBound = bound;
	}

	@Override
  public DEPort asPort()
  {
    return this;
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
  
  public LazyObjects<DEInterface> settable_getLazySetProvidedInterfaces()
  {
  	if (setLazyProvides == null)
  		setLazyProvides = new LazyObjects<DEInterface>(DEInterface.class);
  	return setLazyProvides;
  }

  public LazyObjects<DEInterface> settable_getLazySetRequiredInterfaces()
  {
  	if (setLazyRequires == null)
  		setLazyRequires = new LazyObjects<DEInterface>(DEInterface.class);
  	return setLazyRequires;
  }

  public Set<DEInterface> settable_getSetProvidedInterfaces()
  {
  	if (setProvides == null)
  		setProvides = new HashSet<DEInterface>();
  	return setProvides;
  }

  public Set<DEInterface> settable_getSetRequiredInterfaces()
  {
  	if (setRequires == null)
  		setRequires = new HashSet<DEInterface>();
  	return setRequires;
  }

	@Override
	public Set<? extends DEInterface> getSetProvidedInterfaces()
	{
    return setProvides == null ? new HashSet<DEInterface>() : new HashSet<DEInterface>(setProvides);
	}
	
	@Override
	public Set<? extends DEInterface> getSetRequiredInterfaces()
	{
    return setRequires == null ? new HashSet<DEInterface>() : new HashSet<DEInterface>(setRequires);
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public void setName(String name)
	{
		this.name = name;
	}


	public void setSuppressGeneration(boolean suppress)
	{
		this.suppressGeneration = suppress ? true : null;
	}
	
	@Override
	public boolean isSuppressGeneration()
	{
		return suppressGeneration == null ? false : true;
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

	public void setForceBeanMain(boolean beanMain)
	{
		this.forceBeanMain = beanMain;
	}
	
	public void setForceNotBeanMain(boolean notBeanMain)
	{
		this.forceNotBeanMain = notBeanMain;
	}
	
	public void setForceBeanNoName(boolean beanNoName)
	{
		this.forceBeanNoName = beanNoName;
	}
	
	public void setWantsRequiredWhenProviding(boolean wants)
	{
		wantsRequiredWhenProviding = wants;
	}
	
	@Override
	public boolean isForceBeanMain()
	{
		return forceBeanMain;
	}

	@Override
	public boolean isForceNotBeanMain()
	{
		return forceNotBeanMain;
	}

	@Override
	public boolean isWantsRequiredWhenProviding()
	{
		return wantsRequiredWhenProviding;
	}

	@Override
	public boolean isForceBeanNoName()
	{
		return forceBeanNoName;
	}

	public void setPortKind(PortKindEnum portKind)
	{
		this.portKind = portKind;
	}
	
	@Override
	public PortKindEnum getPortKind()
	{
		return portKind;
	}

	public void setOrdered(boolean ordered)
	{
		this.ordered = ordered;
	}
	
	@Override
	public boolean isOrdered()
	{
		return ordered != null ? ordered : false;
	}

	@Override
	public void resolveLazyReferences()
	{
		if (setLazyProvides != null)
		{
			setLazyProvides.resolve();
			settable_getSetProvidedInterfaces().addAll(setLazyProvides.asObjectsSet());
			setLazyProvides = null;
		}
		if (setLazyRequires != null)
		{
			setLazyRequires.resolve();
			settable_getSetRequiredInterfaces().addAll(setLazyRequires.asObjectsSet());
			setLazyRequires = null;
		}
		
		if (appliedStereotypes != null)
			for (DEObject obj : appliedStereotypes)
				obj.resolveLazyReferences();
	}	
}
