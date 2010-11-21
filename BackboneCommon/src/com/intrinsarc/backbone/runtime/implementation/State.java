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
package com.intrinsarc.backbone.runtime.implementation;

import com.intrinsarc.backbone.runtime.api.*;

public abstract class State
// start generated code
	// main port
 implements IEvent
{
	// required ports
	protected ITransition out;
	// provided ports
	protected ITransitionInImpl in_Provided = new ITransitionInImpl();

	// port setters and getters
	public void setOut(ITransition out) { this.out = out; }
	public ITransition getIn_Provided() { return in_Provided; }

// end generated code
	protected boolean current;

	private class ITransitionInImpl implements ITransition
	{
		public boolean enter()
		{
			current = true;
			return current;
		}
	}
	
	public boolean isCurrent()
	{
		return current;
	}
}
