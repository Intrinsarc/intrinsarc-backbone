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

public class TagConstituent extends DEConstituent
{
	private String UUID;
	private String tag;
	
	public TagConstituent(String UUID, String tag)
	{
		this.UUID = UUID;
		this.tag = tag;
	}
	
	@Override
	public String getName()
	{
		return tag;
	}
	
	@Override
	public DEObject getParent()
	{
		return null;
	}

	@Override
	public Object getRepositoryObject()
	{
		return null;
	}

	@Override
	public String getUuid()
	{
		return UUID;
	}

	@Override
	public List<DEAppliedStereotype> getAppliedStereotypes()
	{
		return null;
	}
	
  public TagConstituent asTag()
  {
  	return this;
  }

  @Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof TagConstituent))
			return false;
		TagConstituent other = (TagConstituent) obj;
		return UUID.equals(other.UUID) && tag.equals(other.tag);
	}

	@Override
	public int hashCode()
	{
		return UUID.hashCode() ^ tag.hashCode();
	}
}
