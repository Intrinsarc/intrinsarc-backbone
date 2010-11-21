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
package com.intrinsarc.backbone.nodes.simple;

import java.util.*;

public class BBSimpleLevel
{
	private BBSimpleLevel parent;
	private List<BBSimpleLevel> children = new ArrayList<BBSimpleLevel>();
	private List<BBSimplePart> parts = new ArrayList<BBSimplePart>();
	
	public BBSimpleLevel(BBSimpleLevel parent)
	{
		this.parent = parent;
	}
	
	public List<BBSimplePart> getParts(int moveUp)
	{
		BBSimpleLevel here = this;
		for (int lp = 0; lp < moveUp; lp++)
		{
			here = here.parent;
			if (here == null)
				return null;
		}
		return here.parts;
	}
	
	public void addLevel(BBSimpleLevel child)
	{
		children.add(child);
	}
	
	public void addPart(BBSimplePart part)
	{
		parts.add(part);
	}
}
