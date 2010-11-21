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

import org.junit.*;

import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;

public class ExpectTests
{
	@Test
	public void expectLiteral()
	{
		makeExpect(" hello there").literal("hello").literal("there");
	}
	
	@Test
	public void expectOptionals()
	{
		final String res[] = {""};
		final Expect ex = makeExpect(" hello andrew goodbye"); 
		ex.literal("hello").
			oneOf(
					new Match(
							new LiteralPatternMatcher("andrew"),
							new IAction()
							{ public void act() { ex.literal("andrew"); res[0] = "optional"; }})).
			literal("goodbye");
		assertEquals("optional", res[0]);
	}
	
	@Test
	public void expectOptionalsMultiple()
	{
		assertEquals("andrew", acceptOptional(makeExpect(" hello andrew goodbye")));
		assertEquals("freddo", acceptOptional(makeExpect(" hello freddo goodbye")));
	}
	
	@Test(expected=ParseException.class)
	public void expectOptionalsBad()
	{
		acceptOptional(makeExpect(" hello foo goodbye"));
	}
	
	private String acceptOptional(Expect e)
	{
		final String res[] = {""};
		e.literal("hello").
			oneOf(
					new Match(
							new LiteralPatternMatcher("andrew"),
							new IAction() { public void act() { res[0] = "andrew"; }}),
					new Match(
							new LiteralPatternMatcher("freddo"),
							new IAction() { public void act() { res[0] = "freddo"; }}));
		return res[0];
	}
	
	@Test
	public void expectUUIDs()
	{
		UuidReference ref = new UuidReference();
		makeExpect("a1-2-3/hello/").uuid(ref);
		assertEquals("a1-2-3", ref.getUuid());
		assertEquals("hello", ref.getName());
		makeExpect("a1.2").uuid(ref);
		assertEquals("a1.2", ref.getUuid());
		makeExpect("Test").uuid(ref);
		assertEquals("Test", ref.getUuid());
	}
	
	
	private Expect makeExpect(String text)
	{
		return new Expect(new Tokenizer("file", new StringReader(text)));
	}
}
