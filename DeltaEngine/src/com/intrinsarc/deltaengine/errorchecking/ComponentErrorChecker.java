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
package com.intrinsarc.deltaengine.errorchecking;

import java.util.*;

import com.intrinsarc.deltaengine.base.*;

public class ComponentErrorChecker
{
  private ErrorRegister errors;
  private DEStratum perspective;
  private DEComponent component;

  public ComponentErrorChecker(
      DEStratum perspective,
      DEComponent component,
      ErrorRegister errors)
  {
    this.perspective = perspective;
    this.component = component;
    this.errors = errors;
  }

  public void performCheck(boolean diagramCheck)
  {
  	// is this element at home?
  	boolean atHome = perspective == component.getHomeStratum();
  	
  	// if this is replacing, and we are not at home, don't bother
  	if (component.isReplacement() && perspective != component.getHomeStratum() || component.isRetired(perspective))
  		return;
  		
    // components can only resemble or substitute the same type
  	ComponentKindEnum kind = component.getComponentKind();
    for (DEElement resembled : component.getRawResembles())
    {
      if (resembled.asComponent() == null || resembled.asComponent().getComponentKind() != kind)
        errors.addError(
            new ErrorLocation(perspective, component), ErrorCatalog.COMPONENT_RESEMBLES_COMPONENT_OF_SAME_TYPE);
    }
    
    for (DEElement substitutes : component.getRawReplaces())
    {
      if (substitutes.asComponent() == null || substitutes.asComponent().getComponentKind() != kind)
        errors.addError(
            new ErrorLocation(perspective, component), ErrorCatalog.COMPONENT_SUBSTITUTES_COMPONENT_OF_SAME_TYPE);
    }
   
    // check for resemblance redundancy
    // only currently applied for components -- applying to interfaces was problematic due
    // to things like serializable being inherited by all and sundry...   AMcV 15/10/2008 
    Set<DEElement> filteredResemblance = component.getResembles(perspective, true, false);
    for (DEElement r : component.getResembles(perspective, false, false))
      if (!filteredResemblance.contains(r) && !r.hasDirectCircularResemblance(perspective))
        errors.addError(
            new ErrorLocation(perspective, component), ErrorCatalog.REDUNDANT_RESEMBLANCE);  

    // check the hyper ports and reference to retired interfaces
    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
    {
    	DEPort port = pair.getConstituent().asPort();

    	// if the port is a hyper-start, make sure many things can connect to it
    	if (port.getPortKind() == PortKindEnum.HYPERPORT)
    	{
    		// if we have some required interfaces and no unbounded multiplicity, complain
    		if (!component.getRequiredInterfaces(perspective, port).isEmpty() && port.getUpperBound() != -1)
          errors.addError(
              new ErrorLocation(perspective, component, port), ErrorCatalog.HYPERPORT_MUST_ALLOW_MULTIPLE);      			
    	}
    	
    	// a hyper port cannot be ordered
    	if (port.getPortKind() == PortKindEnum.HYPERPORT && port.isOrdered())
    		errors.addError(new ErrorLocation(perspective, component, port), ErrorCatalog.HYPERPORTS_CANNOT_BE_ORDERED);
    	
    	// ports may not refer to retired interfaces
    	for (DEInterface i : component.getRequiredInterfaces(perspective, port))
    		if (i.isRetired(perspective))
    			errors.addError(
    					new ErrorLocation(perspective, component, port), ErrorCatalog.PORT_REFERS_TO_RETIRED_INTERFACE);
    	for (DEInterface i : component.getProvidedInterfaces(perspective, port))
    		if (i.isRetired(perspective))
    			errors.addError(
    					new ErrorLocation(perspective, component, port), ErrorCatalog.PORT_REFERS_TO_RETIRED_INTERFACE);
    	
    	// test the interface visibility -- only do this at home...
    	if (atHome)
    		testPortInterfaceVisibility(port);    	
    }
    
    // common section for all components
    
  	// check the ports
    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
    {
    	// follow any link
    	DEPort port = pair.getConstituent().asPort();
    	Set<? extends DEInterface> prov = component.getProvidedInterfaces(perspective, port);
    	Set<? extends DEInterface> req = component.getRequiredInterfaces(perspective, port);
      // there must be no redundancy on the interfaces
      if (overlappingInterfaces(prov) || overlappingInterfaces(req))
    	{
        errors.addError(
            new ErrorLocation(perspective, component, port), ErrorCatalog.OVERLAPPING_INTERFACES_DETECTED);      				      		
    	}
    }

    // some attribute checks
    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getAddObjects())
    	checkAttributes(pair);
    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getReplaceObjects())
    	checkAttributes(pair);
    // check that no port has a lower bound that isn't 0 or 1
    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PORT).getAddObjects())
    {
    	int lower = pair.getConstituent().asPort().getLowerBound();
    	int upper = pair.getConstituent().asPort().getUpperBound();
    	if (lower > upper  && upper > 0 || lower < 0 || upper < -1)
        errors.addError(
            new ErrorLocation(perspective, component), ErrorCatalog.PORT_BOUNDS_BAD);	    		
    }

    /////////////////////////////////////////////////////////////
    // leaf checks
    if (!component.canBeDecomposed(perspective))
    {
    	checkNameOverlaps();
    	
    	// must have at most one main port
    	List<DEPort> mains = component.getBeanMainPorts(perspective); 
    	if (mains.size() > 1)
        errors.addError(
            new ErrorLocation(perspective, component), ErrorCatalog.AT_MOST_ONE_BEAN_MAIN_PORT);
    	
    	// each main should have only provided ports
    	if (mains.size() == 1)
    	{
    		DEPort main = mains.get(0);
    		if (main.getSetProvidedInterfaces().isEmpty() ||
    				main.isMany())
    		{
          errors.addError(
              new ErrorLocation(perspective, component, main), ErrorCatalog.BEAN_MAIN_NOT_SUITABLE);
    		}
    	}
    	
    	// must have at most one no-name port
    	List<DEPort> nonames = component.getBeanNoNamePorts(perspective);
    	{
    		Set<DEInterface> existing = new HashSet<DEInterface>();
      	for (DEPort noname : nonames)
      	{
      		Set<? extends DEInterface> required = component.getRequiredInterfaces(perspective, noname);
      		Set<DEInterface> test = new HashSet<DEInterface>(existing);
      		test.retainAll(required);
      		if (!test.isEmpty())
      		{
            errors.addError(
                new ErrorLocation(perspective, component, noname), ErrorCatalog.NONAME_PORT_OVERLAP);      			
      		}
      		existing.addAll(required);
      	}
    	}
    	
    	// each noname port should have only requires, and be indexed
    	for (DEPort noname : nonames)
    	{
    		if (!noname.getSetProvidedInterfaces().isEmpty() ||
    				noname.getSetRequiredInterfaces().isEmpty() ||
    				!noname.isMany())
    		{
          errors.addError(
              new ErrorLocation(perspective, component, noname), ErrorCatalog.BEAN_NO_NAME_NOT_SUITABLE);
    		}
    	}
    	
    	// if we have multiple implementation inheritance, then complain
    	Set<String> inherited = component.getImplementationInheritances(perspective);
    	if (inherited.size() > 1)
        errors.addError(
            new ErrorLocation(perspective, component), ErrorCatalog.TURN_OFF_INHERITANCE);
    	
      // check that the port links imply mirror interface arrangements
      for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
      {
      	// follow any link
      	DEPort port = pair.getConstituent().asPort();
      	DELinks links = component.getInferredLinks(perspective, port);
      	for (DELinkEnd start : links.getUnidirectionalKeys())
      	{
    			Set<? extends DEInterface> provStart = component.getProvidedInterfaces(perspective, start.getPort());
    			Set<? extends DEInterface> reqStart = component.getRequiredInterfaces(perspective, start.getPort());
      		for (DELinkEnd end : links.getEnds(start))
      		{
      			Set<? extends DEInterface> provEnd = component.getProvidedInterfaces(perspective, end.getPort());
      			Set<? extends DEInterface> reqEnd = component.getRequiredInterfaces(perspective, end.getPort());
      			
      			if (!provStart.equals(reqEnd) || !reqStart.equals(provEnd))
              errors.addError(
                  new ErrorLocation(component, start.getPort()), ErrorCatalog.LINKED_LEAF_PORTS_MUST_MIRROR_INTERFACES);      				
      		}
      	}
      }
      
      // ensure we have no connectors
      for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getConstituents(perspective))
      {
      	errors.addError(
      			new ErrorLocation(component, pair.getConstituent()), ErrorCatalog.LEAF_CANNOT_HAVE_CONNECTORS);
      }
      
      // ensure we have no parts
      for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective))
      {
      	errors.addError(
      			new ErrorLocation(component, pair.getConstituent()), ErrorCatalog.CANNOT_HAVE_PARTS);
      }
      
      // any ports must be named
      for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
      {
      	DEPort port = pair.getConstituent().asPort();
      	String name = port.getName();
      	if (name == null || name.trim().length() == 0)
      		errors.addError(
      				new ErrorLocation(component, port), ErrorCatalog.LEAF_PORTS_MUST_BE_NAMED);
      }
      
      // ensure we have only 1 implementation
      boolean impl = component.hasForcedImplementationClass(perspective);
      if (component.isPlaceholder(perspective))
      {
	      if (impl)
	      	errors.addError(new ErrorLocation(perspective, component), ErrorCatalog.NO_IMPLEMENTATION_ALLOWED);
      }
      else
      if (!component.isAbstract(perspective) && component.isLeaf(perspective) && component.getComponentKind() != ComponentKindEnum.STEREOTYPE)
      {
        String fullImpl = component.getImplementationClass(perspective);
	      if (fullImpl == null)
	      	errors.addError(new ErrorLocation(perspective, component), ErrorCatalog.NO_IMPLEMENTATION);

	      // the implementation name must be valid
	      if (!ElementErrorChecker.isValidClassName(fullImpl))
	      	errors.addError(new ErrorLocation(perspective, component), ErrorCatalog.IMPLEMENTATION_INVALID);
	      ElementErrorChecker.checkStratumPackageExists(errors, component);
      }
      
      // a factory currently cannot be a leaf
      if (component.isFactory(perspective))
      	errors.addError(new ErrorLocation(perspective, component), ErrorCatalog.NO_LEAF_FACTORIES);
    }
    else
    {
    	// a composite cannot have a forced implementation
      boolean impl = component.hasForcedImplementationClass(perspective);
      if (impl)
      	errors.addError(new ErrorLocation(perspective, component), ErrorCatalog.NO_IMPLEMENTATION_ALLOWED);
    	
      // an composite must be nested inside a stratum or package
      if (component.getParent().asStratum() == null)
        errors.addError(
            new ErrorLocation(component.getParent().getParent(), component.getParent()), ErrorCatalog.ELEMENT_NOT_NESTED);
    	
      for (DEConnector conn: component.determineImproperlyMatchedPortInstances(perspective))
      	errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.NO_ONE_TO_ONE_INTERFACE_MAPPING_EXISTS);
      
      // ensure we have no port links
      if (!component.getPortLinks(perspective).isEmpty())
      {
      	errors.addError(
      			new ErrorLocation(component), ErrorCatalog.COMPOSITE_CANNOT_HAVE_PORT_LINKS);
      }
      
      Map<PortPartPerspective, Set<String>> unique = new HashMap<PortPartPerspective, Set<String>>();
      for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR).getConstituents(perspective))
      {
      	// a connector cannot link from 1 port to another
      	DEConnector conn = pair.getConstituent().asConnector();
      	DEPart part1 = conn.getPart(perspective, component, 0);
      	DEPart part2 = conn.getPart(perspective, component, 1);
    		DEPort port1 = conn.getPort(perspective, component, 0);
    		DEPort port2 = conn.getPort(perspective, component, 1);
    		
    		// ensure all the connector ends are ok
    		for (int lp = 0; lp < 2; lp++)
    			if (!conn.isIndexOk(lp))
        		errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.BAD_INDEX);
    		
    		for (int lp = 0; lp < 2; lp++)
    		{
    			DEConnectorEnd end = conn.makeConnectorEnd(perspective, component, lp);
    			if (end.getIndex() != null && end.getPart() != null)
    			{
    				if (end.getPart().getType().isLegacyBean(perspective))
    					errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.LEGACY_BEAN_BAD_INDEX);    				
    			}
    		}
      	
      	if (part1 == null && part2 == null)
      		errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.NO_DIRECT_PORT_CONNECTIONS_ALLOWED);

      	// cannot loopback on the same port instance
      	// disable for now, i'm not sure it's ever a problem
      	// i.e. inference use case 16 seems fine with it in... AMcV 24/6/2010
