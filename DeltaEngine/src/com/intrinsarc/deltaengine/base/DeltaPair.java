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


public class DeltaPair
{
  private String uuid;
  private DEConstituent constituent;
  private DEConstituent original;
  
  public DeltaPair(String uuid, DEConstituent constituent)
  {
    this.uuid = uuid;
    this.original = constituent;
    this.constituent = constituent;
  }

  public DeltaPair(String uuid, DEConstituent original, DEConstituent constituent)
  {
    this.uuid = uuid;
    this.original = original;
    this.constituent = constituent;
  }

  public String getUuid()
  {
    return uuid;
  }

  public DEConstituent getConstituent()
  {
    return constituent;
  }
  
  public DEConstituent getOriginal()
  {
    return original;
  }
  
  public void setOriginal(DEConstituent original)
  {
    this.original = original;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof DeltaPair))
      return false;
    DeltaPair other = (DeltaPair) obj;
    return uuid.equals(other.uuid) && constituent.equals(other.constituent);
  }

  @Override
  public int hashCode()
  {
    return uuid.hashCode() ^ constituent.hashCode();
  }
}
