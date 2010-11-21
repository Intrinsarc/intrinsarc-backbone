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
package com.intrinsarc.backbonetests.loader;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.intrinsarc.backbone.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.deltaengine.base.*;


public class SimpleLoaderTests
{
	@Test
	public void loadSimple() throws Exception
	{
		// load in a simple program consisting of 2 strata
		List<BBStratum> strata = BackboneInterpreter.loadSystem(new File("./tests/com/intrinsarc/backbonetests/loader/backbone-files/system.loadlist"), ">>");
		
		// the top strata should be test
		BBStratum test = strata.get(strata.size() - 1);
		assertEquals("test", test.getName());
		// 2 components and 1 interface
		assertEquals(3, test.getChildElements().size());
		
		testInterfaceIFace(test);
		testComponentA(test);
		testComponentB(test);		
	}

	private void testInterfaceIFace(BBStratum test)
	{
		// test for interface
		DEInterface i = findInterface(test, "IFace");
		assertEquals(1, i.getRawAppliedStereotypes().size());
		DEAppliedStereotype iapplied = i.getRawAppliedStereotype();
		assertNotNull(iapplied);
		DEComponent istereo = iapplied.getStereotype();
		assertNotNull(istereo);
		assertEquals("interface", istereo.getUuid());
	}

	private void testComponentA(BBStratum test)
	{
		// test for A
		DEComponent a = findComponent(test, "A");
		assertNotNull(a);
		assertEquals(ComponentKindEnum.NORMAL, a.getComponentKind());
		
		// should have a single stereotype of type "component"
		assertEquals(1, a.getRawAppliedStereotypes().size());
		DEAppliedStereotype applied = a.getRawAppliedStereotype();
		assertNotNull(applied.getStereotype());
		DEComponent stereo = applied.getStereotype();
		assertEquals("component", stereo.getUuid());
		
		// should have an implementation property of test.A
		assertEquals(1, applied.getProperties().size());
		assertEquals("test.A", applied.getStringProperty("force-implementation"));
		
		// should have a single port named port which provides IFace
		Set<DeltaPair> ports = a.getDeltas(ConstituentTypeEnum.DELTA_PORT).getAddObjects();
		assertEquals(1, ports.size());
		DEPort port = ports.iterator().next().getConstituent().asPort();
		assertEquals("port", port.getName());
		assertEquals(1, port.getSetProvidedInterfaces().size());
		assertEquals(0, port.getSetRequiredInterfaces().size());
		// should be provides irun
		assertEquals("IRun", port.getSetProvidedInterfaces().iterator().next().getUuid());
		
		// should have a single attribute called age
		Set<DeltaPair> attrs = a.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getAddObjects();
		assertEquals(1, attrs.size());
		DEAttribute attr = attrs.iterator().next().getConstituent().asAttribute();
		assertEquals("age", attr.getName());
		assertEquals("int", attr.getType().getUuid());
		assertEquals(0, attr.getDefaultValue().size());		
	}

	private void testComponentB(BBStratum test)
	{
		// test for A
		DEComponent b = findComponent(test, "B");
		assertNotNull(b);
		assertEquals(ComponentKindEnum.NORMAL, b.getComponentKind());
		
		// should have a single stereotype of type "component"
		assertEquals(1, b.getRawAppliedStereotypes().size());
		DEAppliedStereotype applied = b.getRawAppliedStereotype();
		assertNotNull(applied.getStereotype());
		DEComponent stereo = applied.getStereotype();
		assertEquals("component", stereo.getUuid());
		
		// should have no implementation property set
		assertEquals(0, applied.getProperties().size());
		
		// should have a single port named p which provides IFace
		Set<DeltaPair> ports = b.getDeltas(ConstituentTypeEnum.DELTA_PORT).getAddObjects();
		assertEquals(1, ports.size());
		DEPort port = ports.iterator().next().getConstituent().asPort();
		assertEquals("p", port.getName());
		// no set interfaces, it is all inferred
		assertEquals(0, port.getSetProvidedInterfaces().size());
		assertEquals(0, port.getSetRequiredInterfaces().size());
		
		// should have a single unnamed part with a slot of age = 5
		Set<DeltaPair> parts = b.getDeltas(ConstituentTypeEnum.DELTA_PART).getAddObjects();
		assertEquals(1, parts.size());
		DEPart part = parts.iterator().next().getConstituent().asPart();
		assertEquals("A", part.getType().getName());
		assertEquals("e7341f4f-e556-4acf-82c7-0a06d75bbe50", part.getUuid());
		List<DESlot> slots = part.getSlots();
		assertEquals(1, slots.size());
		DESlot slot = slots.get(0);
		assertEquals("age", slot.getAttribute().getName());
		List<DEParameter> vals = slot.getValue();
		assertEquals(1, vals.size());
		DEParameter param = vals.get(0);
		assertEquals("5", param.getLiteral());
		
		// should have a single connector
		Set<DeltaPair> conns = b.getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getAddObjects();
		assertEquals(1, conns.size());
		DEConnector conn = conns.iterator().next().getConstituent().asConnector();
		assertEquals("p", conn.getOriginalPort(0).getName());
		assertNull(conn.getOriginalPart(0));
		assertEquals("port", conn.getOriginalPort(1).getName());
		assertEquals("e7341f4f-e556-4acf-82c7-0a06d75bbe50", conn.getOriginalPart(1).getUuid());
	}

	private DEComponent findComponent(BBStratum stratum, String name)
	{
		for (DEElement e : stratum.getChildElements())
			if (e.getName().equals(name) && e.asComponent() != null)
				return e.asComponent();
		return null;
	}

	private DEInterface findInterface(BBStratum stratum, String name)
	{
		for (DEElement e : stratum.getChildElements())
		{
			if (e.getName().equals(name) && e.asInterface() != null)
				return e.asInterface();
		}
		return null;
	}
}
