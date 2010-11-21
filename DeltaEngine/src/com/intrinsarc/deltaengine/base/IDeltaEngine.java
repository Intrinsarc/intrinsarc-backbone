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

public interface IDeltaEngine
{
  DEObject locateObject(Object repositoryObject);
  /** phase this out when proper stereotypes appear with links to components */
  DEObject locateObjectForStereotype(String uuid);
  
  /** for the top of the hierarchical tree.  must always have a "top" even if we force one */
  DEStratum getRoot();
	DEStratum forceArtificialParent(Set<DEStratum> strata);
	void expandForStereotypesAndFactories(DEStratum perspective, DEElement element);
}
