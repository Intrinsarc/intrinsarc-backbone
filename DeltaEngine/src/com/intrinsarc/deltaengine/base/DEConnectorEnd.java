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


public class DEConnectorEnd implements Comparable<DEConnectorEnd>
{
	private DEConnector connector;
	private int side;
	private DEPart part;
	private DEPort port;
	private Integer index;
	private String originalIndex;
	private boolean takeNext;
	
	public DEConnectorEnd(DEConnector connector, int side, DEPart part, DEPort port, Integer index, String originalIndex, boolean takeNext)
	{
		this.connector = connector;
		this.side = side;
		this.part = part;
		this.port = port;
		this.index = index;
		this.originalIndex = originalIndex;
		this.takeNext = takeNext;
	}

	public DEPart getPart()
	{
		return part;
	}
	
	public DEPort getPort()
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

	public DEConnector getConnector()
	{
		return connector;
	}

	public int getSide()
	{
		return side;
	}

	public int compareTo(DEConnectorEnd o)
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

	public boolean equals(Object obj)
	{
		if (!(obj instanceof DEConnectorEnd))
			return false;
		DEConnectorEnd o = (DEConnectorEnd) obj;
		return
			connector == o.connector &&
			side == o.side &&
			part == o.part &&
			port == o.port &&
			equals(index, o.index) &&
			equals(originalIndex, o.originalIndex) &&
			takeNext == o.takeNext;  
	}

	private boolean equals(Object o1, Object o2)
	{
		if (o1 == null && o2 == null)
			return true;
		if (o1 == null || o2 == null)
			return false;
		return o1.equals(o2);
	}

	public int hashCode()
	{
		return port.hashCode();
	}
	
	
}
