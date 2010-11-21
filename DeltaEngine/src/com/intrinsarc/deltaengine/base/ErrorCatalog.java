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
package com.intrinsarc.deltaengine.base;

public class ErrorCatalog
{
  public static final ErrorDescription STRATUM_NAME = new ErrorDescription("A stratum must have a name");
  public static final ErrorDescription STRATUM_CIRCULAR = new ErrorDescription("Circular strata dependency");

  public static final ErrorDescription PACKAGE_CIRCULAR = new ErrorDescription("Circular package dependency");
  public static final ErrorDescription PACKAGE_PARENT = new ErrorDescription("A package can only be parented by another package type (including stratum)");
  public static final ErrorDescription PACKAGE_NESTING_VISIBILITY = new ErrorDescription("A package may not only depend on other packages within its nesting");
  public static final ErrorDescription PACKAGE_DEPENDS_INSIDE_STRATUM = new ErrorDescription("A package may depend only on other packages or strata in its home package");
  
  public static final ErrorDescription ELEMENT_NAME = new ErrorDescription("An element must have a name");
  public static final ErrorDescription ELEMENT_NOT_NESTED = new ErrorDescription("No nested elements allowed (except leaves in composites)");
  public static final ErrorDescription ELEMENT_NOT_AT_TOPLEVEL = new ErrorDescription("Element is defined at the top level");

  public static final ErrorDescription INTERFACE_RESEMBLES_INTERFACE = new ErrorDescription("An interface can only resemble another interface");
  public static final ErrorDescription INTERFACE_SUBSTITUTES_INTERFACE = new ErrorDescription("An interface can only substitute for another interface");
  public static final ErrorDescription COMPONENT_RESEMBLES_COMPONENT_OF_SAME_TYPE = new ErrorDescription("A component can only resemble another component of the same type");
  public static final ErrorDescription COMPONENT_SUBSTITUTES_COMPONENT_OF_SAME_TYPE = new ErrorDescription("A component can only substitute another component of the same type");
  
  public static final ErrorDescription LEAF_IMMUTABLE = new ErrorDescription("A leaf cannot participate in resemblance or substitution");
	public static final ErrorDescription LEAF_PORTS_MUST_BE_NAMED = new ErrorDescription("Leaf ports must have names");
	public static final ErrorDescription PORT_BOUNDS_BAD = new ErrorDescription("The port bounds are incorrect");
  public static final ErrorDescription RESEMBLES_VISIBLE = new ErrorDescription("An element that is resembled is not visible");
  public static final ErrorDescription MAX_ONE_SUBSTITUTION = new ErrorDescription("An element can substitute for at most one other element");
  public static final ErrorDescription SUBSTITUTION_VISIBLE = new ErrorDescription("An element that is substituted is not visible");
  public static final ErrorDescription SUBSTITUTION_IN_ANOTHER_STRATUM = new ErrorDescription("Can only substitute an element in another stratum");
  public static final ErrorDescription CANNOT_REFER_TO_SUBSTITUTION = new ErrorDescription("Cannot refer directly to a substitution");
  public static final ErrorDescription RESEMBLANCE_DIRECT_CIRCULARITY = new ErrorDescription("Resemblance is directly circular from this perspective");
  public static final ErrorDescription RESEMBLANCE_INDIRECT_CIRCULARITY = new ErrorDescription("Indirect resemblance circularity from this perspective");
  public static final ErrorDescription REDUNDANT_RESEMBLANCE = new ErrorDescription("Direct resemblance graph has redundancy");
  public static final ErrorDescription NO_DELTA_DELETE_REPLACE_OVERLAP = new ErrorDescription("The elements to be replaced and deleted overlap");
  public static final ErrorDescription DELETE_MATCHES_NO_CONSTITUENT = new ErrorDescription("A delete specifies a constituent that does not exist");
  public static final ErrorDescription REPLACE_MATCHES_NO_CONSTITUENT = new ErrorDescription("A replace specifies a constituent that does not exist");
  public static final ErrorDescription REPLACE_CONFLICT = new ErrorDescription("This replacement conflicts with another");
  public static final ErrorDescription STEREOTYPE_REPLACE_CONFLICT = new ErrorDescription("Two or more stereotypes conflict");
  public static final ErrorDescription CONNECTOR_END_NOT_THERE = new ErrorDescription("A connector has an end missing");
	public static final ErrorDescription LINKED_LEAF_PORTS_MUST_MIRROR_INTERFACES = new ErrorDescription("Linked leaf interface ports do not mirror interfaces");
	public static final ErrorDescription NO_ONE_TO_ONE_INTERFACE_MAPPING_EXISTS = new ErrorDescription("No one-to-one interface mapping exists with linked ports");
	public static final ErrorDescription OVERLAPPING_INTERFACES_DETECTED = new ErrorDescription("No interfaces can be chosen to fully satisfy the port");
	public static final ErrorDescription LEAF_CANNOT_HAVE_CONNECTORS = new ErrorDescription("No connectors allowed in leaves");
	public static final ErrorDescription CANNOT_HAVE_PARTS = new ErrorDescription("No parts allowed in this type of component");
	public static final ErrorDescription COMPOSITE_CANNOT_HAVE_PORT_LINKS = new ErrorDescription("No port links allowed in composites");
	public static final ErrorDescription NO_DIRECT_PORT_CONNECTIONS_ALLOWED = new ErrorDescription("No port to port connections allowed");
	public static final ErrorDescription STEREOTYPE_NOT_VISIBLE = new ErrorDescription("Stereotype not visible. Have you made the home stratum depend on backbone?");
	public static final ErrorDescription REQUIRED_PORT_NOT_CONNECTED = new ErrorDescription("A mandatory required port of a part is not connected");

