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

import java.util.*;

import com.intrinsarc.deltaengine.base.*;

public class InterfaceErrorChecker
{
  private ErrorRegister errors;
  private DEStratum perspective;
  private DEInterface iface;

  public InterfaceErrorChecker(
      DEStratum perspective,
      DEInterface iface,
      ErrorRegister errors)
  {
    this.perspective = perspective;
    this.iface = iface;
    this.errors = errors;
  }

  public void performCheck(boolean diagramCheck)
  {
  	// if this is replacing, and we are not at home, don't bother
  	if (iface.isReplacement() && perspective != iface.getHomeStratum() || iface.isRetired(perspective))
  		return;
    
    // all things subsituted or resembled must refer to a composite
    for (DEElement resembled : iface.getRawResembles())
    {
      if (resembled.asInterface() == null)
        errors.addError(
            new ErrorLocation(iface), ErrorCatalog.INTERFACE_RESEMBLES_INTERFACE);
    }
    for (DEElement redef : iface.getReplaces())
    {
      if (redef.asInterface() == null)
        errors.addError(
            new ErrorLocation(iface), ErrorCatalog.INTERFACE_SUBSTITUTES_INTERFACE);
    }
    
    // an interface must be nested inside a stratum or package
    if (iface.getParent().asStratum() == null)
      errors.addError(
          new ErrorLocation(iface.getParent().getParent(), iface.getParent()), ErrorCatalog.ELEMENT_NOT_NESTED);
    
    // an interface must specify an implementation class
    String impl = iface.getImplementationClass(perspective);
    if (impl == null || impl.length() == 0)
    	errors.addError(new ErrorLocation(iface.getParent(), iface), ErrorCatalog.NO_IMPLEMENTATION);
    else
    if (!ElementErrorChecker.isValidClassName(impl))
    	errors.addError(new ErrorLocation(iface.getHomeStratum(), iface), ErrorCatalog.IMPLEMENTATION_INVALID);
    ElementErrorChecker.checkStratumPackageExists(errors, iface);
  }
}
