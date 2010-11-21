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
package com.intrinsarc.backbonetests.logic;

import java.util.*;

import org.junit.*;

import com.intrinsarc.backbone.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.deltaengine.base.*;
import com.intrinsarc.deltaengine.errorchecking.*;

public class TestBase
{
	protected BBStratum top, global, a, aa, ab, aba, b, c, d, e, lonely;
	protected BBComponent componentStereo, interfaceStereo, resemblingStereo;
	protected BBAppliedStereotype appliedComponentStereo, appliedInterfaceStereo;
	protected BBAttribute impl;
	
	@Before
	public void setup()
	{
		BBDeltaEngine engine = new BBDeltaEngine();
		GlobalDeltaEngine.engine = engine;
		GlobalNodeRegistry.reset();
		
		top = engine.getRoot();
		a = new BBStratum("a");
		aa = new BBStratum("aa");
		ab = new BBStratum("ab");
		aba = new BBStratum("aba");
		global = new BBStratum("global");
		b = new BBStratum("b");
		c = new BBStratum("c");
		d = new BBStratum("d");
		e = new BBStratum("e");
		lonely = new BBStratum("lonely");

		c.setRelaxed(true);
		top.setDestructive(true);
		e.setDestructive(true);
		
		a.settable_getRawDependsOn().add(ab);
		a.settable_getRawDependsOn().add(global);
		ab.settable_getRawDependsOn().add(aba);
		b.settable_getRawDependsOn().add(a);

		c.settable_getRawDependsOn().add(a);
		e.settable_getRawDependsOn().add(b);
		d.settable_getRawDependsOn().add(b);
		d.settable_getRawDependsOn().add(c);
		
		// set up some stereotypes
		componentStereo = new BBComponent(new UuidReference("component"));
		componentStereo.setComponentKind(ComponentKindEnum.STEREOTYPE);
		componentStereo.setParent(global);
		impl = new BBAttribute(new UuidReference(DEElement.IMPLEMENTATION_STEREOTYPE_PROPERTY));
		componentStereo.settable_getAddedAttributes().add(impl);
		appliedComponentStereo = new BBAppliedStereotype();
		appliedComponentStereo.setStereotype(componentStereo);
		appliedComponentStereo.settable_getProperties().put(impl, "foo");
		interfaceStereo = new BBComponent(new UuidReference("interface"));
		interfaceStereo.setComponentKind(ComponentKindEnum.STEREOTYPE);
		interfaceStereo.setParent(global);
		interfaceStereo.settable_getAddedAttributes().add(impl);
		appliedInterfaceStereo = new BBAppliedStereotype();
		appliedInterfaceStereo.setStereotype(componentStereo);
		appliedInterfaceStereo.settable_getProperties().put(impl, "foo");
		
		postSetup();
	}
	
	protected void postSetup()
	{
		informAboutParents();
	}
	
	protected void informAboutParents()
	{
		aa.setParentAndTellChildren(a);
		ab.setParentAndTellChildren(a);
		aba.setParentAndTellChildren(ab);
		a.setParentAndTellChildren(top);
		b.setParentAndTellChildren(top);
		c.setParentAndTellChildren(top);
		d.setParentAndTellChildren(top);
		e.setParentAndTellChildren(top);
		lonely.setParentAndTellChildren(top);
		global.setParentAndTellChildren(top);
		top.setParentAndTellChildren(null);
	}

	protected void testPackages(String message, Collection<? extends DEStratum> pkgs, DEStratum[] want)
	{
		Set<DEStratum> setPkgs = new HashSet<DEStratum>(pkgs);
		Set<DEStratum> wantPkgs = new HashSet<DEStratum>();
		for (DEStratum w : want)
			wantPkgs.add(w);
		Assert.assertEquals(message, wantPkgs, setPkgs);
	}
	
	/**
	 * allow the constituents to be confirmed, from a particular perspective
	 * @param element
	 * @param type
	 * @param perspective
	 * @param uuids
	 */
	protected void testConstituents(String message, DEElement element, ConstituentTypeEnum type, DEStratum perspective, DEConstituent[] added, BBReplacedConstituent[] replacedNowAdded)
	{
		Set<DeltaPair> wanted = new HashSet<DeltaPair>();
		if (added != null)
			for (DEConstituent add : added)
				wanted.add(new DeltaPair(add.getUuid(), add));
		if (replacedNowAdded != null)
			for (BBReplacedConstituent repl : replacedNowAdded)
				wanted.add(new DeltaPair(repl.getUuid(), repl.getReplacement()));
		
		Set<DeltaPair> found = element.getDeltas(type).getConstituents(perspective);
		Assert.assertEquals(message, wanted, found);
	}
	
