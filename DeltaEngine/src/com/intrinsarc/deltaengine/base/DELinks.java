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

/**
 * DELinks holds a set of start to end port links.  A link summarises the connectors in a component.
 * Note that this can be navigated from both directions easily.
 * @author andrew
 *
 */
public class DELinks
{
	private Map<DELinkEnd, Set<DELinkEnd>> linksA = new HashMap<DELinkEnd, Set<DELinkEnd>>();
	private Map<DELinkEnd, Set<DELinkEnd>> linksB = new HashMap<DELinkEnd, Set<DELinkEnd>>();
	
	public DELinks()
	{
	}
	
	public void addLink(DELinkEnd start, DELinkEnd finish)
	{
		addLink(linksA, start, finish);
		addLink(linksB, finish, start);
	}
	
	public Set<DELinkEnd> getEnds(DELinkEnd start)
	{
		Set<DELinkEnd> ends = new HashSet<DELinkEnd>();
		Set<DELinkEnd> endsA = linksA.get(start);
		Set<DELinkEnd> endsB = linksB.get(start);
		if (endsA != null)
			ends.addAll(endsA);
		if (endsB != null)
			ends.addAll(endsB);
		return ends;
	}

	public DELinks filterByStart(DELinkEnd start)
	{
		Set<DELinkEnd> ends = getEnds(start);
		DELinks links = new DELinks();
		for (DELinkEnd end : ends)
			links.addLink(start, end);
		return links;
	}
	
	public Set<DELinkEnd> getUnidirectionalKeys()
	{
		return linksA.keySet();
	}

	public Set<DELinkEnd> getBidirectionalKeys()
	{
		Set<DELinkEnd> ends = new HashSet<DELinkEnd>(linksA.keySet());
		ends.addAll(linksB.keySet());
		return ends;
	}

	private void addLink(Map<DELinkEnd, Set<DELinkEnd>> links, DELinkEnd start, DELinkEnd finish)
	{
		Set<DELinkEnd> ends = links.get(start);
		if (ends == null)
		{
			ends = new HashSet<DELinkEnd>();
			links.put(start, ends);
		}
		ends.add(finish);
	}

	public boolean isEmpty()
	{
		return linksA.isEmpty();
	}

	@Override
	public String toString()
	{
		return "DELinks(" + linksA + " | " + linksB + ")";
	}
}
