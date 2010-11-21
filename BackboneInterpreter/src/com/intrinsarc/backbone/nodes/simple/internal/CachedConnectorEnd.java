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
package com.intrinsarc.backbone.nodes.simple.internal;

import com.intrinsarc.backbone.nodes.simple.*;

class CachedConnectorEnd
{
	private BBSimpleConnector conn;
	private int side;
	
	public CachedConnectorEnd(BBSimpleConnector conn, int side)
	{
		this.conn = conn;
		this.side = side;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof CachedConnectorEnd))
			return false;
		CachedConnectorEnd other = (CachedConnectorEnd) obj;
		return conn == other.conn && side == other.side;
	}

	@Override
	public int hashCode()
	{
		return conn.hashCode() ^ side;
	}
}
