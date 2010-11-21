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

public abstract class DEAttribute extends DEConstituent
{
  public DEAttribute()
  {
    super();
  }

  @Override
  public DEAttribute asAttribute()
  {
    return this;
  }
  
  public abstract DEElement getType();
  public abstract List<DEParameter> getDefaultValue();
  public abstract boolean isReadOnly();
  public abstract boolean isWriteOnly();
  public abstract boolean isSuppressGeneration();
}
