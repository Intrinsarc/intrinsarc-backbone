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

public class BBRequirementsFeature extends DEInterface implements INode, Serializable
{
  private transient DEObject parent;
  private String rawName;
  private String uuid = BBUidGenerator.newUuid(getClass());  
  private LazyObjects<DEElement> replaces = new LazyObjects<DEElement>(DEElement.class);
  private LazyObjects<DEElement> resembles = new LazyObjects<DEElement>(DEElement.class);
  private Boolean retired;
  
  // the stereotypes
  private List<DEAppliedStereotype> replacedAppliedStereotypes;
  
  // the constituents  
  private List<String> deletedSubfeatures;
  private List<BBReplacedRequirementsFeatureLink> replacedSubfeatures;
  private List<DERequirementsFeatureLink> addedSubfeatures;

	// for going the other way
  private transient List<DEElement> substituters;
  private transient Set<DEElement> resemblers;
  
  // the deltas for the difference calculations
  private transient boolean initialiseDeltas;
  private transient Deltas subfeatures;
  private transient Deltas appliedStereotypes;
  
  // other cached variables
  private transient Set<String> replacedUuids;
  
  public BBRequirementsFeature(UuidReference reference)
  {
  	this.uuid = reference.getUuid();
  	this.rawName = reference.getName();
  	GlobalNodeRegistry.registry.addNode(this);
  	
    substituters = new ArrayList<DEElement>();
    resemblers = new HashSet<DEElement>();
  }

  public void setParent(DEObject parent)
  {
    this.parent = parent;
    
    // tell all constituents
    informAboutParent(replacedSubfeatures);
    informAboutParent(addedSubfeatures);
    informAboutParent(replacedAppliedStereotypes);
    
  	// tell anything we resemble or substitute about ourselves
  	if (resembles != null)
  		for (DEElement r : resembles.getObjects())
  			r.getPossibleImmediateSubElements().add(this);
  	if (replaces != null)
  		for (DEElement r : replaces.getObjects())
  			r.getReplacers().add(this);
  }

  private void informAboutParent(List<? extends BBReplacedConstituent> constituents)
	{
  	if (constituents != null)
  	{
	  	Set<DEObject> objects = new HashSet<DEObject>();    
	    for (BBReplacedConstituent replaced : constituents)
	    	objects.add(replaced.getReplacement());
	    informAboutParent(objects);
  	}  	
	}
  
	private void informAboutParent(Collection<? extends DEObject> nodes)
  {
		if (nodes != null)
		{
	    for (DEObject node : nodes)
	    	if (nodes  instanceof INode)
		  		((INode) node).setParent(this);
		}
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  public void setRawName(String rawName)
  {
    this.rawName = rawName;
  }
  
  public List<String> settable_getDeletedSubfeatures()
  {
    if (deletedSubfeatures == null)
      deletedSubfeatures = new ArrayList<String>();
    return deletedSubfeatures;
  }

  public List<BBReplacedRequirementsFeatureLink> settable_getReplacedSubfeatures()
  {
    if (replacedSubfeatures == null)
      replacedSubfeatures = new ArrayList<BBReplacedRequirementsFeatureLink>();
    return replacedSubfeatures;
  }

  public List<DERequirementsFeatureLink> settable_getAddedSubfeatures()
  {
    if (addedSubfeatures == null)
      addedSubfeatures = new ArrayList<DERequirementsFeatureLink>();
    return addedSubfeatures;
  }

  public LazyObjects<DEElement> settable_getRawResembles()
  {
    return resembles;
  }
  
  public LazyObjects<DEElement> settable_getRawReplaces()
  {
    return replaces;
  }

  ///////////////////////////////// contract functions ///////////////////////


	@Override
	public Set<DEElement> getPossibleImmediateSubElements()
	{
		return resemblers;
	}

	@Override
	public List<DEElement> getReplacers()
	{
		return substituters;
	}
	
	@Override
	public List<? extends DEElement> getRawResembles()
	{
    return resembles.getObjects();
	}
	
  @Override
  public String getRawName()
  {
    return rawName;
  }
  
  @Override
  public IDeltas getDeltas(ConstituentTypeEnum type)
  {
    if (!initialiseDeltas)
    {
      initialiseDeltas();
      initialiseDeltas = true;
    }
    
    switch (type)
    {
	  	case DELTA_APPLIED_STEREOTYPE:
	  		return appliedStereotypes;
      case DELTA_REQUIREMENT_FEATURE_LINK:
        return subfeatures;
    }
    
    return super.getDeltas(type);
  } 

  @Override
  public List<DEElement> getRawReplaces()
  {
    return replaces.getObjects();
  }

  @Override
  public DEElement asElement()
  {
    return this;
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
	
	///////////////////////////////////////////////////
	///////////////// complex delta handling //////////
	
  private void initialiseDeltas()
  {
    // handle operations
    subfeatures = BBComponent.createDeltas(
        this,
        ConstituentTypeEnum.DELTA_REQUIREMENT_FEATURE_LINK,
        addedSubfeatures,
        deletedSubfeatures,
        replacedSubfeatures);
    
    // handle stereotypes
    Set<DeltaPair> pairs = new HashSet<DeltaPair>();
    if (replacedAppliedStereotypes != null)
	    for (DEAppliedStereotype app : replacedAppliedStereotypes)
	    	pairs.add(new DeltaPair(app.getUuid(), app));
    appliedStereotypes = new Deltas(
    		this,
    		new HashSet<DeltaPair>(),
    		new HashSet<String>(),
    		pairs,
    		ConstituentTypeEnum.DELTA_APPLIED_STEREOTYPE);
  }

  @Override
  public Set<String> getReplaceUuidsOnly()
  {
    if (replacedUuids == null)
    {
      replacedUuids = new LinkedHashSet<String>();
      BBComponent.addReplacedUuids(replacedUuids, replacedSubfeatures);
    }
    
    return replacedUuids;
  }

  public List<DEAppliedStereotype> settable_getReplacedAppliedStereotypes()
	{
  	if (replacedAppliedStereotypes == null)
  		replacedAppliedStereotypes = new ArrayList<DEAppliedStereotype>();
  	return replacedAppliedStereotypes;
	}

  public void setRawRetired(boolean retired)
  {
  	this.retired = retired ? true : null;
  }
  
	@Override
	public boolean isRawRetired()
	{
		return retired != null ? retired : false;
	}

	@Override
	public boolean isRawAbstract()
	{
		// features are never abstract
		return false;
	}
}
