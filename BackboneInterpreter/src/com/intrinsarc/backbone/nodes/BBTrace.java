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

public class BBTrace extends DETrace implements INode, Serializable
{
  private transient DEObject parent;
  private String uuid = BBUidGenerator.newUuid(getClass());
	private DERequirementsFeature target;
	private List<DEAppliedStereotype> appliedStereotypes;
  
  public BBTrace(String uuid)
  {
  	this.uuid = uuid;
  	GlobalNodeRegistry.registry.addNode(this);
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
	public DERequirementsFeature getTarget()
	{
		return target;
	}

	@Override
	public String getName()
	{
		return null;
	}	
}
