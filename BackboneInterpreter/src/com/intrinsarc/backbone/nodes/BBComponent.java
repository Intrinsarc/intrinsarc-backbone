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

public class BBComponent extends DEComponent implements INode
{
  private DEObject parent;
  private String rawName;
  private String uuid = BBUidGenerator.newUuid(getClass());
  private String kind;
  private Boolean retired;
  private Boolean isAbstract;  
  private LazyObjects<DEElement> replaces = new LazyObjects<DEElement>(DEElement.class);
  private LazyObjects<DEElement> resembles = new LazyObjects<DEElement>(DEElement.class);
  
  // the stereotypes
  private List<DEAppliedStereotype> replacedAppliedStereotypes;
  
  // the attributes
  private List<String> deletedAttributes;
  private List<BBReplacedAttribute> replacedAttributes;
  private List<BBAttribute> addedAttributes;
  
  // the ports
  private List<String> deletedPorts;
  private List<BBReplacedPort> replacedPorts;
  private List<BBPort> addedPorts;
  
  // the parts
  private List<String> deletedParts;
  private List<BBReplacedPart> replacedParts;
  private List<BBPart> addedParts;
  
  // the connectors
  private List<String> deletedConnectors;
  private List<BBReplacedConnector> replacedConnectors;
  private List<BBConnector> addedConnectors;
  
  // the connectors
  private List<String> deletedPortLinks;
  private List<BBReplacedConnector> replacedPortLinks;
  private List<BBConnector> addedPortLinks;
	
  // for going the other way
  private List<DEElement> substituters;
  private Set<DEElement> resemblers;
  
  // the deltas for the difference calculations
  private boolean initialiseDeltas;
  private Deltas attributes;
  private Deltas parts;
  private Deltas ports;
  private Deltas connectors;
  private Deltas portLinks;
  private Deltas appliedStereotypes;
  
  // other cached variables
  private Set<String> replacedUuids;
  
  public BBComponent(String uuid)
  {
  	this.uuid = uuid;
  	this.rawName = uuid;
  	kind = ComponentKindEnum.NORMAL.name();
  	GlobalNodeRegistry.registry.addNode(this);

    substituters = new ArrayList<DEElement>();
    resemblers = new HashSet<DEElement>();
  }
  
  public BBComponent(UuidReference reference)
  {
  	this(reference.getUuid());
  	rawName = reference.getName();
  }
  
  public List<DEAppliedStereotype> settable_getReplacedAppliedStereotypes()
	{
  	if (replacedAppliedStereotypes == null)
  		replacedAppliedStereotypes = new ArrayList<DEAppliedStereotype>();
  	return replacedAppliedStereotypes;
	}

