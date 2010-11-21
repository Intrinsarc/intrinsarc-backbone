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

public class ErrorChecker
{
  private ErrorRegister errors;

  public ErrorChecker(ErrorRegister errors)
  {
    this.errors = errors;
  }

  public void performCheck(final DEStratum perspective, DEObject element, final boolean diagramCheck)
  {
    element.visit(
      new DEObjectVisitorAdapter()
      {
        @Override
        public void visitInterface(DEInterface iface)
        {
        	if (perspective != GlobalDeltaEngine.engine.getRoot())
        	{
	      		new InterfaceErrorChecker(perspective, iface, errors).performCheck(diagramCheck);
	      		new ElementErrorChecker(perspective, iface, errors).performCheck(diagramCheck);
        	}
        	else
        		errors.addError(
        				new ErrorLocation(perspective, iface), ErrorCatalog.ELEMENT_NOT_AT_TOPLEVEL);
        }

        @Override
        public void visitComponent(DEComponent component)
        {
        	if (perspective != GlobalDeltaEngine.engine.getRoot())
        	{
	          new ComponentErrorChecker(perspective, component, errors).performCheck(diagramCheck);
	          new ElementErrorChecker(perspective, component, errors).performCheck(diagramCheck);
        	}
        	else
        		errors.addError(
        				new ErrorLocation(perspective, component), ErrorCatalog.ELEMENT_NOT_AT_TOPLEVEL);
        }

        @Override
        public void visitStratum(DEStratum stratum)
        {
          new StratumErrorChecker(stratum, errors).performCheck(diagramCheck);
        }          
      });
  }
}
