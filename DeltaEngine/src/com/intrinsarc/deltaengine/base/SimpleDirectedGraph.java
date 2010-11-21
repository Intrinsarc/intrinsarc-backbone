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

public class SimpleDirectedGraph<N>
{
	private Set<N> nodes = new LinkedHashSet<N>();
	private Map<N, Set<N>> edges = new HashMap<N, Set<N>>();

	public SimpleDirectedGraph()
	{
	}

	public void addEdge(N start, N end)
	{
		if (!nodes.contains(start) || !nodes.contains(end))
			throw new IllegalStateException("Adding an edge without the nodes");
		Set<N> e = edges.get(start);
		if (e == null)
		{
			e = new HashSet<N>();
			edges.put(start, e);
		}
		e.add(end);
	}

	public Set<N> getOutgoing(N node)
	{
		Set<N> out = edges.get(node);
		return out == null ? new HashSet<N>() : new LinkedHashSet<N>(out);
	}

	public boolean pathExists(N start, N end)
	{
		return pathExists(new HashSet<N>(), start, end);
	}

	private boolean pathExists(HashSet<N> visited, N start, N end)
	{
		if (start == end)
			return true;
		if (visited.contains(start))
			throw new IllegalStateException("Graph is not directed!");
		visited.add(start);
		for (N next : getOutgoing(start))
			if (pathExists(visited, next, end))
				return true;
		return false;
	}

	public List<N> makeTopologicalSort(boolean tolerateCircularity)
	{
		List<N> sorted = new ArrayList<N>();
		makeTopologicalSort(new LinkedHashSet<N>(nodes), sorted, tolerateCircularity);
		return sorted;
	}

	private void makeTopologicalSort(HashSet<N> left, List<N> sorted, boolean tolerateCircularity)
	{
		// find anything in left doesn't depend on left (apart from itself)
		while (left.size() != 0)
		{
			List<N> move = new ArrayList<N>();
			for (N l : left)
				if (getOutgoing(l).isEmpty() || !getOutgoing(l).removeAll(left))
					move.add(l);
			left.removeAll(move);
			sorted.addAll(move);
			if (move.isEmpty())
			{
				if (tolerateCircularity)
				{
					// remove the first and carry on
					N top = left.iterator().next();
					left.remove(top);
					sorted.add(top);
				}
				else
				{
					System.out.println("$$ graph = " + left);
					for (N n : edges.keySet())
						System.out.println("  $$ Node = " + n + ", edges = " + edges.get(n));
					throw new IllegalStateException("Graph is not directed!");
				}
			}
		}
	}

	public void addNode(N node)
	{
		nodes.add(node);
	}

	public boolean containsNode(N node)
	{
		return nodes.contains(node);
	}
}