  public void setParent(DEObject parent)
  {
    this.parent = parent;
    
    // tell all constituents
    informAboutParent(replacedAttributes);
    informAboutParent(addedAttributes);
    informAboutParent(replacedPorts);
    informAboutParent(addedPorts);
    informAboutParent(replacedParts);
    informAboutParent(addedParts);
    informAboutParent(replacedConnectors);
    informAboutParent(addedConnectors);
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
	    	if (node instanceof INode)
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
  
	public void setComponentKind(ComponentKindEnum componentKind)
	{
		this.kind = componentKind.name();		
	}
	
	public ComponentKindEnum getComponentKind()
	{
		return ComponentKindEnum.valueOf(kind);
	}
	
  ///////////////////////// the attributes ////////////////////////  
  public List<String> settable_getDeletedAttributes()
  {
    if (deletedAttributes == null)
      deletedAttributes = new ArrayList<String>();
    return deletedAttributes;
  }

  public List<BBReplacedAttribute> settable_getReplacedAttributes()
  {
    if (replacedAttributes == null)
      replacedAttributes = new ArrayList<BBReplacedAttribute>();
    return replacedAttributes;
  }

  public List<BBAttribute> settable_getAddedAttributes()
  {
    if (addedAttributes == null)
      addedAttributes = new ArrayList<BBAttribute>();
    return addedAttributes;
  }
  
  ///////////////////////// the ports ////////////////////////  
  public List<String> settable_getDeletedPorts()
  {
    if (deletedPorts == null)
      deletedPorts = new ArrayList<String>();
    return deletedPorts;
  }

  public List<BBReplacedPort> settable_getReplacedPorts()
  {
    if (replacedPorts == null)
      replacedPorts = new ArrayList<BBReplacedPort>();
    return replacedPorts;
  }

  public List<BBPort> settable_getAddedPorts()
  {
    if (addedPorts == null)
      addedPorts = new ArrayList<BBPort>();
    return addedPorts;
  }

  ///////////////////////// the parts ////////////////////////  
  public List<String> settable_getDeletedParts()
  {
    if (deletedParts == null)
      deletedParts = new ArrayList<String>();
    return deletedParts;
  }

  public List<BBReplacedPart> settable_getReplacedParts()
  {
    if (replacedParts == null)
      replacedParts = new ArrayList<BBReplacedPart>();
    return replacedParts;
  }

  public List<BBPart> settable_getAddedParts()
  {
    if (addedParts == null)
      addedParts = new ArrayList<BBPart>();
    return addedParts;
  }

  ///////////////////////// the connectors ////////////////////////  
  public List<String> settable_getDeletedConnectors()
  {
    if (deletedConnectors == null)
      deletedConnectors = new ArrayList<String>();
    return deletedConnectors;
  }

  public List<BBReplacedConnector> settable_getReplacedConnectors()
  {
    if (replacedConnectors == null)
      replacedConnectors = new ArrayList<BBReplacedConnector>();
    return replacedConnectors;
  }

  public List<BBConnector> settable_getAddedConnectors()
  {
    if (addedConnectors == null)
      addedConnectors = new ArrayList<BBConnector>();
    return addedConnectors;
  }

  ///////////////////////// the port links ////////////////////////  
  public List<String> settable_getDeletedPortLinks()
  {
    if (deletedPortLinks == null)
      deletedPortLinks = new ArrayList<String>();
    return deletedPortLinks;
  }

  public List<BBReplacedConnector> settable_getReplacedPortLinks()
  {
    if (replacedPortLinks == null)
      replacedPortLinks = new ArrayList<BBReplacedConnector>();
    return replacedPortLinks;
  }

  public List<BBConnector> settable_getAddedPortLinks()
  {
    if (addedPortLinks == null)
      addedPortLinks = new ArrayList<BBConnector>();
    return addedPortLinks;
  }

  //////////////////////////////////////////////////////
  
  public LazyObjects<DEElement> settable_getReplaces()
  {
    return replaces;
  }

  public LazyObjects<DEElement> settable_getRawResembles()
  {
  	return resembles;
  }
  
  ////////////////////// contract functions ///////////////////////
  
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
	public List<DEElement> getRawReplaces()
	{
    return replaces.getObjects();
	}

	@Override
	public List<? extends DEElement> getRawResembles()
	{
    return resembles.getObjects();
	}

  @Override
  public boolean canBeDecomposed(DEStratum perspective)
  {
    return
    	getComponentKind() != ComponentKindEnum.PRIMITIVE &&
    	!isLeaf(perspective) &&
    	!isPlaceholder(perspective);
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
      case DELTA_PART:
        return parts;
      case DELTA_ATTRIBUTE:
        return attributes;
      case DELTA_PORT:
        return ports;
      case DELTA_CONNECTOR:
        return connectors;
      case DELTA_PORT_LINK:
      	return portLinks;
    }
    
    return super.getDeltas(type);
  }  

