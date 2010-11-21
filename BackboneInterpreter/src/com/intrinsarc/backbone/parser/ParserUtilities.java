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

public class ParserUtilities
{
	public static <T> void parseUUIDs(final Expect ex, final LazyObjects<T> references)
	{
		ex.oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								UuidReference reference = new UuidReference();
								ex.uuid(reference);
								references.addReference(reference);
							}
						}));		
	}
	
	public static <T> void parseUUIDs(final Expect ex, final List<String> references)
	{
		ex.oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								UuidReference reference = new UuidReference();
								ex.uuid(reference);
								references.add(reference.getUuid());
							}
						}));		
	}
	
	public static List<BBAppliedStereotype> parseAppliedStereotype(final Expect ex)
	{
		final List<BBAppliedStereotype> stereos = new ArrayList<BBAppliedStereotype>();
		ex.guard("\u00ab",
			new IAction()
			{
				final UuidReference stereo = new UuidReference();
				public void act()
				{
					ex.oneOrMore(",",
							new LiteralMatch(
									new IAction()
									{
										public void act()
										{
											final BBAppliedStereotype applied = new BBAppliedStereotype();
											ex.uuid(stereo);
											
											// resolve the stereotype
											applied.setStereotype(stereo);

											ex.
												guard(":",
													new IAction()
													{
														public void act()
														{
															ex.oneOrMore(",",
																	new LiteralMatch(
																			new IAction()
																			{
																				public void act()
																				{
																					parseAppliedValue(applied, ex);
																				}
																			}));
														}
													});
											
											stereos.add(applied);
										}
									}));
					ex.literal("\u00bb");
				}
			});
		return stereos;
	}
	

	private static void parseAppliedValue(BBAppliedStereotype applied, final Expect ex)
	{
		UuidReference attr = new UuidReference();
		
		LazyObject<DEAttribute> lattr = new LazyObject<DEAttribute>(DEAttribute.class, attr);
		final String value[] = {"true"};
		ex.
			uuid(attr).
			guard("=",
				new IAction()
				{
					public void act()
					{
						value[0] = ex.parameter().getText();
					}
				});
		applied.settable_getLazyProperties().put(lattr, value[0]);
	}
}
