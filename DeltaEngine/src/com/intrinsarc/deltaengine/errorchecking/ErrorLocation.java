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


public class ErrorLocation
{
  private String thirdUuid;
  private String secondUuid;
  private String firstUuid;
  private List<String> hierarchy = new ArrayList<String>();
  private ErrorLocation firstOnly;
  private ErrorLocation firstAndSecondOnly;
  private String readableError;

  /**
   * this is where the error should be signalled.  e.g. for a component in error that is not in it's home stratum, then
   * the final destination is the stratum.  A similar process applies to ports etc.
   */
  private String finalDestinationUUID;

  /** use for an error which doesn't depend on the scope.  e.g. a stratum cycle
   * @param element
   */
  public ErrorLocation(DEObject first)
  {
    this(null, null, first);
  }
  
  /** use for an error which is valid only in a stratum, say.
   * @param element
   */
  public ErrorLocation(DEObject second, DEObject first)
  {
    this(null, second, first);
  }
  
  /**
   * use for an error which does depend on scope.  e.g. a port found through resemblance that is not corrected properly.
   * note: the port may not be directly owned by the owner, or the owner located in the stratum, due to the resemblance
   *       and substitution rules.
   *       
   *       A stratum in error is (null, null, stratum) and final destination is stratum
   *       A component (at home) in error is (null, stratum, component) and finalDestination is component
   *       A component (not at home) in error is (null, stratum, component) and finalDestination is stratum.
   *       A port (at home for both component and port) in error is (stratum, component, port) and finalDestination is port
   *       A port (at home for component, not at home for port) in error is (stratum, component, port) and finalDestination is component
   *       A port (not at home for component or port) in error is (stratum, component, port) and finalDestination is stratum
   * @param stratum
   * @param owner
   * @param element
   */
  public ErrorLocation(DEObject third, DEObject second, DEObject first)
  {
  	if (third != null)
  		this.thirdUuid = third.getUuid();
  	if (second != null)
  		this.secondUuid = second.getUuid();
    this.firstUuid = first.getUuid();
    
    // if the second is not actually parented by the third, the third is the final destination
    DEObject startHierarchy = first;
    if (second != null && !parentOf(second, first))
    	startHierarchy = second;
    if (third != null && !parentOf(third, second))
    	startHierarchy = third;
    determineHierarchy(startHierarchy);

    // cache the derived error locations
    if (third != null)
    	firstAndSecondOnly = new ErrorLocation(null, second, first);
    else
    	firstAndSecondOnly = this;
    if (third != null || second != null)
    	firstOnly = new ErrorLocation(null, null, first);
    else
    	firstOnly = this;

    readableError =
    	"(" + translate(third, true) + translate(second, true) + translate(first, false) + ")";
  }

	private String translate(DEObject element, boolean addDelimiter)
	{
	  if (element == null)
	    return "";
	  
	  String delimiter = DEObject.SEPARATOR;
	  if (!addDelimiter)
	    delimiter = "";
	  
	  String name = element.getName();
	  if (name == null || name.length() == 0)
	    name = "<???>";
	  return name + delimiter;
	}

	private boolean parentOf(DEObject parent, DEObject child)
  {
    return child.getParent() == parent;
  }

  public boolean elementIsInErrorHierarchy(String uuid)
  {
    return hierarchy.contains(uuid);
  }
  
  public boolean matchesFinalDestination(String uuid)
  {
    return compareUuid(uuid, finalDestinationUUID);
  }
  
  public boolean matchesFirst(DEObject element)
  {
  	return compareUuid(element.getUuid(), firstUuid);
  }
  
  private void determineHierarchy(DEObject element)
  {
    finalDestinationUUID = element.getUuid();
    element = element.getParent();
    
    while (element != null)
    {
      hierarchy.add(element.getUuid());
      element = element.getParent();
    }
  }

  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ErrorLocation))
      return false;
    ErrorLocation other = (ErrorLocation) obj;
    return
    	compareUuid(thirdUuid, other.thirdUuid) &&
    	compareUuid(secondUuid, other.secondUuid) &&
    	compareUuid(firstUuid, other.firstUuid);
  }

  private boolean compareUuid(String a, String b)
  {
    String uuidA = (a == null ? "" : a);
    String uuidB = (b == null ? "" : b);
    return uuidA.equals(uuidB);
  }

  @Override
  public int hashCode()
  {
    int hash = firstUuid.hashCode();
    if (secondUuid != null)
      hash ^= secondUuid.hashCode();
    if (thirdUuid != null)
      hash ^= thirdUuid.hashCode();
    return hash;
  }

  public String toString()
  {
    return readableError;
  }

  public ErrorLocation firstAndSecondOnly()
  {
    return firstAndSecondOnly;
  }

  public ErrorLocation firstOnly()
  {
    return firstOnly;
  }

	public String getThirdUuid()
	{
		return thirdUuid;
	}

	public String getSecondUuid()
	{
		return secondUuid;
	}

	public String getFirstUuid()
	{
		return firstUuid;
	}
}
