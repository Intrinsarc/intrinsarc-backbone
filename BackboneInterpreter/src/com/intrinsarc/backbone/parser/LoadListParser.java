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

import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.parserbase.*;

public class LoadListParser
{
	private Expect ex;

	public LoadListParser(Expect ex)
	{
		this.ex = ex;
	}
	
	public BBLoadList parse()
	{
		final BBLoadList list = new BBLoadList();
		ex.
			literal("load-list").
			literal("{").
			guard("variables", new IAction() { public void act() { parseVariables(list); } }).
			guard("directories", new IAction() { public void act() { parseDirectories(list); } }).
			literal("}");
		return list;
	}
	
	private void parseVariables(final BBLoadList list)
	{
		ex.literal(":").oneOrMore(",",
				new StringMatch(
						new IAction()
						{
							public void act()
							{
								String name[] = {""};
								String value[] = {""};
								ex.string(name).literal("=").string(value);
								list.settable_getVariables().add(new BBVariableDefinition(name[0], value[0]));
							}
						})).literal(";");
	}

	private void parseDirectories(final BBLoadList list)
	{
		ex.literal(":").oneOrMore(",",
				new StringMatch(
						new IAction()
						{
							public void act()
							{
								String name[] = {""};
								String path[] = {""};
								ex.string(name).literal("=").string(path);
								list.settable_getStrataDirectories().add(new BBStratumDirectory(path[0], name[0]));
							}
						})).literal(";");
	}
}
