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
package com.intrinsarc.backbone.nodes;

import java.io.*;

import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.deltaengine.base.*;

public class BBPortRemap implements Serializable
{
	private String uuid;
	private LazyObject<DEPort> port = new LazyObject<DEPort>(DEPort.class);
	
	public BBPortRemap(String uuid, DEPort port)
	{
		this.uuid = uuid;
		this.port.setObject(port);
	}

	public BBPortRemap(String uuid, UuidReference reference)
	{
		this.uuid = uuid;
		port.setReference(reference);
	}

	public String getUuid()
	{
		return uuid;
	}
	
	public DEPort getPort()
	{
		return port.getObject();
	}

	public void resolveLazyReferences()
	{
		port.resolve();
	}
}
