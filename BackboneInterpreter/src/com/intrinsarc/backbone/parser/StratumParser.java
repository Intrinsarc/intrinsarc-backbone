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
package com.intrinsarc.backbone.parser;

import java.util.*;

import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class StratumParser
{
	private Expect ex;
	
	public StratumParser(Expect ex)
	{
		this.ex = ex;
	}
	
	public BBStratum parse()
	{
		UuidReference reference = new UuidReference();
		final UuidReference parentRef = new UuidReference();
		boolean relaxed[] = {false};
		boolean destructive[] = {false};
		final List<String> dependsOn = new ArrayList<String>();
		
		// the parser
		ex.
			literal("stratum").
			uuid(reference);
		
		BBStratum bb = new BBStratum(reference);
		final BBStratum stratum[]= new BBStratum[]{bb};

		ex.
			guard("parent",
					new IAction() { public void act() { ex.uuid(parentRef); } }). 
			optionalLiteral("is-relaxed", relaxed).
			optionalLiteral("is-destructive", destructive).
			guard("depends-on",
					new IAction() { public void act() { ParserUtilities.parseUUIDs(ex, dependsOn); } }).
			literal("{").
			inside(
					new IAction() { public void act() { parseElements(stratum); } }).
			literal("}");
		
		// save away any values
		bb.setDestructive(destructive[0]);
		bb.setRelaxed(relaxed[0]);
		for (String dep : dependsOn)
			bb.settable_getRawDependsOn().add(GlobalNodeRegistry.registry.getNode(dep, DEStratum.class));
		bb.setParentUuid(parentRef.getUuid());
		bb.resolveLazyReferences();
		
		return bb;
	}
	
	protected void parseElements(final BBStratum[] stratum)
	{
		ex.zeroOrMore(
				new LiteralMatch("feature",
						new IAction() { public void act() { stratum[0].settable_getElements().add(new FeatureParser(ex).parse()); } }),
				new LiteralMatch("component",
						new IAction() { public void act() { stratum[0].settable_getElements().add(new ComponentParser(ex).parse()); } }),
				new LiteralMatch("interface",
						new IAction() { public void act() { stratum[0].settable_getElements().add(new InterfaceParser(ex).parse()); } }));
	}
}
