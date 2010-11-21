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
package com.intrinsarc.deltaengine.errorchecking;

import java.util.*;

import com.intrinsarc.deltaengine.base.*;

public abstract class StratumIterator
{
	private List<? extends DEStratum> toCheck;
	private int startFrom;
	private boolean deepCheck;

	public StratumIterator(List<? extends DEStratum> toCheck, int startFrom, boolean deepCheck)
	{
		this.toCheck = toCheck;
		this.startFrom = startFrom;
		this.deepCheck = deepCheck;
	}
	
	public void iterate()
	{
  	int size = toCheck.size();
  	final int count[] = {0};
  	for (int lp = startFrom; lp < size; lp++)
  	{
  		// treat the current package as the perspective
  		DEStratum perspective = toCheck.get(lp);
  		boolean root = perspective == GlobalDeltaEngine.engine.getRoot();
  		
  		// if this is destructive, check all that came before the topmost
  		if (!root && (deepCheck || perspective.isDestructive() || joinsDestructive(perspective)))
  		{
	  		for (int before = 0; before <= lp; before++)
	  		{
	  			DEStratum current = toCheck.get(before);
	  			if (perspective.getTransitivePlusMe().contains(current))
	  			{
	  				process(perspective, current);
	  				count[0]++;
	  			}
	  		}
  		}
  		else
  		{
  			process(perspective, perspective);
  			count[0]++;
  		}
  	}
	}

	public static boolean joinsDestructive(DEStratum perspective)
	{
		// if this perspective joins 2 previously unconnected destructive strata, then force a full check
		Set<DEStratum> pkgs = perspective.getTransitive();
		for (DEStratum p : pkgs)
			if (p.isDestructive())
			{
				// look for another package that depends on this
				boolean another = false;
				for (DEStratum q : pkgs)
					if (q != p && q.getTransitive().contains(p))
					{
						another = true;
						break;
					}
				if (!another)
					return true;
			}
		return false;
	}

	protected abstract void process(DEStratum perspective, DEStratum current);
}

