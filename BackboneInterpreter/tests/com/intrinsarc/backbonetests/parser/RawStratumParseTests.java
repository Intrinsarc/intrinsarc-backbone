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
package com.intrinsarc.backbonetests.parser;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;

public class RawStratumParseTests
{
	@Test
	public void testStratumParse()
	{
		String uuid[] = {""};
		String name[] = {""};
		boolean relaxed[] = {false};
		List<String> dependsOn = new ArrayList<String>();
		parseStratumDeclaration("stratum a is-relaxed;", uuid, name, relaxed, dependsOn);
		assertEquals("a", uuid[0]);
		assertTrue(relaxed[0]);
	}
	
	@Test(expected=ParseException.class)
	public void testStratumParseFail()
	{
		String uuid[] = {""};
		String name[] = {""};
		boolean relaxed[] = {false};
		List<String> dependsOn = new ArrayList<String>();
		parseStratumDeclaration("stratum a is-relaxed test", uuid, name, relaxed, dependsOn);
	}
	
	@Test
	public void testStratumParse2()
	{
		String uuid[] = {""};
		String name[] = {""};
		boolean relaxed[] = {false};
		List<String> dependsOn = new ArrayList<String>();
		parseStratumDeclaration("stratum a is-relaxed depends-on test1, test2-a-b;", uuid, name, relaxed, dependsOn);
		assertEquals("a", uuid[0]);
		assertTrue(relaxed[0]);
		assertEquals(2, dependsOn.size());
		assertEquals("test1", dependsOn.get(0));
		assertEquals("test2-a-b", dependsOn.get(1));
	}
	
	private void parseStratumDeclaration(String decl, String uuid[], String name[], boolean relaxed[], final List<String> dependsOn)
	{
		final Expect ex = new Expect(new Tokenizer("file", new StringReader(decl)));
		UuidReference ref = new UuidReference();
		ex.
			literal("stratum").
			uuid(ref).
			optionalLiteral("is-relaxed", relaxed).
			guard("depends-on",
					new IAction() { public void act() { parseNames(ex, dependsOn); } }).
			literal(";");
		uuid[0] = ref.getUuid();
		name[0] = ref.getName();
	}
	
	private void parseNames(final Expect ex, final List<String> dependsOn)
	{
		ex.oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{ public void act()
							{
								UuidReference ref = new UuidReference();
								ex.uuid(ref);
								dependsOn.add(ref.getUuid());
							}
						}));		
	}
}

