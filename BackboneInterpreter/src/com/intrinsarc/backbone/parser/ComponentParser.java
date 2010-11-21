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

public class ComponentParser
{
	private Expect ex;
	
	public ComponentParser(Expect ex)
	{
		this.ex = ex;
	}
	
	public BBComponent parse()
	{
		final UuidReference uuid = new UuidReference();
		boolean normal[] = {false};
		boolean factory[] = {false};
		boolean placeholder[] = {false};
		boolean primitive[] = {false};
		boolean stereotype[] = {false};
		boolean bean[] = {false};
		boolean lifecycle[] = {false};
		final String implementation[] = {null};
		ex.literal("component");
		ex.
			uuid(uuid);
		
		final BBComponent c = new BBComponent(uuid);
		
		ex.
			optionalLiteral("is-normal", normal).
			optionalLiteral("is-factory", factory).
			optionalLiteral("is-placeholder", placeholder).
			optionalLiteral("is-primitive", primitive).
			optionalLiteral("is-stereotype", stereotype).
			optionalLiteral("is-legacy-bean", bean).
			optionalLiteral("has-lifecycle-callbacks", lifecycle).
			guard("implementation-class",
					new IAction() { public void act() { ex.literal(implementation); } }).
			guard("resembles",
					new IAction() { public void act() { ParserUtilities.parseUUIDs(ex, c.settable_getRawResembles()); } }).
			guard("replaces",
					new IAction() { public void act() { ParserUtilities.parseUUIDs(ex, c.settable_getReplaces()); } }).
			literal("{");
		
		// handle stereotypes
		List<BBAppliedStereotype> stereos = ParserUtilities.parseAppliedStereotype(ex);
		c.settable_getReplacedAppliedStereotypes().addAll(stereos);
		
		c.setComponentKind(ComponentKindEnum.NORMAL);
		if (primitive[0])
			c.setComponentKind(ComponentKindEnum.PRIMITIVE);
		if (stereotype[0])
			c.setComponentKind(ComponentKindEnum.STEREOTYPE);

		// possibly add a stereotype
		if (factory[0] || normal[0] || placeholder[0] || bean[0] || c.settable_getRawResembles().isEmpty() || implementation[0] != null)
		{
			BBAppliedStereotype stereo;
			if (stereos.isEmpty())
			{
				stereo = new BBAppliedStereotype();
				stereo.setStereotype(new UuidReference("component"));
				c.settable_getReplacedAppliedStereotypes().add(stereo);
			}
			else
				stereo = stereos.get(0);

			if (factory[0])
				addBooleanStereotypeProperty(stereo, DEComponent.FACTORY_STEREOTYPE_PROPERTY, true);
			if (placeholder[0])
				addBooleanStereotypeProperty(stereo, DEComponent.PLACEHOLDER_STEREOTYPE_PROPERTY, true);
			if (bean[0])
				addBooleanStereotypeProperty(stereo, DEComponent.LEGACY_BEAN_STEREOTYPE_PROPERTY, true);
			if (implementation[0] != null)
				addStringStereotypeProperty(stereo, DEComponent.IMPLEMENTATION_STEREOTYPE_PROPERTY, implementation[0]);			
			if (lifecycle[0])
				addBooleanStereotypeProperty(stereo, DEComponent.LIFECYCLE_CALLBACKS_PROPERTY, true);
		}
		
		ex.
			zeroOrMore(
					new LiteralMatch("delete-attributes",
							new IAction() { public void act() { parseDeletions(c.settable_getDeletedAttributes()); } }),
					new LiteralMatch("replace-attributes",
							new IAction() { public void act() { parseReplacedAttributes(c.settable_getReplacedAttributes()); } }),
					new LiteralMatch("attributes",
						new IAction() { public void act() { parseAddedAttributes(c.settable_getAddedAttributes()); } }),
						
					new LiteralMatch("delete-parts",
						new IAction() { public void act() { parseDeletions(c.settable_getDeletedParts()); } }),
					new LiteralMatch("replace-parts",
							new IAction() { public void act() { parseReplacedParts(c.settable_getReplacedParts()); } }),
					new LiteralMatch("parts",
							new IAction() { public void act() { parseAddedParts(c.settable_getAddedParts()); } }),
					
					new LiteralMatch("delete-ports",
						new IAction() { public void act() { parseDeletions(c.settable_getDeletedPorts()); } }),
					new LiteralMatch("replace-ports",
							new IAction() { public void act() { parseReplacedPorts(c.settable_getReplacedPorts()); } }),
					new LiteralMatch("ports",
							new IAction() { public void act() { parseAddedPorts(c.settable_getAddedPorts()); } }),
					
					new LiteralMatch("delete-connectors",
						new IAction() { public void act() { parseDeletions(c.settable_getDeletedConnectors()); } }),
					new LiteralMatch("replace-connectors",
							new IAction() { public void act() { parseReplacedConnectors(c.settable_getReplacedConnectors()); } }),
					new LiteralMatch("connectors",
							new IAction() { public void act() { parseAddedConnectors(c.settable_getAddedConnectors()); } }),
					
					new LiteralMatch("delete-port-links",
						new IAction() { public void act() { parseDeletions(c.settable_getDeletedPortLinks()); } }),
					new LiteralMatch("replace-port-links",
							new IAction() { public void act() { parseReplacedLinks(c.settable_getReplacedPortLinks()); } }),
					new LiteralMatch("port-links",
							new IAction() { public void act() { parseAddedLinks(c.settable_getAddedPortLinks()); } })).
			literal("}");

		return c;
	}
	