	/**
	 * ensure we have the same actual elements as we are expecting
	 * @param message
	 * @param actual
	 * @param expect
	 */
	protected void testElements(String message, Set<? extends DEElement> actual, DEElement[] expect)
	{
		Set<DEElement> expectElements = new HashSet<DEElement>();
		for (DEElement e : expect)
			expectElements.add(e);
		Assert.assertEquals(message, expectElements, actual);
	}
	
	protected void makePrimitive(String name, BBComponent[] component, BBPort[] provPort, BBInterface provIface, BBInterface provIface_req, boolean manyProv, BBPort[] reqPort, BBInterface reqIface, BBInterface reqIface_prov, boolean manyReq)
	{
		// make the component
		BBComponent comp = new BBComponent(new UuidReference(name));
		comp.settable_getReplacedAppliedStereotypes().add(appliedComponentStereo);
		a.settable_getElements().add(comp);
		comp.setParent(a);
		component[0] = comp;
		
		BBPort prov = null;
		BBPort req = null;
		if (provPort != null)
		{
			BBPort port = new BBPort(new UuidReference(name + "ProvPort"));
			port.setName(port.getUuid());
			comp.settable_getAddedPorts().add(port);
			port.setParent(comp);
			if (provIface != null)
				port.settable_getSetProvidedInterfaces().add(provIface);
			if (provIface_req != null)
				port.settable_getSetRequiredInterfaces().add(provIface_req);
			provPort[0] = port;
			prov = port;
			
			// is this many?
			if (manyProv)
			{
				port.setLowerBound(0);
				port.setUpperBound(-1);
			}
		}

		if (reqPort != null)
		{
			BBPort port = new BBPort(new UuidReference(name + "ReqPort"));
			port.setName(port.getUuid());
			comp.settable_getAddedPorts().add(port);
			port.setParent(comp);
			if (reqIface != null)
				port.settable_getSetRequiredInterfaces().add(reqIface);
			if (reqIface_prov != null)
				port.settable_getSetProvidedInterfaces().add(reqIface_prov);
			reqPort[0] = port;
			req = port;
			
			// is this many?
			if (manyReq)
			{
				port.setLowerBound(0);
				port.setUpperBound(-1);
			}
		}
		
		// if we have both ports, make a port link
		if (prov != null && req != null)
		{
			BBConnector conn = new BBConnector("bar");
			conn.setName("bar");
			conn.setToPort(prov);
			conn.setFromPort(req);
			conn.setParent(comp);
			comp.settable_getAddedPortLinks().add(conn);
			conn.setFromIndex("0");
			conn.setToIndex("0");
		}
	}
	
	protected void checkErrors()
	{
		ErrorRegister errors = new ErrorRegister();
		StratumErrorDetector detector = new StratumErrorDetector(errors);
		List<DEStratum> system = new ArrayList<DEStratum>(top.determineOrderedPackages(false, true));
		
    detector.checkAllInOrder(system, system.size() - 1, true, null);
		if (errors.countErrors() > 0)
		{
			Map<ErrorLocation, Set<ErrorDescription>> all = errors.getAllErrors();
			for (ErrorLocation loc : all.keySet())
				for (ErrorDescription desc : all.get(loc))
					System.out.println("  >> " + loc + ": " + desc);
		}
		Assert.assertEquals("Errors in model: ", 0, errors.countErrors());
	}
	
	protected void expectErrors(ErrorDescription[] expectErrorDescriptions)
	{
		ErrorRegister errors = new ErrorRegister();
		StratumErrorDetector detector = new StratumErrorDetector(errors);
		List<DEStratum> system = new ArrayList<DEStratum>(top.determineOrderedPackages(false, true));
		
    detector.checkAllInOrder(system, system.size() - 1, true, null);
    Set<ErrorDescription> allIgnores = new HashSet<ErrorDescription>();
    for (ErrorDescription desc : expectErrorDescriptions)
    	allIgnores.add(desc);
    Set<ErrorDescription> foundIgnores = new HashSet<ErrorDescription>();
    
		Map<ErrorLocation, Set<ErrorDescription>> all = errors.getAllErrors();
		for (ErrorLocation loc : all.keySet())
			for (ErrorDescription desc : all.get(loc))
			{
				boolean found = false;
				for (ErrorDescription ignore : expectErrorDescriptions)
					if (desc == ignore)
					{
						found = true;
						foundIgnores.add(desc);
					}
				
				if (!found)
					Assert.assertTrue("Found unexpected error: " + desc, false);
			}
		
		// if we didn't find all the errors, complain
		Assert.assertEquals("Didn't find right number of errors", allIgnores, foundIgnores);
	}
	
	protected void setImplementationStereo(BBInterface[] interfaces)
	{
		for (BBInterface iface : interfaces)
			iface.settable_getReplacedAppliedStereotypes().add(appliedInterfaceStereo);
	}
}
