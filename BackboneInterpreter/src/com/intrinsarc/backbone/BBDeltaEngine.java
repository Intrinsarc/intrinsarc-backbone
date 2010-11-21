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
package com.intrinsarc.backbone;

import java.util.*;

import com.intrinsarc.backbone.expanders.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.deltaengine.base.*;

public class BBDeltaEngine implements IDeltaEngine
{
  public BBDeltaEngine()
  {
  }

  /**
   * returns null if object has no Backbone representation
   */
  public DEObject locateObject(Object repositoryObject)
  {
  	return (DEObject) repositoryObject;
  }

  public BBStratum getRoot()
  {
    return GlobalNodeRegistry.registry.getRoot();
  }
  
	public DEStratum forceArtificialParent(Set<DEStratum> strata)
	{
		// not used for this engine
		return null;
	}

	public void expandForStereotypesAndFactories(DEStratum perspective, DEElement element)
	{
		if (element.isExpanded(perspective))
			return;
		element.setExpanded(perspective);
		
		// expand
		new FactoryExpander().expand(perspective, element);
		element.clearCache(perspective);
		new StateExpander().expand(perspective, element);
		element.clearCache(perspective);
		new StandardComponentExpander().expand(perspective, element);
		element.clearCache(perspective);
	}
	
	public DEObject locateObjectForStereotype(String uuid)
	{
		return GlobalNodeRegistry.registry.getNode(uuid, DEComponent.class);
	}
}
