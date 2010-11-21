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
package com.intrinsarc.backbone.nodes.simple;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.deltaengine.base.*;

public abstract class BBSimpleElement extends BBSimpleObject
{
	public abstract String getUuid();
	public abstract Class<?> getImplementationClass();
	public abstract String getImplementationClassName();
	public abstract void resolveImplementation(BBSimpleElementRegistry registry) throws BBImplementationInstantiationException;
  public abstract boolean isLegacyBean();
	public abstract DEElement getComplex();
}
