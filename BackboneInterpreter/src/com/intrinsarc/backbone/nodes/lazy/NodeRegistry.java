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
package com.intrinsarc.backbone.nodes.lazy;

import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class NodeRegistry
{
  private Map<String, DEObject> nodes = new HashMap<String, DEObject>();
  private BBStratum root;
  public NodeRegistry()
  {
  }
  
  public NodeRegistry initialize()
  {
  	root = new BBStratum("model");
  	root.setName("model");
  	root.setDestructive(true);
  	addNode(root);
  	return this;
  }
  
  public BBStratum getRoot()
  {
  	return root;
  }
  
  public void addNode(DEObject node)
  {
    nodes.put(node.getUuid(), node);
  }
  
  public <T> T getNode(UuidReference reference, Class<T> cls)
  {
  	DEObject obj = nodes.get(reference.getUuid());
  	if (obj == null)
  		throw new BBNodeNotFoundException("Object " + cls.getName() + ", UUID = " + reference.getUuid() + " not found", reference.toString());
  	if (!(cls.isAssignableFrom(obj.getClass())))
  		throw new BBNodeNotFoundException(
  				"Found UUID = " + reference.getUuid() + ", name = " + obj.getName() +
  				" but it is not an instance of " + cls.getName() + ", class is " + obj.getClass(),
  				reference.toString());
  	return (T) obj;
  }
  
  public <T> T getNode(String uuid, Class<T> cls)
  {
  	DEObject obj = nodes.get(uuid);
  	if (obj == null)
  		throw new BBNodeNotFoundException("Object " + cls.getName() + ", UUID = " + uuid, "");
  	if (!(cls.isAssignableFrom(obj.getClass())))
  		throw new BBNodeNotFoundException(
  				"Found  UUID = " + uuid + ", name = " + obj.getName() +
  				" but it is not an instance of " + cls.getName() + ", class is " + obj.getClass(),
  				"");
  	return (T) obj;
  }

	public boolean hasNode(UuidReference uuidReference)
	{
		return nodes.containsKey(uuidReference.getUuid());
	}
}
