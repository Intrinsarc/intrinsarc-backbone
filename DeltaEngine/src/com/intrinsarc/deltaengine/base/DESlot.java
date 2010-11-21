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

public abstract class DESlot extends DEObject
{
	public abstract DEAttribute getAttribute();
	public abstract List<DEParameter> getValue();
	
	public DEAttribute getAttribute(DEStratum perspective, DEElement element)
	{
		return translateOriginalToConstituent(perspective, element, getAttribute());
	}
	
	public static DEAttribute translateOriginalToConstituent(DEStratum perspective, DEElement element, DEAttribute original)
	{
    // find the attribute in the component which we reference (as the original)
    for (DeltaPair pair : element.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getConstituents(perspective))
    {
      if (pair.getOriginal() == original)
      	return pair.getConstituent().asAttribute();
    }
    return null;
	}
}
