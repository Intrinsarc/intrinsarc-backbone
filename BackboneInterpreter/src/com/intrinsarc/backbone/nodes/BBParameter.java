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
package com.intrinsarc.backbone.nodes;

import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.deltaengine.base.*;

public class BBParameter extends DEParameter
{
	private String literal;
	private LazyObject<DEAttribute> attribute;

	public BBParameter(String literal)
	{
		this.literal = literal;
	}

	public BBParameter(DEAttribute attribute)
	{
		this.attribute = new LazyObject<DEAttribute>(DEAttribute.class, attribute);
	}

	public BBParameter(UuidReference reference)
	{
		this.attribute = new LazyObject<DEAttribute>(DEAttribute.class, reference);
	}

	@Override
	public DEAttribute getAttribute()
	{
		if (attribute == null)
			return null;
		return attribute.getObject();
	}

	@Override
	public String getLiteral()
	{
		return literal;
	}

	@Override
	public void resolveLazyReferences()
	{
		if (attribute != null)
			attribute.resolve();
	}
}
