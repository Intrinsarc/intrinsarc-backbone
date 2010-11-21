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
package com.intrinsarc.deltaengine.base;

import java.util.*;

public abstract class DEObject
{
  public static String SEPARATOR = " :: ";
  private transient DEStratum home;
  
  // NOTE: objects are defined by their system identity.  we could match based on uuid, but it isn't necessary.
  public abstract String getUuid();
  public abstract String getName();
  public abstract Object getRepositoryObject();
  public abstract DEObject getParent();
	public abstract List<? extends DEAppliedStereotype> getAppliedStereotypes();
  
  /** type testing */
  public DEStratum asStratum()
  {
    return null;
  }
  
	public DERequirementsFeature asRequirementsFeature()
	{
		return null;
	}

	public DEComponent asComponent()
  {
    return null;
  }
  
  public DEInterface asInterface()
  {
    return null;
  }
  
  public DEElement asElement()
  {
    return null;
  }
  
  public DEConstituent asConstituent()
  {
    return null;
  }
  
  public DESlot asSlot()
	{
		return null;
	}

  public DEObject()
  {
  }
  
  public IDeltaEngine getEngine()
  {
  	return GlobalDeltaEngine.engine;
  }
  
  public String getFullyQualifiedName()
  {
  	return getFullyQualifiedName(SEPARATOR);
  }
  
  public String getFullyQualifiedName(String separator)
  {
    if (getParent() != null && getParent() != getEngine().getRoot())
      return getParent().getFullyQualifiedName(separator) + separator + getName();
    return getName();
  }
  
  /**
   * look up until we find an owning stratum -- works even if this is a constituent
   * @return
   */
  public DEStratum getHomeStratum()
  {
    if (home != null)
      return home;
    
    // travel up until a stratum is found, or return null
    DEObject parent = getParent();
    while (parent != null)
    {
      if (parent.asStratum() != null)
      {
        home = parent.asStratum();
        return home;
      }
      parent = parent.getParent();
    }
    return null;
  }
  
  /**
   * look up until we find a parent stratum
   */
	public DEStratum getParentStratum()
	{
		DEObject current = getParent();
		while (current != null && !(current.asStratum() != null))
			current = current.getParent();
		if (current == null)
			return null;
		return current.asStratum();
	}
  
  public void visit(DEObjectVisitor visitor)
  {
    if (asStratum() != null)
      visitor.visitStratum(asStratum());
    
    if (asComponent() != null)
      visitor.visitComponent(asComponent());
    
    if (asInterface() != null)
      visitor.visitInterface(asInterface());
  }
  
  public String toString()
  {
  	String name = getName();
  	if (name == null || name.length() == 0)
  		name = getUuid();
    return getClass().getSimpleName() + "(" + name + ")";
  }
  
  public List<? extends DEAppliedStereotype> extractAppliedStereotypes(DEStratum perspective)
  {
  	// packages/strata do not have stereotypes directly in Backbone, so ignore
  	List<? extends DEAppliedStereotype> applied = new ArrayList<DEAppliedStereotype>();
  	if (asStratum() != null)
  		return applied;
  	
  	if (asElement() != null)
  		applied = asElement().getAppliedStereotypes(perspective);
  	else
  	if (asConstituent() != null)
  		applied = asConstituent().getAppliedStereotypes();
  	else
  		return applied;
  	
  	return applied;
  }
  
  public DEAppliedStereotype extractAppliedStereotype(DEStratum perspective, String stereoUUID)
  {
  	// find the appropriate applied stereotype
  	for (DEAppliedStereotype appl : extractAppliedStereotypes(perspective))
  	{
  		Set<DEElement> closure =
  			new HashSet<DEElement>(
  					appl.getStereotype().getSuperElementClosure(perspective, false));
  		closure.add(appl.getStereotype());
  		for (DEElement e : closure)
  			if (e.getUuid().equals(stereoUUID))
  				return appl;
  	}
  	return null;
  }
  
  public boolean extractBooleanAppliedStereotypeProperty(DEStratum perspective, String stereoUUID, String propertyUUID)
  {
  	DEAppliedStereotype appl = extractAppliedStereotype(perspective, stereoUUID);
  	if (appl == null)
  		return false;
  	return appl.getBooleanProperty(propertyUUID);
  }

  public String extractStringAppliedStereotypeProperty(DEStratum perspective, String stereoUUID, String propertyUUID)
  {
  	DEAppliedStereotype appl = extractAppliedStereotype(perspective, stereoUUID);
  	if (appl == null)
  		return null;
  	return appl.getStringProperty(propertyUUID);
  }
  
  public void resolveLazyReferences()
  {  	
  }
}
