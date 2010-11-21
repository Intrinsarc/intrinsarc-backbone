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


public class Terminal implements IStateTerminalComponent, ITerminal
{
// start generated code
	private ITransition out_ITransitionRequired;
	public void setOut(ITransition out) { out_ITransitionRequired = out; }
// end generated code
	
	private boolean current;
	
	public boolean isCurrent()
	{
		return current;
	}
	
	public void moveToNextState()
	{
		if (out_ITransitionRequired != null)
			current = !out_ITransitionRequired.enter();
	}

	public ITransition getIn(Class<?> required)
	{
		return new ITransition()
		{
			public boolean enter()
			{
				current = true;
				return true;
			}
		};
	}
}
