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
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class FeatureParser
{
	private Expect ex;
	
	public FeatureParser(Expect ex)
	{
		this.ex = ex;
	}
	
	public BBRequirementsFeature parse()
	{
		UuidReference ref = new UuidReference();

		ex.literal();
		ex.
			uuid(ref);
		
		final BBRequirementsFeature f = new BBRequirementsFeature(ref);

		ex.
			guard("resembles",
					new IAction() { public void act() { ParserUtilities.parseUUIDs(ex, f.settable_getRawResembles()); } }).
			guard("replaces",
					new IAction() { public void act() { ParserUtilities.parseUUIDs(ex, f.settable_getRawReplaces()); } }).
			literal("{");

		// handle stereotypes
		List<BBAppliedStereotype> stereos = ParserUtilities.parseAppliedStereotype(ex);
		f.settable_getReplacedAppliedStereotypes().addAll(stereos);
		
		ex.
			zeroOrMore(
					new LiteralMatch("delete-subfeatures",
							new IAction() { public void act() { parseDeletions(f.settable_getDeletedSubfeatures()); } }),
					new LiteralMatch("replace-subfeatures",
							new IAction() { public void act() { parseReplacedSubfeatures(f.settable_getReplacedSubfeatures()); } }),
					new LiteralMatch("subfeatures",
						new IAction() { public void act() { parseAddedSubfeatures(f.settable_getAddedSubfeatures()); } })).
		  literal("}");
		
		return f;
	}

	private void parseReplacedSubfeatures(List<BBReplacedRequirementsFeatureLink> replacedSubfeatures)
	{
		ex.literal().literal(":").
		oneOrMore(
			",",
			new LiteralMatch(
					new IAction()
					{
						public void act()
						{
							ParserUtilities.parseAppliedStereotype(ex);
							UuidReference ref = new UuidReference();
							ex.uuid(ref).literal("becomes");
							parseSubfeature();
						}
					})).
		literal(";");	}

	private void parseAddedSubfeatures(List<DERequirementsFeatureLink> addedSubfeatures)
	{
		ex.literal().literal(":").
			oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								parseSubfeature();
							}
						})).
			literal(";");
	}
	
	private BBRequirementsFeatureLink parseSubfeature()
	{
		List<BBAppliedStereotype> applied = ParserUtilities.parseAppliedStereotype(ex);
		
		UuidReference ref = new UuidReference();
		final SubfeatureKindEnum kind[] = {null};
		ex.
			uuid(ref);
		
		final BBRequirementsFeatureLink sub = new BBRequirementsFeatureLink(ref);
		sub.setAppliedStereotypes(applied);
		
		ex.
			oneOf(
					new LiteralMatch("is-mandatory",
							new IAction() { public void act() { kind[0] = SubfeatureKindEnum.MANDATORY; } }),
					new LiteralMatch("is-optional",
							new IAction() { public void act() { kind[0] = SubfeatureKindEnum.OPTIONAL; } }),
					new LiteralMatch("is-one-of",
							new IAction() { public void act() { kind[0] = SubfeatureKindEnum.ONE_OF; } }),
					new LiteralMatch("is-one-or-more",
							new IAction() { public void act() { kind[0] = SubfeatureKindEnum.ONE_OR_MORE; } })							
					).literal();

		return sub;
	}

	private void parseDeletions(List<String> deletedUUIDs)
	{
		ex.literal().literal(":");
		ParserUtilities.parseUUIDs(ex, deletedUUIDs);
		ex.literal(";");
	}
}
