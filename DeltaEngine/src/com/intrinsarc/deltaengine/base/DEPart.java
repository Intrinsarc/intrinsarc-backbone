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

public abstract class DEPart extends DEConstituent
{
  private Map<DEStratum, Set<DeltaPair>> remappedPorts;
  public abstract Set<DeltaPair> getPortRemaps();
  public abstract DEComponent getType();
  public abstract List<DESlot> getSlots();
  
  public DEPart()
  {
    super();
  }

  @Override
  public DEPart asPart()
  {
    return this;
  }
  
  public Set<DeltaPair> getPortsAfterRemap(DEStratum perspective)
  {
  	// lazily instantiate the remapped ports
  	if (remappedPorts == null)
      remappedPorts = new HashMap<DEStratum, Set<DeltaPair>>();  		
  	
    // if the cache holds the results, return immediately
    Set<DeltaPair> newPairs = remappedPorts.get(perspective);
    if (newPairs != null)
      return newPairs;
    
    DEComponent type = getType();
    // be defensive -- before design is completed, parts may have no types...
    if (type == null)
      return new HashSet<DeltaPair>();
    
    IDeltas deltas = type.getDeltas(ConstituentTypeEnum.DELTA_PORT);
    Set<DeltaPair> pairs = deltas.getConstituents(perspective);
    
    // remove any ports that have been remapped
    newPairs = new LinkedHashSet<DeltaPair>();
    for (DeltaPair pair : pairs)
    {
      DeltaPair remapped = pair;
      for (DeltaPair remapPair : getPortRemaps())
        if (pair.getConstituent() == remapPair.getConstituent())
        {
          remapped = remapPair;
          // make it look like a replace
          remapped.setOriginal(remapPair.getOriginal());
          break;
        }      
      newPairs.add(remapped);
    }
    
    // save in the cache
    remappedPorts.put(perspective, newPairs);    
    
    return newPairs;
  }
  
	public DEPort unRemap(DEPort port)
	{
		// look to see if this has been remapped, and take the remap value
		for (DeltaPair pair : getPortRemaps())
			if (pair.getConstituent() == port)
				return pair.getOriginal().asPort();
		return port;
	}
}
