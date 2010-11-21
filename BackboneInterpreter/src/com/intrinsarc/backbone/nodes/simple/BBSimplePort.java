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

import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.deltaengine.base.*;

public class BBSimplePort extends BBSimpleObject
{
	private String name;
	private transient String rawName;
	private List<BBSimpleInterface> provides;
	private List<BBSimpleInterface> requires;
  private int upperBound;
  private int lowerBound;
	private DEPort complex;
	private boolean resolved;
	private BBSimpleComponent owner;
	private boolean beanMain;
	private boolean beanNoName;
	private boolean wantsRequiredWhenProviding;
	private boolean complexProvided;
	
	public BBSimplePort(BBSimpleElementRegistry registry, DEComponent component, DEPort port, BBSimpleComponent owner)
	{
		rawName = port.getName();
		name = registry.makeName(rawName);
		upperBound = port.getUpperBound();
		lowerBound = port.getLowerBound();
		this.owner = owner;
		Set<? extends DEInterface> provided = component.getProvidedInterfaces(registry.getPerspective(), port);
		if (!provided.isEmpty())
		{
			provides = new ArrayList<BBSimpleInterface>();
			for (DEInterface prov : provided)
				provides.add(registry.retrieveInterface(prov));
		}
		Set<? extends DEInterface> required = component.getRequiredInterfaces(registry.getPerspective(), port);
		if (!required.isEmpty())
		{
			requires = new ArrayList<BBSimpleInterface>();
			for (DEInterface req : component.getRequiredInterfaces(registry.getPerspective(), port))
				requires.add(registry.retrieveInterface(req));
		}
		complex = port;
		
		// is this a bean main?
		beanMain = component.getBeanMainPorts(registry.getPerspective()).contains(complex);
		beanNoName = component.getBeanNoNamePorts(registry.getPerspective()).contains(complex);
		wantsRequiredWhenProviding = port.isWantsRequiredWhenProviding();
		
		// a port is complex provided if it has 2 or more provided ports and is not a bean main
		complexProvided = !beanMain && component.getProvidedInterfaces(registry.getPerspective(), complex).size() > 1;
	}

	public void resolveImplementation(BBSimpleElementRegistry registry) throws BBImplementationInstantiationException
	{
		if (resolved)
			return;
		resolved = true;
		
		if (provides != null)
			for (BBSimpleInterface prov : provides)
				prov.resolveImplementation(registry);
		if (requires != null)
			for (BBSimpleInterface req : requires)
				req.resolveImplementation(registry);
	}

	public DEPort getComplexPort()
	{
		return complex;
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getRawName()
	{
		return rawName;
	}

	public List<BBSimpleInterface> getProvides()
	{
		if (provides == null)
			return new ArrayList<BBSimpleInterface>();
		return provides;
	}

	public List<BBSimpleInterface> getRequires()
	{
		if (requires == null)
			return new ArrayList<BBSimpleInterface>();
		return requires;
	}

	public int getUpperBound()
	{
		return upperBound;
	}

	public int getLowerBound()
	{
		return lowerBound;
	}

	public BBSimpleComponent getOwner()
	{
		return owner;
	}
	
	public boolean isIndexed()
	{
		return upperBound > 1 || upperBound == -1;
	}
	
	public boolean suppressGeneration()
	{
		return false;
	}

	public boolean isHyperport()
	{
		return complex.getPortKind() == PortKindEnum.HYPERPORT;
	}

	@Override
	public Map<String, List<? extends BBSimpleObject>> getChildren(boolean top)
	{
		Map<String, List<? extends BBSimpleObject>> children = new LinkedHashMap<String, List<? extends BBSimpleObject>>();
		children.put("provided interfaces", provides);
		children.put("required interfaces", requires);
		return children;
	}

	@Override
	public String getTreeDescription()
	{
		return
			"Port " + name + ", lower bound = " + lowerBound +
			", upper bound = " + upperBound;
	}
	
	public boolean isBeanMain()
	{
		return beanMain;
	}

	public boolean isBeanNoName()
	{
		return beanNoName;
	}
	
	public boolean isComplexProvided()
	{
		return complexProvided;
	}
	
	public boolean isWantsRequiredWhenProviding()
	{
		return wantsRequiredWhenProviding;
	}
}
