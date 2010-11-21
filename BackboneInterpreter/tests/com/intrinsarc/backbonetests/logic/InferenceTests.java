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

import org.junit.*;

import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.deltaengine.base.*;

public class InferenceTests extends TestBase
{
	BBInterface aIface;
	BBInterface bIface;
	BBInterface cIface;
	BBInterface dIface;
	BBInterface eIface;

	BBComponent aprov[] = new BBComponent[1];
	BBPort aprovPort[] = new BBPort[1];
	
	BBComponent areq[] = new BBComponent[1];
	BBPort areqPort[] = new BBPort[1];
	
	BBComponent athru[] = new BBComponent[1];
	BBPort athruReqPort[] = new BBPort[1];
	BBPort athruProvPort[] = new BBPort[1];
	
	BBComponent aprovmanythru[] = new BBComponent[1];
	BBPort aprovmanythruReqPort[] = new BBPort[1];
	BBPort aprovmanythruProvPort[] = new BBPort[1];
	
	BBComponent areqmanythru[] = new BBComponent[1];
	BBPort areqmanythruReqPort[] = new BBPort[1];
	BBPort areqmanythruProvPort[] = new BBPort[1];
	
	BBComponent bthru[] = new BBComponent[1];
	BBPort bthruReqPort[] = new BBPort[1];
	BBPort bthruProvPort[] = new BBPort[1];
	
	BBComponent breq[] = new BBComponent[1];
	BBPort breqPort[] = new BBPort[1];
	
	BBComponent bprov[] = new BBComponent[1];
	BBPort bprovPort[] = new BBPort[1];
	
	BBComponent eprov[] = new BBComponent[1];
	BBPort eprovPort[] = new BBPort[1];
	
	BBComponent creq[] = new BBComponent[1];
	BBPort creqPort[] = new BBPort[1];
	
	BBComponent provbreqathru[] = new BBComponent[1];
	BBPort provbreqathruReqPort[] = new BBPort[1];
	BBPort provbreqathruProvPort[] = new BBPort[1];

	BBComponent provbreqa[] = new BBComponent[1];
	BBPort provbreqaPort[] = new BBPort[1];
	
	@Override
	protected void postSetup()
	{
		// make the interfaces
		aIface = new BBInterface("aIface");
		bIface = new BBInterface("bIface");
		bIface.settable_getRawResembles().addObject(aIface);
		cIface = new BBInterface("cIface");
		cIface.settable_getRawResembles().addObject(aIface);
		dIface = new BBInterface("dIface");
		dIface.settable_getRawResembles().addObject(bIface);
		dIface.settable_getRawResembles().addObject(cIface);
		eIface = new BBInterface("eIface");
		a.settable_getElements().add(aIface);
		a.settable_getElements().add(bIface);
		a.settable_getElements().add(cIface);
		a.settable_getElements().add(dIface);
		a.settable_getElements().add(eIface);
		
		setImplementationStereo(new BBInterface[]{aIface, bIface, cIface, dIface, eIface});
		
				
		makePrimitive("breq", breq, null, null, null, false, breqPort, bIface, null, false);
		makePrimitive("aprov", aprov, aprovPort, aIface, null, false, null, null, null, false);
		makePrimitive("aprovmanythru", aprovmanythru, aprovmanythruProvPort, aIface, null, true, aprovmanythruReqPort, aIface, null, false);
		makePrimitive("athru", athru, athruProvPort, aIface, null, false, athruReqPort, aIface, null, false);
		makePrimitive("areqmanythru", areqmanythru, areqmanythruProvPort, aIface, null, false, areqmanythruReqPort, aIface, null, true);
		makePrimitive("areq", areq, null, null, null, false, areqPort, aIface, null, false);
		makePrimitive("creq", creq, null, null, null, false, creqPort, cIface, null, false);
		makePrimitive("bprov", bprov, bprovPort, bIface, null, false, null, null, null, false);
		makePrimitive("bthru", bthru, bthruProvPort, bIface, null, false, bthruReqPort, bIface, null, false);
		makePrimitive("eprov", eprov, eprovPort, eIface, null, false, null, null, null, false);
		makePrimitive("provbreqathru", provbreqathru, provbreqathruProvPort, bIface, aIface, false, provbreqathruReqPort, bIface, aIface, false);
		makePrimitive("provbreqa", provbreqa, provbreqaPort, bIface, aIface, false, null, null, null, false);
		informAboutParents();
	}

