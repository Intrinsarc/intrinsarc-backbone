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

import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.parser.*;
import com.intrinsarc.backbone.parserbase.*;

public class LoadListParserTests
{
	@Test
	public void TestSimple() throws FileNotFoundException
	{
		String file = "tests/com/instrinsarc/backbonetests/parser/simple.loadlist";
		final Expect ex =
			new Expect(
					new Tokenizer(
							file, new FileReader(file)));
		
		BBLoadList ll = new LoadListParser(ex).parse();
		
		// test the contents
		assertEquals("BB", ll.settable_getVariables().get(0).getName());
		assertEquals("C:/temp/BB", ll.settable_getVariables().get(0).getValue());
		assertEquals("CC", ll.settable_getVariables().get(1).getName());
		assertEquals("C:/temp/BB", ll.settable_getVariables().get(1).getValue());

		assertEquals(7, ll.settable_getStrataDirectories().size());
		assertEquals("backbone", ll.settable_getStrataDirectories().get(1).getStratumName());
		assertEquals("$BB/base/backbone", ll.settable_getStrataDirectories().get(1).getPath());
		assertEquals("backbone :: backbone_profile", ll.settable_getStrataDirectories().get(6).getStratumName());
		assertEquals("$BB/base/backbone/backbone profile", ll.settable_getStrataDirectories().get(6).getPath());
	}
}