  @Override
  public String getRawName()
  {
    return rawName;
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
    // handle attributes
    attributes = createDeltas(
        this,
        ConstituentTypeEnum.DELTA_ATTRIBUTE,
        addedAttributes,
        deletedAttributes,
        replacedAttributes);
    
    // handle parts
    parts = createDeltas(
        this,
        ConstituentTypeEnum.DELTA_PART,
        addedParts,
        deletedParts,
        replacedParts);
        
    // handle ports
    ports = createDeltas(
        this,
        ConstituentTypeEnum.DELTA_PORT,
        addedPorts,
        deletedPorts,
        replacedPorts);
    
    // handle connectors
    connectors = createDeltas(
        this,
        ConstituentTypeEnum.DELTA_CONNECTOR,
        addedConnectors,
        deletedConnectors,
        replacedConnectors);
    
    // handle port links
    portLinks = createDeltas(
        this,
        ConstituentTypeEnum.DELTA_PORT_LINK,
        addedPortLinks,
        deletedPortLinks,
        replacedPortLinks);
    
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
      addReplacedUuids(replacedUuids, replacedAttributes);
      addReplacedUuids(replacedUuids, replacedParts);
      addReplacedUuids(replacedUuids, replacedPorts);
      addReplacedUuids(replacedUuids, replacedConnectors);
      addReplacedUuids(replacedUuids, replacedPortLinks);
    }
    
    return replacedUuids;
  }
  
  //////////////////////////////////////////// static helper functions
  public static void addReplacedUuids(Set<String> replacedUuids, List<? extends BBReplacedConstituent> replaces)
  {
  	if (replaces != null)
	    for (BBReplacedConstituent replace : replaces)
	      replacedUuids.add(replace.getUuid());
  }

  /**
   * record the deltas, as a set of pairs for adds, a set of uuids for deletes and a set of pairs for replaces
   */
  public static Deltas createDeltas(
      DEElement deElement,
      ConstituentTypeEnum type,
      List<? extends DEConstituent> elements,
      List<String> deltaDeletedConstituents,
      List<? extends BBReplacedConstituent> deltaReplacedElements)
  {
    IDeltaEngine engine = GlobalDeltaEngine.engine;
    Set<DeltaPair> adds = new LinkedHashSet<DeltaPair>();
    Set<String> deletes = new LinkedHashSet<String>();
    Set<DeltaPair> replaces = new LinkedHashSet<DeltaPair>();

    if (elements != null)
	    for (DEConstituent element : elements)
	      adds.add(new DeltaPair(element.getUuid(), engine.locateObject(element).asConstituent()));

    if (deltaDeletedConstituents != null)
    	deletes.addAll(deltaDeletedConstituents);
    	
    if (deltaReplacedElements != null)
	    for (BBReplacedConstituent replace : deltaReplacedElements)
	    {
	      replaces.add(
	          new DeltaPair(
	              replace.getUuid(),
	              engine.locateObject(replace.getReplacement()).asConstituent()));
	    }
    
    return new Deltas(deElement, adds, deletes, replaces, type);
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

  public void setRawAbstract(boolean isAbstract)
  {
  	this.isAbstract = isAbstract ? true : null;
  }
  
	@Override
	public boolean isRawAbstract()
	{
		return isAbstract != null ? isAbstract: false;
	}
	
	@Override
	public void resolveLazyReferences()
	{
		resembles.resolve();
		replaces.resolve();
		resolve(replacedAppliedStereotypes);
		resolveReplace(replacedAttributes);
		resolve(addedAttributes);
		resolveReplace(replacedPorts);
		resolve(addedPorts);
		resolveReplace(replacedParts);
		resolve(addedParts);
		resolveReplace(replacedConnectors);
		resolve(addedConnectors);
		resolveReplace(replacedPortLinks);
		resolve(addedPortLinks);
	}

	private void resolveReplace(List<? extends BBReplacedConstituent> replaced)
	{
		if (replaced != null)
			for (BBReplacedConstituent c : replaced)
				c.getReplacement().resolveLazyReferences();
	}

	private void resolve(List<? extends DEObject> objects)
	{
		if (objects != null)
			for (DEObject obj : objects)
				obj.resolveLazyReferences();
	}
}
