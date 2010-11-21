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


public class DummyDeltas implements IDeltas
{
	public DummyDeltas()
	{
	}

  public Set<ErrorDescription> isWellFormed(DEStratum perspective)
  {
  	return new HashSet<ErrorDescription>();
  }
  
  public Set<DeltaPair> getConstituents(DEStratum perspective)
  {
  	return new HashSet<DeltaPair>();
  }

	public Set<DeltaPair> getConstituents(DEStratum perspective, boolean omitSynthetics)
  {
		return new HashSet<DeltaPair>();
  }

  public Set<DeltaPair> getPairs(DEStratum perspective, DeltaPairTypeEnum toGet)
  {
  	return new HashSet<DeltaPair>();
  }
  
	public Collection<String> getIds(DEStratum perspective, DeltaIdTypeEnum idType)
	{
		return new ArrayList<String>();
	}

	public Set<DeltaPair> getAddObjects()
	{
		return new HashSet<DeltaPair>();
	}

	public Set<String> getDeleteObjects()
	{
		return new HashSet<String>();
	}

	public Set<DeltaPair> getReplaceObjects()
	{
		return new HashSet<DeltaPair>();
	}

	public void clearCache(DEStratum perspective)
	{
	}
}