	@Test
	public void inferenceCase1()
	{
		BBComponent ccase = new BBComponent("case1");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		// add the parts
		BBPart part1 = new BBPart("athru");
		part1.setType(athru[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("breq");
		part2.setType(breq[0]);
		ccase.settable_getAddedParts().add(part2);
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(athruReqPort[0]);
		conn1.setToPart(part1);
		conn1.setName("conn1");
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(athruProvPort[0]);
		conn2.setFromPart(part1);
		conn2.setToPort(breqPort[0]);
		conn2.setToPart(part2);
		conn2.setName("conn2");
		ccase.settable_getAddedConnectors().add(conn2);
		
		checkErrors();
		
		testElements("case1 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{bIface});
		testElements("case1 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{});
	}

	@Test
	public void inferenceCase2()
	{
		BBComponent ccase = new BBComponent("case2");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		
		// add the parts
		BBPart part1 = new BBPart("athru");
		part1.setType(athru[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("athru2");
		part2.setType(athru[0]);
		ccase.settable_getAddedParts().add(part2);
		BBPart part3 = new BBPart("breq");
		part3.setType(breq[0]);
		ccase.settable_getAddedParts().add(part3);
		
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(athruReqPort[0]);
		conn1.setToPart(part1);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(athruProvPort[0]);
		conn2.setFromPart(part1);
		conn2.setToPort(athruReqPort[0]);
		conn2.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn2);
		BBConnector conn3 = new BBConnector("conn3");
		conn3.setFromPort(athruProvPort[0]);
		conn3.setFromPart(part2);
		conn3.setToPort(breqPort[0]);
		conn3.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn3);
		checkErrors();
		
		testElements("case2 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{bIface});
		testElements("case2 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{});
	}

	@Test
	public void inferenceCase3()
	{
		BBComponent ccase = new BBComponent("case3");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		
		// add the parts
		BBPart part1 = new BBPart("breq");
		part1.setType(breq[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("athru");
		part2.setType(athru[0]);
		ccase.settable_getAddedParts().add(part2);
		BBPart part3 = new BBPart("bprov");
		part3.setType(bprov[0]);
		ccase.settable_getAddedParts().add(part3);
		
		// add the internal connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(breqPort[0]);
		conn1.setFromPart(part1);
		conn1.setToPort(athruProvPort[0]);
		conn1.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(athruReqPort[0]);
		conn2.setFromPart(part2);
		conn2.setToPort(bprovPort[0]);
		conn2.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn2);
		
		checkErrors();	
	}
	
	@Test
	public void inferenceCase4()
	{
		BBComponent ccase = new BBComponent("case4");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		
		// add the parts
		BBPart part1 = new BBPart("breq");
		part1.setType(breq[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("athru");
		part2.setType(athru[0]);
		ccase.settable_getAddedParts().add(part2);
		BBPart part3 = new BBPart("eprov");
		part3.setType(eprov[0]);
		ccase.settable_getAddedParts().add(part3);
		
		// add the internal connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(breqPort[0]);
		conn1.setFromPart(part1);
		conn1.setToPort(athruProvPort[0]);
		conn1.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(athruReqPort[0]);
		conn2.setFromPart(part2);
		conn2.setToPort(eprovPort[0]);
		conn2.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn2);
		
		expectErrors(new ErrorDescription[]{ErrorCatalog.NO_ONE_TO_ONE_INTERFACE_MAPPING_EXISTS});	
	}
	
	@Test
	public void inferenceCase5()
	{
		BBComponent ccase = new BBComponent("case5");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		// add the parts
		BBPart part1 = new BBPart("athru");
		part1.setType(athru[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("bprov");
		part2.setType(bprov[0]);
		ccase.settable_getAddedParts().add(part2);
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(athruProvPort[0]);
		conn1.setToPart(part1);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(athruReqPort[0]);
		conn2.setFromPart(part1);
		conn2.setToPort(bprovPort[0]);
		conn2.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn2);
		checkErrors();
		
		testElements("case5 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{});
		testElements("case5 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{bIface});
	}

	@Test
	public void inferenceCase6()
	{
		BBComponent ccase = new BBComponent("case6");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		BBPort port2 = new BBPort("port2", "port2");
		ccase.settable_getAddedPorts().add(port2);

		// add the parts
		BBPart part1 = new BBPart("athru");
		part1.setType(athru[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("bthru");
		part2.setType(bthru[0]);
		ccase.settable_getAddedParts().add(part2);
		
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(athruReqPort[0]);
		conn1.setToPart(part1);
		ccase.settable_getAddedConnectors().add(conn1);
		
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(athruProvPort[0]);
		conn2.setFromPart(part1);
		conn2.setToPort(bthruReqPort[0]);
		conn2.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn2);

		BBConnector conn3 = new BBConnector("conn3");
		conn3.setFromPort(bthruProvPort[0]);
		conn3.setFromPart(part2);
		conn3.setToPort(port2);
		ccase.settable_getAddedConnectors().add(conn3);

		checkErrors();
		
		testElements("case6 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{bIface});
		testElements("case6 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{});
		testElements("case6 required interfaces", ccase.getRequiredInterfaces(top, port2), new DEElement[]{});
		testElements("case6 provided interfaces", ccase.getProvidedInterfaces(top, port2), new DEElement[]{bIface});
	}
	
	@Test
	public void inferenceCase7()
	{
		BBComponent ccase = new BBComponent("case7");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		
		// add the parts
		BBPart part1 = new BBPart("breq");
		part1.setType(breq[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("athru");
		part2.setType(athru[0]);
		ccase.settable_getAddedParts().add(part2);
		BBPart part3 = new BBPart("aprov");
		part3.setType(aprov[0]);
		ccase.settable_getAddedParts().add(part3);
		
		// add the internal connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(breqPort[0]);
		conn1.setFromPart(part1);
		conn1.setToPort(athruProvPort[0]);
		conn1.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(athruReqPort[0]);
		conn2.setFromPart(part2);
		conn2.setToPort(aprovPort[0]);
		conn2.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn2);
		
		expectErrors(new ErrorDescription[]{ErrorCatalog.NO_ONE_TO_ONE_INTERFACE_MAPPING_EXISTS});	
	}

	@Test
	public void inferenceCase8()
	{
		BBComponent ccase = new BBComponent("case8");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		
		// make the port
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		
		// add the parts
		BBPart part1 = new BBPart("aprov");
		part1.setType(aprov[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("bprov");
		part2.setType(bprov[0]);
		ccase.settable_getAddedParts().add(part2);
		BBPart part3 = new BBPart("aprovmanythru");
		part3.setType(aprovmanythru[0]);
		ccase.settable_getAddedParts().add(part3);
		
		// add the internal connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(aprovPort[0]);
		conn1.setFromPart(part1);
		conn1.setToPort(aprovmanythruReqPort[0]);
		conn1.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(bprovPort[0]);
		conn2.setFromPart(part2);
		conn2.setToPort(aprovmanythruReqPort[0]);
		conn2.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn2);
		
		BBConnector conn3 = new BBConnector("conn3");
		conn3.setFromPort(aprovmanythruProvPort[0]);
		conn3.setFromPart(part3);
		conn3.setToPort(port);
		ccase.settable_getAddedConnectors().add(conn3);
		
		checkErrors();
		
		testElements("case8 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{});
		testElements("case8 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{aIface});
	}

	@Test
	public void inferenceCase9()
	{
		BBComponent ccase = new BBComponent("case9");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		
		// make the port
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		
		// add the parts
		BBPart part1 = new BBPart("areq");
		part1.setType(areq[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("breq");
		part2.setType(breq[0]);
		ccase.settable_getAddedParts().add(part2);
		BBPart part3 = new BBPart("areqmanythru");
		part3.setType(areqmanythru[0]);
		ccase.settable_getAddedParts().add(part3);
		
		// add the internal connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(areqPort[0]);
		conn1.setFromPart(part1);
		conn1.setToPort(areqmanythruProvPort[0]);
		conn1.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn1);
		
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(breqPort[0]);
		conn2.setFromPart(part2);
		conn2.setToPort(areqmanythruProvPort[0]);
		conn2.setToPart(part3);
		ccase.settable_getAddedConnectors().add(conn2);
		
		BBConnector conn3 = new BBConnector("conn3");
		conn3.setFromPort(areqmanythruReqPort[0]);
		conn3.setFromPart(part3);
		conn3.setToPort(port);
		ccase.settable_getAddedConnectors().add(conn3);
		
		checkErrors();
		
		testElements("case9 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{bIface});
		testElements("case9 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{});
	}
	
	@Test
	public void inferenceCase10()
	{
		BBComponent ccase = new BBComponent("case10");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		BBPort port2 = new BBPort("port2", "port2");
		ccase.settable_getAddedPorts().add(port2);

		// add the parts
		BBPart part1 = new BBPart("athru");
		part1.setType(athru[0]);
		ccase.settable_getAddedParts().add(part1);
		
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(athruReqPort[0]);
		conn1.setToPart(part1);
		ccase.settable_getAddedConnectors().add(conn1);
		
		BBConnector conn3 = new BBConnector("conn3");
		conn3.setFromPort(athruProvPort[0]);
		conn3.setFromPart(part1);
		conn3.setToPort(port2);
		ccase.settable_getAddedConnectors().add(conn3);

		checkErrors();
		
		testElements("case10 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{aIface});
		testElements("case10 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{});
		testElements("case10 required interfaces", ccase.getRequiredInterfaces(top, port2), new DEElement[]{});
		testElements("case10 provided interfaces", ccase.getProvidedInterfaces(top, port2), new DEElement[]{aIface});
	}
	
	@Test
	public void inferenceCase11()
	{
		BBComponent ccase = new BBComponent("case11");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		
		// make the port
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		
		// add the parts
		BBPart part1 = new BBPart("creq");
		part1.setType(creq[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("breq");
		part2.setType(breq[0]);
		ccase.settable_getAddedParts().add(part2);
		
		// add the internal connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(creqPort[0]);
		conn1.setFromPart(part1);
		conn1.setToPort(port);
		ccase.settable_getAddedConnectors().add(conn1);
		
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(breqPort[0]);
		conn2.setFromPart(part2);
		conn2.setToPort(port);
		ccase.settable_getAddedConnectors().add(conn2);
		
		checkErrors();
		
		testElements("case11 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{dIface});
		testElements("case11 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{});
	}
	
	@Test
	public void inferenceCase12()
	{
		BBComponent ccase = new BBComponent("case12");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);

		BBPart part = new BBPart("bprov");
		part.setType(bprov[0]);
		ccase.settable_getAddedParts().add(part);
		
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(bprovPort[0]);
		conn1.setToPart(part);
		ccase.settable_getAddedConnectors().add(conn1);
		checkErrors();
		
		testElements("case12 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{});
		testElements("case12 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{bIface});
	}
	
	@Test
	public void inferenceCase13()
	{
		BBComponent ccase = new BBComponent("case13");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		
		// make the port
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		
		// add the parts
		BBPart part = new BBPart("areq");
		part.setType(areq[0]);
		ccase.settable_getAddedParts().add(part);
		
		// add the internal connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(areqPort[0]);
		conn1.setFromPart(part);
		conn1.setToPort(port);
		ccase.settable_getAddedConnectors().add(conn1);		
		checkErrors();
		
		testElements("case13 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{aIface});
		testElements("case13 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{});
	}

	@Test
	public void inferenceCase14()
	{
		BBComponent ccase = new BBComponent("case14");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		
		// add the parts
		BBPart part1 = new BBPart("provbreqathru");
		part1.setType(provbreqathru[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("provbreqa");
		part2.setType(provbreqa[0]);
		ccase.settable_getAddedParts().add(part2);
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(provbreqathruProvPort[0]);
		conn1.setToPart(part1);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(provbreqathruReqPort[0]);
		conn2.setFromPart(part1);
		conn2.setToPort(provbreqaPort[0]);
		conn2.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn2);
		checkErrors();
		
		testElements("case14 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{aIface});
		testElements("case14 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{bIface});
	}

	@Test
	public void inferenceCase15()
	{
		BBComponent ccase = new BBComponent("case15");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort pport = new BBPort("pport", "pport");
		ccase.settable_getAddedPorts().add(pport);
		BBPort rport = new BBPort("rport", "rport");
		ccase.settable_getAddedPorts().add(rport);
		
		// add the parts
		BBPart part1 = new BBPart("areqmanythru");
		part1.setType(areqmanythru[0]);
		ccase.settable_getAddedParts().add(part1);
		BBPart part2 = new BBPart("bprov");
		part2.setType(bprov[0]);
		ccase.settable_getAddedParts().add(part2);
		
		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(pport);
		conn1.setToPort(areqmanythruProvPort[0]);
		conn1.setToPart(part1);
		ccase.settable_getAddedConnectors().add(conn1);
		
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setFromPort(areqmanythruReqPort[0]);
		conn2.setFromPart(part1);
		conn2.setToPort(bprovPort[0]);
		conn2.setToPart(part2);
		ccase.settable_getAddedConnectors().add(conn2);

		BBConnector conn3 = new BBConnector("conn3");
		conn3.setFromPort(areqmanythruReqPort[0]);
		conn3.setFromPart(part1);
		conn3.setToPort(rport);
		ccase.settable_getAddedConnectors().add(conn3);
		
		checkErrors();
		
		testElements("case15 required interfaces", ccase.getRequiredInterfaces(top, rport), new DEElement[]{aIface});
		testElements("case15 provided interfaces", ccase.getProvidedInterfaces(top, pport), new DEElement[]{aIface});
	}

	@Test
	public void inferenceCase16()
	{
		BBComponent ccase = new BBComponent("case16");
		a.settable_getElements().add(ccase);
		ccase.setParent(a);
		BBPort port = new BBPort("port", "port");
		ccase.settable_getAddedPorts().add(port);
		
		// add the parts
		BBPart part1 = new BBPart("provbreqathru");
		part1.setType(provbreqathru[0]);
		ccase.settable_getAddedParts().add(part1);

		// add the connectors
		BBConnector conn1 = new BBConnector("conn1");
		conn1.setFromPort(port);
		conn1.setToPort(provbreqathruProvPort[0]);
		conn1.setToPart(part1);
		ccase.settable_getAddedConnectors().add(conn1);
		BBConnector conn2 = new BBConnector("conn2");
		conn2.setToPort(provbreqathruReqPort[0]);
		conn2.setToPart(part1);
		conn2.setFromPort(provbreqathruReqPort[0]);
		conn2.setFromPart(part1);
		ccase.settable_getAddedConnectors().add(conn2);

		checkErrors();
		
		testElements("case16 required interfaces", ccase.getRequiredInterfaces(top, port), new DEElement[]{bIface});
		testElements("case16 provided interfaces", ccase.getProvidedInterfaces(top, port), new DEElement[]{bIface});	}
}
