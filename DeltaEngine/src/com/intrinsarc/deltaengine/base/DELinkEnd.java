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

public class DELinkEnd
{
	private DEPort port;
	private DEPart part;
	
	public DELinkEnd(DEPort port)
	{
		this.port = port;
	}
	
	public DELinkEnd(DEPort port, DEPart part)
	{
		this.port = port;
		this.part = part;
	}
	
	public DEPort getPort()
	{
		return port;
	}
	
	public DEPart getPart()
	{
		return part;
	}
		
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DELinkEnd))
			return false;
		DELinkEnd other = (DELinkEnd) obj;
		return port == other.port && part == other.part;
	}

	@Override
	public int hashCode()
	{
		if (part == null)
			return port.hashCode();
		if (port == null)
			return 0;
		return port.hashCode() ^ part.hashCode();
	}

	@Override
	public String toString()
	{
		if (part == null)
			return "DEComponentLinkEnd(" + port + ")";
		return "DEPartLinkEnd(" + port + ", " + part + ")";
	}

	/** a component link end has no part -- it links to a port */
	public boolean isComponentLinkEnd()
	{
		return part == null;
	}
}