	// currently disabled -- seems like an unnecessarily harsh restriction AMcV 24/6/2010
//	public static final ErrorDescription NO_CONNECTOR_LOOPBACKS = new ErrorDescription("Connector loopbacks are not allowed");
	public static final ErrorDescription PARENT_OF_DESTRUCTIVE_STRATUM_MUST_BE_DESTRUCTIVE_ALSO = new ErrorDescription("A destructive stratum must have a destructive parent stratum");
	public static final ErrorDescription HOME_OF_DESTRUCTIVE_ELEMENT_MUST_BE_DESTRUCTIVE_ALSO = new ErrorDescription("A destructive element must have a destructive home stratum");
	public static final ErrorDescription CANNOT_SEE_TYPE = new ErrorDescription("Type is not visible");
	public static final ErrorDescription CANNOT_SEE_INTERFACE = new ErrorDescription("An interface is not visible");
	public static final ErrorDescription NO_LEAF_FACTORIES = new ErrorDescription("Leaves cannot be factories");

	public static final ErrorDescription NO_IMPLEMENTATION = new ErrorDescription("A single implementation class must be specified");
	public static final ErrorDescription NO_IMPLEMENTATION_ALLOWED = new ErrorDescription("No implementation can be specified for this component");
	public static final ErrorDescription NO_FULL_CLASS_ALLOWED = new ErrorDescription("No composite full class can be specified for a leaf");
	public static final ErrorDescription ATTRIBUTE_MUST_HAVE_TYPE = new ErrorDescription("An attribute must have a type");
	public static final ErrorDescription PORT_MUST_PROVIDE_OR_REQUIRE = new ErrorDescription("A port must provide or require at least one interface");
	public static final ErrorDescription SLOT_ALIAS_INCOMPATIBLE_TYPE = new ErrorDescription("A slot assignment to an attribute has the wrong type");
	public static final ErrorDescription PORT_HAS_TOO_MUCH_OPTIONALITY_FOR_DELEGATION = new ErrorDescription("Port has too much optionality for delegation");
	public static final ErrorDescription PORT_UPPER_TOO_HIGH_FOR_DELEGATION = new ErrorDescription("Port upper bounds is too high for port instance");
	public static final ErrorDescription SELF_COMPOSITION = new ErrorDescription("Inclusion of part causes self-composition");
	public static final ErrorDescription BAD_INDEX = new ErrorDescription("Index must be of form [index]");
	public static final ErrorDescription CANNOT_INSTANTIATE_ABSTRACT_COMPONENT = new ErrorDescription("Cannot instantiate an abstract component");
	public static final ErrorDescription CONNECTOR_TO_ORDERED_PORT_MUST_BE_INDEXED = new ErrorDescription("A connector to an ordered port must be indexed");
	public static final ErrorDescription HYPERPORTS_CANNOT_BE_ORDERED = new ErrorDescription("Hyperports cannot be ordered");
	public static final ErrorDescription CONNECTOR_INDEX_IS_NOT_UNIQUE = new ErrorDescription("Connector indices must be unique for a given port");
	public static final ErrorDescription DELEGATE_CONNECTORS_CANNOT_HAVE_INDICES = new ErrorDescription("Delegation connectors cannot have indices");
	public static final ErrorDescription ATTRIBUTE_DEFAULT_BAD = new ErrorDescription("Defaults specified by > must be set via the actual value field");
	public static final ErrorDescription SLOT_DEFAULT_BAD = new ErrorDescription("Slot values specified by > must be set via the actual value field");
	public static final ErrorDescription SLOT_MUST_HAVE_VALUE = new ErrorDescription("All slots must have values");
	public static final ErrorDescription AT_MOST_ONE_BEAN_MAIN_PORT = new ErrorDescription("Choose a single bean-main from a non-indexed (providing) port");
	public static final ErrorDescription NONAME_PORT_OVERLAP = new ErrorDescription("A bean no-name port have the same interfaces as another");
	public static final ErrorDescription BEAN_MAIN_NOT_SUITABLE = new ErrorDescription("Port is not suitable for a bean-main port");
	public static final ErrorDescription BEAN_NO_NAME_NOT_SUITABLE = new ErrorDescription("Port is not suitable for a bean-no-name port");
	public static final ErrorDescription TURN_OFF_INHERITANCE = new ErrorDescription("Turn off implementation inheritance using no-inheritance");
	public static final ErrorDescription NAME_CONFLICTS = new ErrorDescription("Name (including singular variants) conflicts with another port or attribute name");
	public static final ErrorDescription LEGACY_BEAN_BAD_INDEX = new ErrorDescription("Legacy beans do not support integer indices");
	public static final ErrorDescription IMPLEMENTATION_INVALID = new ErrorDescription("Implementation class must be a valid Java identifier");
	public static final ErrorDescription NO_STRATUM_PACKAGE = new ErrorDescription("Set the Java package on a stratum with leaves or interfaces");