	private void addBooleanStereotypeProperty(BBAppliedStereotype stereo, String attrUUID, boolean value)
	{
		stereo.settable_getProperties().put(
				GlobalNodeRegistry.registry.getNode(attrUUID, DEAttribute.class),
				value ? "true" : "false");
	}

	private void addStringStereotypeProperty(BBAppliedStereotype stereo, String attrUUID, String value)
	{
		stereo.settable_getLazyProperties().put(
				new LazyObject<DEAttribute>(DEAttribute.class, new UuidReference(attrUUID)),
				value); 
	}

	private void parseReplacedLinks(final List<BBReplacedConnector> replacedLinks)
	{
		ex.literal().literal(":").
		oneOrMore(
			",",
			new LiteralMatch(
					new IAction()
					{
						public void act()
						{
							UuidReference reference = new UuidReference();
							ex.uuid(reference).literal("becomes");
							replacedLinks.add(new BBReplacedConnector(reference, parseConnector()));
						}
					})).
		literal(";");
	}

	private void parseAddedLinks(final List<BBConnector> addedLinks)
	{
		ex.literal().literal(":").
			oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								addedLinks.add(parseConnector());
							}
						})).
			literal(";");
	}
	
	private void parseReplacedConnectors(final List<BBReplacedConnector> replacedConnectors)
	{
		ex.literal().literal(":").
		oneOrMore(
			",",
			new LiteralMatch(
					new IAction()
					{
						public void act()
						{
							UuidReference reference = new UuidReference();
							ex.uuid(reference).literal("becomes");
							replacedConnectors.add(new BBReplacedConnector(reference, parseConnector()));
						}
					})).
		literal(";");
	}

	private void parseAddedConnectors(final List<BBConnector> addedConnectors)
	{
		ex.literal().literal(":").
			oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								addedConnectors.add(parseConnector());
							}
						})).
			literal(";");
	}
	
	private BBConnector parseConnector()
	{
		List<BBAppliedStereotype> applied = ParserUtilities.parseAppliedStereotype(ex);

		UuidReference reference = new UuidReference();
		ex.
			uuid(reference);
		
		final BBConnector connector = new BBConnector(reference);
		ex.oneOf(
				new LiteralMatch("delegates-from",
						new IAction() { public void act()
						{ connector.setDelegate(true); }}),
				new LiteralMatch("joins",
						new IAction() { public void act() {} }));
		ex.literal();
		end(connector, true);
		ex.literal("to");
		end(connector, false);
		
		return connector;
	}
	
	private void end(final BBConnector connector, final boolean from)
	{
		UuidReference portRef = new UuidReference();
		final UuidReference partRef = new UuidReference();
		ex.
			uuid(portRef);
		
		if (from)
			connector.setLazyFromPort(portRef);
		else
			connector.setLazyToPort(portRef);
		
		ex.
			guard("[",
				new IAction()
				{
					public void act()
					{
						ex.oneOf(
								new IntegerMatch(
										new IAction() { public void act()
										{
											String index = ex.next().getText();
											if (from)
												connector.setFromIndex(index);
											else
												connector.setToIndex(index);
										}}),
								new LiteralMatch("+",
										new IAction() { public void act()
										{
											ex.literal();
											if (from)
												connector.setFromTakeNext(true);
											else
												connector.setToTakeNext(true);
										}}),
								new LiteralMatch(
										new IAction() { public void act()
										{
											String index = ex.next().getText();
											if (from)
												connector.setFromIndex(index);
											else
												connector.setToIndex(index);
										}}));

						ex.literal("]");
					}
				}).
			guard("@",
				new IAction()
				{
					public void act()
					{
						ex.uuid(partRef);
						if (from)
							connector.setLazyFromPart(partRef);
						else
							connector.setLazyToPart(partRef);
					}
				});					
	}

	private void parseReplacedParts(final List<BBReplacedPart> replacedParts)
	{
		ex.literal().literal(":").
		oneOrMore(
			",",
			new LiteralMatch(
					new IAction()
					{
						public void act()
						{
							UuidReference reference = new UuidReference();
							ex.uuid(reference).literal("becomes");
							replacedParts.add(new BBReplacedPart(reference, parsePart()));
						}
					})).
		literal(";");	}

	private void parseAddedParts(final List<BBPart> addedParts)
	{
		ex.literal().literal(":").
			oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								addedParts.add(parsePart());
							}
						})).
			literal(";");
	}
	
	private BBPart parsePart()
	{
		List<BBAppliedStereotype> applied = ParserUtilities.parseAppliedStereotype(ex);
		
		UuidReference reference = new UuidReference();
		UuidReference typeRef = new UuidReference();
		ex.
			uuid(reference).
			literal(":").
			uuid(typeRef);
		

		final BBPart part = new BBPart(reference);
		part.setAppliedStereotypes(applied);		
		part.setType(typeRef);
		
		ex.
			guard("slots", new SlotsProcessor(part)).
			guard("port-remaps", new RemapProcessor(part));
		
		return part;
	}
	
	private class RemapProcessor implements IAction
	{
		private BBPart part;
		public RemapProcessor(BBPart part)
		{
			this.part = part;
		}
		
		public void act()
		{
			ex.literal(":").
				zeroOrMore(
						new NonSemiColonOrCommaMatch(null,
							new IAction() {
								public void act()
								{
									UuidReference portRef = new UuidReference();
									UuidReference originalPortRef = new UuidReference();
									ex.
										uuid(portRef).
										literal("maps-onto").
										uuid(originalPortRef);
									
									BBPortRemap remap = new BBPortRemap(originalPortRef.getUuid(), portRef);
									part.settable_getPortRemaps().add(remap);
								}
							}));
		}
	}

	
	private class SlotsProcessor implements IAction
	{
		private BBPart part;
		public SlotsProcessor(BBPart part)
		{
			this.part = part;
		}
		
		public void act()
		{
			ex.literal(":").
				zeroOrMore(
						new NonSemiColonOrCommaMatch("port-remaps",
							new IAction() {
								public void act()
								{
									final UuidReference attrRef = new UuidReference();
									ex.
										uuid(attrRef).
										guard("=",
											new IAction()
											{
												public void act()
												{
													BBSlot slot = new BBSlot(attrRef, parseParameters());
													part.settable_getSlots().add(slot);
												}
											});										
								}
							}));
		}
	}
	
	private void parseReplacedPorts(final List<BBReplacedPort> replacedPorts)
	{
		ex.literal().literal(":").
		oneOrMore(
			",",
			new LiteralMatch(
					new IAction()
					{
						public void act()
						{
							UuidReference reference = new UuidReference();
							ex.uuid(reference).literal("becomes");
							replacedPorts.add(new BBReplacedPort(reference.getUuid(), parsePort()));
						}
					})).
		literal(";");	}

	private void parseAddedPorts(final List<BBPort> addedPorts)
	{
		ex.literal().literal(":").
			oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								addedPorts.add(parsePort());
							}
						})).
			literal(";");
	}
	
	private BBPort parsePort()
	{
		UuidReference reference = new UuidReference();
		boolean create[] = {false};
		boolean hyper[] = {false};
		boolean autoConnect[] = {false};
		boolean ordered[] = {false};
		boolean beanMain[] = {false};
		boolean notBeanMain[] = {false};
		boolean beanNoName[] = {false};
		boolean wantsRequired[] = {false};
		
		ParserUtilities.parseAppliedStereotype(ex);
		ex.
			uuid(reference);
		final BBPort port = new BBPort(reference);

		ex.guard("[",
				new IAction()
				{
					public void act()
					{
						// read the lower, .. and upper
						int lower[] = {0};
						int upper[] = {0};
						final boolean upperUnbounded[] = {false};
						ex.
							integer(lower).
							literal("upto").
							guard("*", new IAction()
							{
								public void act()
								{
									upperUnbounded[0] = true;
								}
							});
						
						if (!upperUnbounded[0])
							ex.integer(upper);
						ex.literal("]");
						
						port.setLowerBound(lower[0]);
						if (upperUnbounded[0])
							port.setUpperBound(-1);
						else
							port.setUpperBound(upper[0]);
					}
				}).
			optionalLiteral("is-create-port", create).
			optionalLiteral("is-hyperport", hyper).
			optionalLiteral("is-autoconnect", autoConnect).
			optionalLiteral("is-ordered", ordered).
			optionalLiteral("force-bean-main", beanMain).
			optionalLiteral("force-not-bean-main", notBeanMain).
			optionalLiteral("force-bean-noname", beanNoName).
			optionalLiteral("wants-required-when-providing", wantsRequired);			

		if (create[0])
			port.setPortKind(PortKindEnum.CREATE);
		if (hyper[0])
			port.setPortKind(PortKindEnum.HYPERPORT);
		if (autoConnect[0])
			port.setPortKind(PortKindEnum.AUTOCONNECT);
		if (ordered[0])
			port.setOrdered(true);
		if (beanMain[0])
			port.setForceBeanMain(true);
		if (notBeanMain[0])
			port.setForceNotBeanMain(true);
		if (beanNoName[0])
			port.setForceBeanNoName(true);
		if (wantsRequired[0])
			port.setWantsRequiredWhenProviding(true);

		ex.
			guard("provides",
					new IAction() {
						public void act()
						{
							parseInterfaces(port.settable_getLazySetProvidedInterfaces());
						}
					}).
			guard("requires",
					new IAction() {
						public void act()
						{
							parseInterfaces(port.settable_getLazySetRequiredInterfaces());
						}
					});
		
		return port;
	}
	

	private void parseInterfaces(final LazyObjects<DEInterface> ifaces)
	{
		ex.
			oneOrMore(
				"&",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								UuidReference reference = new UuidReference();
								ex.uuid(reference);
								ifaces.addReference(reference);
							}
						}));
	}

	private void parseReplacedAttributes(final List<BBReplacedAttribute> replacedAttributes)
	{
		ex.literal().literal(":").
		oneOrMore(
			",",
			new LiteralMatch(
					new IAction()
					{
						public void act()
						{
							UuidReference ref = new UuidReference();
							ex.uuid(ref).literal("becomes");
							replacedAttributes.add(new BBReplacedAttribute(ref, parseAttribute()));
						}
					})).
		literal(";");
	}

	private void parseAddedAttributes(final List<BBAttribute> addedAttributes)
	{
		ex.literal().literal(":").
			oneOrMore(
				",",
				new LiteralMatch(
						new IAction()
						{
							public void act()
							{
								addedAttributes.add(parseAttribute());
							}
						})).
			literal(";");
	}
	
	private BBAttribute parseAttribute()
	{
		List<BBAppliedStereotype> applied = ParserUtilities.parseAppliedStereotype(ex);
		
		UuidReference ref = new UuidReference();
		UuidReference typeRef = new UuidReference();
		final boolean readOnly[] = {false};
		final boolean writeOnly[] = {false};
		ex.
			optionalLiteral("read-only", readOnly).
			optionalLiteral("write-only", writeOnly).
			uuid(ref);
		
		final BBAttribute attr = new BBAttribute(ref);
		
		attr.setAppliedStereotypes(applied);
		ex.
			literal(":").
			uuid(typeRef).
			guard("=",
					new IAction() {
						public void act()
						{
							attr.setDefaultValue(parseParameters());
						}
					});
		attr.setType(typeRef);
		
		return attr;
	}
	
	private List<DEParameter> parseParameters()
	{
		final List<DEParameter> params = new ArrayList<DEParameter>();
		
		ex.
			guard("(",
				new IAction() {
					public void act()
					{
						ex.oneOrMore(
								",",
								new ParameterMatch(
										new IAction()
										{
											public void act()
											{
												parseParameter(params);
											}
										})).literal(")");
					}
				},
				new IAction() {
					public void act()
					{
						ex.oneOf(
								new ParameterMatch(
										new IAction()
										{
											public void act()
											{
												parseParameter(params);
											}
										}));
					}
				});
		return params;
	}

	private void parseParameter(final List<DEParameter> params)
	{
		Token tok = ex.peek();
		if (tok.getType() == TokenType.LITERAL && !tok.getText().equals("null") && !tok.getText().equals("true") && !tok.getText().equals("false") && !tok.getText().equals("default"))
			params.add(new BBParameter(ex.nextUuid()));
		else
		if (tok.getType() == TokenType.STRING)
			params.add(new BBParameter("\"" + ex.next().getText() + "\""));
		else
			params.add(new BBParameter(ex.next().getText()));
	}

	private void parseDeletions(List<String> deletedUUIDs)
	{
		ex.literal().literal(":");
		ParserUtilities.parseUUIDs(ex, deletedUUIDs);
		ex.literal(";");
	}		
}
