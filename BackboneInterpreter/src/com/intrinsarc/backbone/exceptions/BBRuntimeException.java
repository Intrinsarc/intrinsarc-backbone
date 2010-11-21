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

public class BBRuntimeException extends Exception
{
	private BBSimpleElement element;
	
	public BBRuntimeException(String message, BBSimpleElement element)
	{
		super(message);
		this.element = element;
	}

	public BBRuntimeException(String message, BBSimpleElement element, Throwable cause)
	{
		super(message, cause);
		this.element = element;
	}
	
	public BBSimpleElement getElement()
	{
		return element;		
	}
}
