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
package com.intrinsarc.backbone.exceptions;

import com.intrinsarc.backbone.nodes.simple.*;
import com.intrinsarc.deltaengine.base.*;

public class BBImplementationInstantiationException extends BBInterpreterException
{
	private String elementName;
	private String elementUuid;
	
	public BBImplementationInstantiationException(String message, BBSimpleElement element)
	{
		super(message);
		this.elementUuid = element.getUuid();
	}

	public BBImplementationInstantiationException(String message, BBSimpleElement element, Exception cause)
	{
		super(message, cause);
		this.elementUuid = element.getUuid();
	}
	
	public BBImplementationInstantiationException(String message, DEElement element)
	{
		super(message);
		this.elementName = element.getName();
		this.elementUuid = element.getUuid();
	}

	public BBImplementationInstantiationException(String message, DEElement element, Exception cause)
	{
		super(message, cause);
		this.elementName = element.getName();
		this.elementUuid = element.getUuid();
	}
	
	public String getElementUuid()
	{
		return elementUuid;
	}

	public String getElementName()
	{
		return elementName;
	}
}
