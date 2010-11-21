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

import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class LazyObjects<T>
{
	private List<UuidReference> references;
	private List<T> objects = new ArrayList<T>();
	private Class<T> cls;
	
	public LazyObjects(List<UuidReference> references, Class<T> cls)
	{
		this.references = references;
		this.cls = cls;
	}
	
	public LazyObjects(Class<T> cls)
	{
		this.references = new ArrayList<UuidReference>();
		this.cls = cls;
	}
	
	public void addReference(UuidReference reference)
	{
		references.add(reference);
	}
	
	public void addObject(T object)
	{
		objects.add(object);
	}
	
	public void resolve()
	{
		for (UuidReference ref : references)
		{
			T obj = GlobalNodeRegistry.registry.getNode(ref, cls);
			objects.add(obj);
		}
		references = null;
	}
	
	public List<T> getObjects()
	{
		return objects;
	}

	public boolean isEmpty()
	{
		return references.isEmpty();
	}

	public Set<T> asObjectsSet()
	{
		return new HashSet<T>(objects);
	}

	public List<T> asObjectsList()
	{
		return objects;
	}
}
