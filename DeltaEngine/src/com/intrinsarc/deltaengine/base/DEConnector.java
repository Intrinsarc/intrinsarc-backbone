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

public abstract class DEConnector extends DEConstituent
{
  public DEConnector()
  {
    super();
  }

  @Override
  public DEConnector asConnector()
  {
    return this;
  }

  public abstract DEPort getOriginalPort(int index);
  public abstract DEPart getOriginalPart(int index);
  public abstract boolean isIndexOk(int index);
  public abstract String getIndex(int index);
  public abstract boolean getTakeNext(int index);
  public abstract boolean isDelegate();
  
  /**
   * get the part from the perspective -- may have been replaced since the connector was defined 
   */
  public DEPart getPart(DEStratum perspective, DEComponent component, int index)
  {
  	return getPart(perspective, component, index, false);
  }

  /**
   * get the part from the perspective -- may have been replaced since the connector was defined 
   */
  public DEPart getPart(DEStratum perspective, DEComponent component, int index, boolean returnOriginal)
  {
    DEPart original = getOriginalPart(index);
    if (original == null)
      return null;
    
    // look in the parent for a part of the same uuid
    IDeltas parts = component.getDeltas(ConstituentTypeEnum.DELTA_PART);
    Set<DeltaPair> pairs = parts.getConstituents(perspective);
    for (DeltaPair pair : pairs)
    {
      if (pair.getOriginal() == original)
      {
      	if (returnOriginal)
      		return pair.getOriginal().asPart();
        return pair.getConstituent().asPart();
      }
    }

    return null;
  }

  /**
   * get the port from the perspective -- may have been replaced since the connector was defined
   * or remapped in the replacing part 
   */
  public DEPort getPort(DEStratum perspective, DEComponent component, int index)
  {
  	return getPort(perspective, component, index, false);
  }
  
  /**
   * get the port from the perspective -- may have been replaced since the connector was defined
   * or remapped in the replacing part 
   */
  public DEPort getPort(DEStratum perspective, DEComponent component, int index, boolean returnOriginal)
  {
    DEPort original = getOriginalPort(index);
    DEPart now = getPart(perspective, component, index);    
    
    // this is possible if we connect to a part, for instance
    if (original == null)
    	return null;
    
    Set<DeltaPair> pairs; 
    // if now is not set, this is a port on the classifier
    if (now == null)
    {
      IDeltas ports = component.getDeltas(ConstituentTypeEnum.DELTA_PORT);
      pairs = ports.getConstituents(perspective);
    }
    else
    {
      // look at the type of the part rather than the classifier
      pairs = now.getPortsAfterRemap(perspective);
    }
    
    // find one with the correct uuid
    for (DeltaPair pair : pairs)
    {
      // looking at remapped ports
      if (pair.getUuid().equals(original.getUuid()))
      {
      	if (returnOriginal)
      		return pair.getOriginal().asPort();
      	return pair.getConstituent().asPort();
      }
    }

    // if we got here, we found nothing
    return null;
  }

	public DEConnectorEnd makeConnectorEnd(DEStratum perspective, DEComponent owner, int side)
	{
		String original = getIndex(side);
		Integer numerical = translateOriginalIndex(original);
		return new DEConnectorEnd(
				this,
				side,
				getPart(perspective, owner, side),
				getPort(perspective, owner, side),
				numerical,
				original,
				getTakeNext(side));
	}
	
	private Integer translateOriginalIndex(String index)
	{
		if (index == null)
			return null;
		int size = index.length();
		for (int lp = 0; lp < size; lp++)
			if (!Character.isDigit(index.charAt(lp)))
				return null;
		return new Integer(index);
	}
}