//      	if (part1 == part2 && port1 == port2)
//      		errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.NO_CONNECTOR_LOOPBACKS);
      	
      	// if a component links to an ordered port, then it must have an index set, unless it is a delegate
      	if (!conn.isDelegate())
      	{
	      	for (int lp = 0; lp < 2; lp++)
	      	{
	      		DEPort port = conn.getPort(perspective, component, lp);
	      		String index = conn.getIndex(lp);
	      		DEPart otherPart = conn.getPart(perspective, component, 1-lp);
	      		if (index == null && port != null && port.isOrdered() && (otherPart == null || !otherPart.getType().isFactory(perspective)))
	      			errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.CONNECTOR_TO_ORDERED_PORT_MUST_BE_INDEXED);
	      		
	      		// if this has an index, place it in the unique map -- if already there, this is an error
	      		if (index != null)
	      		{
	        		DEPart part = conn.getPart(perspective, component, lp);
	        		PortPartPerspective ppp = new PortPartPerspective(perspective, port, part);
	        		Set<String> uniq = unique.get(ppp);
	        		if (uniq == null)
	        		{
	        			uniq = new HashSet<String>();
	        			unique.put(ppp, uniq);
	        		}
	        		
	        		// complain if the alias is already there
	        		if (!uniq.add(index))
	        			errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.CONNECTOR_INDEX_IS_NOT_UNIQUE);        			
	      		}
	      	}
      	}
      	else
      	{
      		// if this is a delegate, it cannot have any indices
      		if (conn.getIndex(0) != null || conn.getIndex(1) != null)
      			errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.DELEGATE_CONNECTORS_CANNOT_HAVE_INDICES);
      		
      		// port instance must have more optionality than port
      		if (part1 == null)
      		{
      			if  (port1.getLowerBound() > port2.getLowerBound())
      				errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.PORT_HAS_TOO_MUCH_OPTIONALITY_FOR_DELEGATION);
      			if (port1.getUpperBound() > port2.getUpperBound())
        			errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.PORT_UPPER_TOO_HIGH_FOR_DELEGATION);
      		}
      		if (part2 == null)
      		{
      			if (port2.getLowerBound() > port1.getLowerBound())
      				errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.PORT_HAS_TOO_MUCH_OPTIONALITY_FOR_DELEGATION);
      			if (port2.getUpperBound() > port1.getUpperBound())
        			errors.addError(new ErrorLocation(perspective, component, conn), ErrorCatalog.PORT_UPPER_TOO_HIGH_FOR_DELEGATION);
      		}
      	}      	
      }
      
      // a composite cannot directly or indirectly include itself or anything that would prevent
      // the composition hierarchy from terminating in leaf instances
    	noSelfComposition();
    }
    
    // make sure that the attributes can be topologically sorted using their default values
    if (!component.defaultValuesCanBeTopologicallySorted(perspective))
    	errors.addError(
    			new ErrorLocation(perspective, component), ErrorCatalog.NO_ATTRIBUTE_SORT_ORDER);
    
    IDeltas conns = component.getDeltas(ConstituentTypeEnum.DELTA_CONNECTOR);
    Set<DeltaPair> pairs = conns.getConstituents(perspective);
    
    // checks that used to be only for the home stratum, but have been extended
    for (DeltaPair pair : pairs)
    {
      DEConnector conn = pair.getConstituent().asConnector();
      for (int lp = 0; lp < 2; lp++)
        if (conn.getPort(perspective, component, lp) == null)
          errors.addError(
              new ErrorLocation(perspective, component), ErrorCatalog.CONNECTOR_END_NOT_THERE);
    }
  
    // test that the part has a type and some other checks,
    // but only if this hasn't been retired
    if (!component.isRetired(perspective))
    {
	    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective))
	    {
	    	ensureRequiredConnected(pair);
	    	testPartTypeExists(pair);
	    	// only test visibility at home
	    	if (atHome)
	    		testPartTypeVisibility(pair);
	    	testPartSlots(pair);
	    	testPartRetirement(pair);
	    	for (DESlot slot : pair.getConstituent().asPart().getSlots())
	    		checkSlot(pair.getConstituent().asPart(), slot);
	    }
    }
  }

	private void checkNameOverlaps()
	{
		// cannot have any name overlaps between ports and attributes (including plural/singular in some cases)
		Set<String> taken = new HashSet<String>();
    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
    {
    	DEPort port = pair.getConstituent().asPort();
    	String name = port.getName();
    	
    	checkName(taken, name, port);
    	if (port.isMany())
    	{
    		String singleName = makeSingular(name);
    		if (!name.equals(singleName))
    			checkName(taken, singleName, port);
    	}
    }
    
    for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getConstituents(perspective))
    {
    	DEAttribute attr = pair.getConstituent().asAttribute();
    	checkName(taken, attr.getName(), attr);
    }
	}

  private void checkName(Set<String> taken, String name, DEObject object)
	{
  	if (taken.contains(name))
  	{
      errors.addError(
          new ErrorLocation(perspective, component, object), ErrorCatalog.NAME_CONFLICTS);
  	}
  	else
  		taken.add(name);
	}

	private String makeSingular(String name)
	{
  	if (name.endsWith("s") && name.length() > 1)
  		return name.substring(0, name.length() - 1);
  	return name;
	}
	
	private void ensureRequiredConnected(DeltaPair partPair)
	{
		// if any port is not optional and has required, then it must have at least 1 connector
		DEPart part = partPair.getConstituent().asPart();
		DEComponent partType = part.getType();
		if (partType != null)
		{
			DELinks links = component.getCompositeConnectorsAsLinks(perspective);
			for (DeltaPair pair : partType.getDeltas(ConstituentTypeEnum.DELTA_PORT).getConstituents(perspective))
			{
				DEPort port = pair.getConstituent().asPort();
				if (!partType.getRequiredInterfaces(perspective, port).isEmpty() && port.getLowerBound() > 0)
				{
					DELinkEnd end = new DELinkEnd(port, part);
					if (links.getEnds(end).isEmpty())
					{
						errors.addError(
				  			new ErrorLocation(perspective, component, part), ErrorCatalog.REQUIRED_PORT_NOT_CONNECTED);
					}
				}
			}
		}
	}

	private void checkSlot(DEPart part, DESlot slot)
	{
		if (part.getType() == null)
			return;
		
		// a slot must have a value of some sort
		if (slot.getValue() == null)
		{
			errors.addError(
          new ErrorLocation(perspective, component, part), ErrorCatalog.SLOT_MUST_HAVE_VALUE);
		}
		
		Set<DEAttribute> readOnly = new HashSet<DEAttribute>();
		Set<DEAttribute> writeOnly = new HashSet<DEAttribute>();
		
		DEComponent type = part.getType();
		for (DeltaPair pair : type.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getConstituents(perspective))
		{
	  	if (pair.getConstituent().asAttribute().isReadOnly())
	  		readOnly.add(pair.getOriginal().asAttribute());
	  	if (pair.getConstituent().asAttribute().isWriteOnly())
	  		writeOnly.add(pair.getOriginal().asAttribute());			
		}
		
		// a slot cannot refer in any way to a write-only attribute
		if (slot.getValue() != null)
			for (DEParameter p : slot.getValue())
			{
				DEAttribute pAttr = p.getAttribute(perspective, component);
				if (pAttr != null && writeOnly.contains(pAttr))
					errors.addError(
		          new ErrorLocation(perspective, component, part), ErrorCatalog.CANNOT_REFER_TO_WRITEONLY_ATTRIBUTE);					
			}
		if (readOnly.contains(slot.getAttribute(perspective, component)))
			errors.addError(
          new ErrorLocation(perspective, component, part), ErrorCatalog.CANNOT_SET_READONLY_ATTRIBUTE);
		
		// if this is a single assignment, ensure it is the same type
		if (slot.getValue().size() == 1 && slot.getValue().get(0).getAttribute() != null)
		{
			DEAttribute alias = slot.getValue().get(0).getAttribute();;
			if (alias != null)
			{
				if (slot.getAttribute().getType() != alias.getType())
					errors.addError(
							new ErrorLocation(perspective, component, part), ErrorCatalog.SLOT_ALIAS_INCOMPATIBLE_TYPE);						
			}
		}
	}

	private void checkAttributes(DeltaPair pair)
	{
  	DEAttribute attr = pair.getConstituent().asAttribute();
		
    // ensure the visibility is ok
  	testAttributeTypeVisibility(pair);

  	// make sure we don't have attributes that are readonly and writeonly	    	
  	testSensibleAttribute(attr);

  	// make sure that we don't have any default values for read-only attributes  	
  	testNoDefaultForReadOnlyAttributes(attr);
  	
  	// ensure that if we have a > for a default, that we also have an actual value set
  	testSensibleDefault(attr);
	}

	private void testSensibleDefault(DEAttribute attr)
	{
		// if this has a default, but it isn't specified (can happen with >) then complain
		if (attr.getDefaultValue().size() == 1)
		{
			DEParameter param = attr.getDefaultValue().get(0);
			if (param.getLiteral() != null && param.getLiteral().startsWith(">"))
			errors.addError(
          new ErrorLocation(perspective, component, attr), ErrorCatalog.ATTRIBUTE_DEFAULT_BAD);
		}
	}

	private void testSensibleAttribute(DEAttribute attr)
	{
		if (attr.isReadOnly() && attr.isWriteOnly())
			errors.addError(
					new ErrorLocation(component, attr), ErrorCatalog.ATTRIBUTE_CANNOT_BE_READ_ONLY_AND_WRITE_ONLY);		
	}

	private void testNoDefaultForReadOnlyAttributes(DEAttribute attr)
	{
		if (attr.isReadOnly() && !attr.getDefaultValue().isEmpty())
			errors.addError(
					new ErrorLocation(component, attr), ErrorCatalog.NO_DEFAULT_VALUE_FOR_READ_ONLY_ATTRIBUTE);					
	}

	private void testPartRetirement(DeltaPair pair)
	{
		DEComponent partType = pair.getConstituent().asPart().getType();
		if (partType != null)
		{
			if (partType.isRetired(perspective))
		  	errors.addError(
		  			new ErrorLocation(perspective, component, pair.getConstituent()), ErrorCatalog.PART_REFERS_TO_RETIRED_TYPE);

			// check for parts with abstract types also
			if (partType.isAbstract(perspective))
				errors.addError(
		  			new ErrorLocation(perspective, component, pair.getConstituent()), ErrorCatalog.CANNOT_INSTANTIATE_ABSTRACT_COMPONENT);
		}
	}
	
	private void testPortInterfaceVisibility(DEPort port)
	{
		Set<? extends DEInterface> provided = component.getProvidedInterfaces(perspective, port);
		Set<? extends DEInterface> required = component.getRequiredInterfaces(perspective, port);

		for (DEInterface prov : provided)
			if (!perspective.getCanSeePlusMe().contains(prov.getHomeStratum()))
		  	errors.addError(
		  			new ErrorLocation(component, port), ErrorCatalog.CANNOT_SEE_INTERFACE);
		for (DEInterface req : required)
			if (!perspective.getCanSeePlusMe().contains(req.getHomeStratum()))
		  	errors.addError(
		  			new ErrorLocation(component, port), ErrorCatalog.CANNOT_SEE_INTERFACE);
		
		if (provided.size() + required.size() == 0)
	  	errors.addError(
	  			new ErrorLocation(component, port), ErrorCatalog.PORT_MUST_PROVIDE_OR_REQUIRE);
	}
	
	private void testPartTypeVisibility(DeltaPair pair)
	{
		DEComponent partType = pair.getConstituent().asPart().getType();
		if (partType != null)
		{
			if (!perspective.getCanSeePlusMe().contains(partType.getHomeStratum()))
		  	errors.addError(
		  			new ErrorLocation(component, pair.getConstituent()), ErrorCatalog.CANNOT_SEE_TYPE);
		}
	}
	
	private void testPartTypeExists(DeltaPair pair)
	{
		DEComponent partType = pair.getConstituent().asPart().getType();
		if (partType == null)
			errors.addError(
	  			new ErrorLocation(perspective, component, pair.getConstituent()), ErrorCatalog.NO_PART_TYPE);
	}
	

	private void testPartSlots(DeltaPair pair)
	{
		DEPart part = pair.getConstituent().asPart();
		DEComponent partType = part.getType();
		
		// part may have no type
		if (partType == null)
			return;

		// if we have a faulty slot, then complain
		for (DESlot slot : part.getSlots())
		{
			if (slot.getValue() != null && slot.getValue().size() == 1)
			{
				DEParameter param = slot.getValue().get(0);
				if (param.getLiteral() != null && param.getLiteral().startsWith(">"))
				errors.addError(
	          new ErrorLocation(perspective, component, part), ErrorCatalog.SLOT_DEFAULT_BAD);
			}
		}

		// don't bother to see if all non-default attributes have slots if this is a bean -- all attributes are optional
		if (partType.isLegacyBean(perspective))
			return;
		
		for (DeltaPair attrPair : partType.getDeltas(ConstituentTypeEnum.DELTA_ATTRIBUTE).getConstituents(perspective))
		{
			DEAttribute attr = attrPair.getConstituent().asAttribute();
			if (attr.getDefaultValue().isEmpty() && noSlotFor(part, attrPair.getOriginal()))
			  	errors.addError(
			  			new ErrorLocation(component, pair.getConstituent()), ErrorCatalog.MUST_HAVE_SLOT_FOR_ATTRIBUTES_WITHOUT_DEFAULT);						
		}
	}


	private boolean noSlotFor(DEPart part, DEConstituent original)
	{
		for (DESlot slot : part.getSlots())
			if (slot.getAttribute() == original)
				return false;
		return true;
	}

	private void testAttributeTypeVisibility(DeltaPair pair)
	{
		DEElement attrType = pair.getConstituent().asAttribute().getType();
		if (attrType != null)
		{
			if (!perspective.getCanSeePlusMe().contains(attrType.getHomeStratum()))
		  	errors.addError(
		  			new ErrorLocation(component, pair.getConstituent()), ErrorCatalog.CANNOT_SEE_TYPE);
			// ensure that if this is a component, then it is a primitive type or an interface
			if (attrType.asInterface() != null)
				;
			else
			if (attrType.asComponent() != null)
		  	if (attrType.asComponent().getComponentKind() != ComponentKindEnum.PRIMITIVE)
		  		errors.addError(
		  				new ErrorLocation(component, pair.getConstituent()), ErrorCatalog.ATTRIBUTE_COMPONENT_TYPES_MUST_BE_PRIMITIVE);
		}
		else
			errors.addError(
					new ErrorLocation(component, pair.getConstituent()), ErrorCatalog.ATTRIBUTE_MUST_HAVE_TYPE);
	}

	private boolean overlappingInterfaces(Set<? extends DEInterface> ifaces)
	{
		for (DEElement iface : ifaces)
		{
			Set<DEElement> tree = iface.getInheritanceTree(perspective);
			for (DEElement iface2 : ifaces)
			{
				if (iface != iface2)
				{
					Set<DEElement> overlap = new HashSet<DEElement>(tree);
					overlap.retainAll(iface2.getInheritanceTree(perspective));
					if (overlap.size() > 0)
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * self-composition check.
	 * this is a test to see if we are composed of ourselves at any level.  basically,
	 * we just keep expanding out, keeping track of what we've found, and ensure we don't come across
	 * a type as a part type in the hierarchy.  AMcV 4/8/2010
	 */
	private void noSelfComposition()
	{
		Set<DEComponent> above = new HashSet<DEComponent>();
		above.add(component);
		for (DeltaPair pair : component.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective))
		{
			DEPart part = pair.getConstituent().asPart();
			if (part.getType() != null)
			{
				if (isSelfComposed(perspective, above, part.getType()))
				{
		  		errors.addError(
		  				new ErrorLocation(perspective, component, part), ErrorCatalog.SELF_COMPOSITION);						
				}
			}
		}
	}

	/**
	 * returns true if self composition
	 * @param types
	 * @param current
	 * @return
	 */
	public static boolean isSelfComposed(DEStratum perspective, Set<DEComponent> above, DEComponent current)
	{
		if (above.contains(current))
			return true;
		
		// create a new "above"
		Set<DEComponent> newAbove = new HashSet<DEComponent>(above);
		newAbove.add(current);
		for (DeltaPair pair : current.getDeltas(ConstituentTypeEnum.DELTA_PART).getConstituents(perspective))
		{
			DEPart part = pair.getConstituent().asPart();
			if (part.getType() != null)
				if (isSelfComposed(perspective, newAbove, part.getType()))
					return true;
		}
		return false;
	}
}