	// the diagram errors
  public static final ErrorDescription DIAGRAM_ELEMENT_NOT_VISIBLE = new ErrorDescription("This element is not visible from this diagram", true);
	public static final ErrorDescription ATTRIBUTE_COMPONENT_TYPES_MUST_BE_PRIMITIVE = new ErrorDescription("An attribute type must be either an interface or primitive component");
	public static final ErrorDescription NO_ATTRIBUTE_SORT_ORDER = new ErrorDescription("Attributes cannot be sorted by default values");
	public static final ErrorDescription ATTRIBUTE_CANNOT_BE_READ_ONLY_AND_WRITE_ONLY = new ErrorDescription("An attribute cannot be both read-only and write-only");
	public static final ErrorDescription NO_DEFAULT_VALUE_FOR_READ_ONLY_ATTRIBUTE = new ErrorDescription("A read-only attribute cannot have a default value");
	public static final ErrorDescription CANNOT_REFER_TO_WRITEONLY_ATTRIBUTE = new ErrorDescription("A slot cannot refer to a write-only attribute");
	public static final ErrorDescription CANNOT_SET_READONLY_ATTRIBUTE = new ErrorDescription("A slot cannot set a value for a read-only attribute");
	public static final ErrorDescription MUST_HAVE_SLOT_FOR_ATTRIBUTES_WITHOUT_DEFAULT = new ErrorDescription("Slots must exist for all attributes without default values");
	public static final ErrorDescription HYPERPORT_MUST_ALLOW_MULTIPLE = new ErrorDescription("A hyperport must allow many connectors");
	public static final ErrorDescription CANNOT_RESEMBLE_REDEF_RETIRED_ELEMENT = new ErrorDescription("Cannot resemble or redefine a retired element");
	public static final ErrorDescription RETIRE_MUST_EVOLVE_ONLY = new ErrorDescription("A retirement must only resemble and redefine the element it retires");
	public static final ErrorDescription DIAGRAM_ELEMENT_REFERS_TO_RETIRED = new ErrorDescription("Cannot show retired element here");
	public static final ErrorDescription NO_PART_TYPE = new ErrorDescription("A part must have a type");
	public static final ErrorDescription PART_REFERS_TO_RETIRED_TYPE = new ErrorDescription("Part refers to a retired type");
	public static final ErrorDescription PORT_REFERS_TO_RETIRED_INTERFACE = new ErrorDescription("Port refers to retired interface");
	public static final ErrorDescription CANNOT_REPLACE_ELEMENT_IN_CHECK_ONCE_STRATUM = new ErrorDescription("Cannot replace an element in a 'check once' stratum");
}
