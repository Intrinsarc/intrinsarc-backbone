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

import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class BBAppliedStereotype extends DEAppliedStereotype
{
  private transient DEObject parent;
	private LazyObject<DEComponent> stereotype = new LazyObject<DEComponent>(DEComponent.class);
	private Map<LazyObject<DEAttribute>, String> lazyProperties;
	private Map<DEAttribute, String> properties;
	
	public BBAppliedStereotype()
	{		
	}
	
	@Override
	public String getName()
	{
		return "";
	}
	
	public Map<LazyObject<DEAttribute>, String> settable_getLazyProperties()
	{
		if (lazyProperties == null)
			lazyProperties = new HashMap<LazyObject<DEAttribute>, String>();
		return lazyProperties;		
	}
	
	public Map<DEAttribute, String> settable_getProperties()
	{
		if (properties == null)
			properties = new HashMap<DEAttribute, String>();
		return properties;		
	}
	
	@Override
	public Map<DEAttribute, String> getProperties()
	{
		if (properties == null)
			return new HashMap<DEAttribute, String>(); 
		return properties;
	}

  public void setParent(DEObject parent)
  {
    this.parent = parent;
  }

	@Override
	public DEComponent getStereotype()
	{
		return stereotype.getObject();
	}

	@Override
	public DEObject getParent()
	{
		return parent;
	}
	
	public void setStereotype(DEComponent stereo)
	{
		stereotype.setObject(stereo);
	}

	
	public void setStereotype(UuidReference stereo)
	{
		stereotype.setReference(stereo);
	}
	
	@Override
	public void resolveLazyReferences()
	{
		stereotype.resolve();
		if (lazyProperties != null)
		{
			Map<DEAttribute, String> props = settable_getProperties();
			for (LazyObject<DEAttribute> attr : lazyProperties.keySet())
			{
				attr.resolve();
				props.put(attr.getObject(), lazyProperties.get(attr));
			}
		}
		lazyProperties = null;
	}
}
