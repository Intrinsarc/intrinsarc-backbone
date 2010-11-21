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


public abstract class DEConstituent extends DEObject
{
	@Override
  public DEComponent asComponent()
  {
    return null;
  }

  @Override
  public DEConstituent asConstituent()
  {
    return this;
  }

  public DEConstituent()
  {
    super();
  }
  
  public String getRawName()
  {
    return getName();
  }
  
  public DEPort asPort()
  {
    return null;
  }
  
  public DEPart asPart()
  {
    return null;
  }
  
  public DEAttribute asAttribute()
  {
    return null;
  }
  
  public DEOperation asOperation()
  {
    return null;
  }
  
  public DEConnector asConnector()
  {
    return null;
  }
  
	public DERequirementsFeatureLink asRequirementsFeatureLink()
	{
		return null;
	}

	public DEAppliedStereotype asAppliedStereotype()
  {
    return null;
  }
  
  public TagConstituent asTag()
  {
  	return null;
  }

	public DETrace asTrace()
	{
		return null;
	}

	public boolean isSynthetic()
	{
		return false;
	}
	
	public boolean isPullUp()
	{
		return false;
	}

	public boolean includesStereotype(String uuid)
	{
		if (getAppliedStereotypes() == null)
			return false;
		
		for (DEAppliedStereotype applied : getAppliedStereotypes())
			if (applied.getStereotype().getUuid().equals(uuid))
				return true;
		return false;
	}
}
