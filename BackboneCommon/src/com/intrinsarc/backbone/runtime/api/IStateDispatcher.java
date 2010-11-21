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
package com.intrinsarc.backbone.runtime.api;


public interface IStateDispatcher
{
	public void setDDispatch(IEvent event, int index);	
	public void addDDispatch(IEvent event);
	public void removeDDispatch(IEvent event);
	
	public void setDStart(ITerminal start);
	public void setDEnd(ITerminal end, int index);
	
	public IEvent getDEvents_Provided(Class<?> required);
}

