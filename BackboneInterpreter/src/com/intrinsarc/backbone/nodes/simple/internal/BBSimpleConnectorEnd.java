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

public class BBSimpleConnectorEnd implements Comparable<BBSimpleConnectorEnd>
{
	private BBSimpleConnector connector;
	private int side;
	private BBSimplePart part;
	private BBSimplePort port;
	private Integer index;
	private String originalIndex;
	private boolean takeNext;
	
	public BBSimpleConnectorEnd(BBSimpleConnector connector, int side, BBSimplePart part, BBSimplePort port, Integer index, String originalIndex, boolean takeNext)
	{
		this.connector = connector;
		this.side = side;
		this.part = part;
		this.port = port;
		this.index = index;
		this.originalIndex = originalIndex;
		this.takeNext = takeNext;
	}

	public BBSimplePart getPart()
	{
		return part;
	}
	
	public BBSimplePort getPort()
	{
		return port;
	}

	public Integer getIndex()
	{
		return index;
	}

	public boolean isTakeNext()
	{
		return takeNext;
	}

	public String getOriginalIndex()
	{
		return originalIndex;
	}

	public BBSimpleConnector getConnector()
	{
		return connector;
	}

	public int getSide()
	{
		return side;
	}

	public int compareTo(BBSimpleConnectorEnd o)
	{
		if (index != null)
		{
			if (o.index == null)
				return -1;
			else
				return index.compareTo(o.index);
		}
		
		String a = originalIndex;
		String b = o.originalIndex;
		if (b == null)
			return -1;
		if (a == null)
			return 1;
		return a.compareTo(b);
	}
}
