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

import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.simple.internal.*;
import com.intrinsarc.backbone.runtime.api.*;

public class Creator implements ICreate
{
	private int factoryNumber;
	public int getFactoryNumber() { return factoryNumber; }
	public void setFactoryNumber(int factory) { this.factoryNumber = factory; }

	private BBSimpleInstantiatedFactory context;
	private Object lastMemento;
	
	public Creator(BBSimpleInstantiatedFactory context)
	{
		this.context = context;
	}
	
	public Object create(Map<String, Object> values)
	{
		BBSimpleInstantiatedFactory me = new BBSimpleInstantiatedFactory(null, context, context.getFactory(factoryNumber));
		try
		{
			me.instantiate(values, null);
			lastMemento = me;
			return me;
		}
		catch (BBRuntimeException ex)
		{
			// translate this into a runtime exception so the program doesn't have to catch it
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	public void destroy(Object memento)
	{
		BBSimpleInstantiatedFactory me = (BBSimpleInstantiatedFactory) (memento == null ? lastMemento : memento);
		if (me == null)
			throw new IllegalStateException("No last memento available to destroy instantiated factory");
		try
		{
			me.destroy();
			lastMemento = null;
		}
		catch (BBRuntimeException ex)
		{
			// translate this into a runtime exception so the program doesn't have to catch it
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}
}
