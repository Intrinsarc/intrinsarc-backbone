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
package com.intrinsarc.deltaengine.errorchecking;

import com.intrinsarc.deltaengine.base.*;

public class StratumErrorChecker
{
  private ErrorRegister errors;
  private DEStratum stratum;

  public StratumErrorChecker(
      DEStratum stratum,
      ErrorRegister errors)
  {
    this.stratum = stratum;
    this.errors = errors;
  }

  public void performCheck(boolean diagramCheck)
  {
  	IDeltaEngine engine = GlobalDeltaEngine.engine;
  	
    // check to see if we have a circularity amongst strata
    if (stratum.isCircular())
      errors.addError(
          new ErrorLocation(stratum), ErrorCatalog.STRATUM_CIRCULAR);
    
    // must have a name
    if (stratum.getName().length() == 0)
      errors.addError(
          new ErrorLocation(stratum), ErrorCatalog.STRATUM_NAME);
    
    // a destructive package can only be in another destructive package
    if (engine.getRoot() != stratum && stratum.isDestructive() && stratum.getParentStratum() != null && !stratum.getParentStratum().isDestructive())
    {
      errors.addError(
          new ErrorLocation(stratum), ErrorCatalog.PARENT_OF_DESTRUCTIVE_STRATUM_MUST_BE_DESTRUCTIVE_ALSO);
    }
  }
}
