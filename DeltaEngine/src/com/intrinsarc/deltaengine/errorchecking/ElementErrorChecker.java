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

public class ElementErrorChecker
{
  private DEStratum perspective;
  private DEElement element;
  private ErrorRegister errors;

  public ElementErrorChecker(
      DEStratum perspective,
      DEElement element,
      ErrorRegister errors)
  {
    this.perspective = perspective;
    this.element = element;
    this.errors = errors;
  }

  public void performCheck(boolean diagramCheck)
  {
    DEStratum myHome = element.getHomeStratum();
    
  	// if this is replacing, and we are not at home, don't bother
  	if (element.isReplacement() && perspective != myHome)
  		return;
  	
    // element must be visible
    if (diagramCheck && !perspective.getCanSeePlusMe().contains(myHome))
      errors.addError(
          new ErrorLocation(perspective, element), ErrorCatalog.DIAGRAM_ELEMENT_NOT_VISIBLE);
    
    // element must not be retired
    if (diagramCheck && element.isRetired(perspective) && !(element.isRawRetired() && myHome == perspective))
      errors.addError(
          new ErrorLocation(perspective, element), ErrorCatalog.DIAGRAM_ELEMENT_REFERS_TO_RETIRED);

    // must have a name
    if ((element.getName() == null || element.getName().length() == 0) && !element.isReplacement())
      errors.addError(
          new ErrorLocation(myHome, element), ErrorCatalog.ELEMENT_NAME);
    
    // anything we substitute must be visible, and there must only be 1 substitution
    Set<DEElement> redefs = element.getReplaces();
    if (redefs.size() > 1)
      errors.addError(
          new ErrorLocation(perspective, element), ErrorCatalog.MAX_ONE_SUBSTITUTION);

    // anything we resemble must be visible
    Set<DEStratum> visible = new LinkedHashSet<DEStratum>(myHome.getCanSeePlusMe());
    for (DEElement resemble : element.getRawResembles())
    {
      if (!visible.contains(resemble.getHomeStratum()))
        errors.addError(
            new ErrorLocation(perspective, element), ErrorCatalog.RESEMBLES_VISIBLE);
      
      // cannot refer directly to a substitution
      checkReferenceToSubstitution(resemble, element);
      
      // cannot refer to a retired element, unless we are retiring also
      if (!element.isRetired(perspective))
      {
      	if (resemble.isRetired(perspective))
      		errors.addError(
      				new ErrorLocation(perspective, element), ErrorCatalog.CANNOT_RESEMBLE_REDEF_RETIRED_ELEMENT);
      }
    }

    for (DEElement redef : redefs)
    {
      DEStratum redefHome = redef.getHomeStratum();
      if (!visible.contains(redefHome))
        errors.addError(
            new ErrorLocation(perspective, element), ErrorCatalog.SUBSTITUTION_VISIBLE);
      
      // cannot refer to a retired element, unless we are retiring also
      if (!element.isRetired(perspective))
      	if (redef.isRetired(perspective))
      		errors.addError(
      				new ErrorLocation(perspective, element), ErrorCatalog.CANNOT_RESEMBLE_REDEF_RETIRED_ELEMENT);

      // a redef can't be of a component in the same stratum
      if (redefHome == myHome)
        errors.addError(
            new ErrorLocation(perspective, element), ErrorCatalog.SUBSTITUTION_IN_ANOTHER_STRATUM);
      
      // cannot refer directly to a substitution
      checkReferenceToSubstitution(redef, element);
    }
    
    // if we are retiring an element, we must only evolve it, and involve no others
    if (perspective == myHome && element.isRawRetired())
    {
    	if (!element.getRawResembles().equals(new ArrayList<DEElement>(redefs)) || redefs.size() != 1)
    	{
    		errors.addError(
    				new ErrorLocation(perspective, element), ErrorCatalog.RETIRE_MUST_EVOLVE_ONLY);
    	}
    }
    
	  // ensure we don't have a resemblance circularity
	  if (element.hasDirectCircularResemblance(perspective))
	    errors.addError(
	        new ErrorLocation(perspective, element), ErrorCatalog.RESEMBLANCE_DIRECT_CIRCULARITY);
	  else
	  if (element.hasIndirectCircularResemblance(perspective))
	    errors.addError(
	        new ErrorLocation(perspective, element), ErrorCatalog.RESEMBLANCE_INDIRECT_CIRCULARITY);
	  
	  // ensure that we don't have 2 items with the same uuid
	  for (ConstituentTypeEnum type : ConstituentTypeEnum.values())
	  {
	    IDeltas deltas = element.getDeltas(type);
	    Set<DeltaPair> constituents = deltas.getConstituents(perspective);
	    for (DeltaPair pair : constituents)
	    {
	      for (DeltaPair other : constituents)
	      {
	        if (pair != other && pair.getUuid().equals(other.getUuid()))
	        {
	        	// errors for stereotypes should be raised on the actual element itself
		      	if (type != ConstituentTypeEnum.DELTA_APPLIED_STEREOTYPE)
		          errors.addError(
		              new ErrorLocation(
		                  perspective,
		                  element, 
		                  pair.getConstituent()),
		                  ErrorCatalog.REPLACE_CONFLICT);
		      	else
		          errors.addError(
		              new ErrorLocation(
		                  perspective,
		                  element),
		                  ErrorCatalog.STEREOTYPE_REPLACE_CONFLICT);		      		
	        }
	      }
	    }
	  }    
    
    // a destructive element must be in a destructive stratum
	  DEStratum home = element.getHomeStratum();
    if (perspective == home && element.isDestructive() && !element.getHomeStratum().isDestructive())
      errors.addError(
          new ErrorLocation(element), ErrorCatalog.HOME_OF_DESTRUCTIVE_ELEMENT_MUST_BE_DESTRUCTIVE_ALSO);
    
    // we cannot replace an element in a stratum with "check once if read only" set
    if (element.isReplacement())
    {
    	for (DEElement replace : element.getReplaces())
    	{
    		DEStratum replaceHome = replace.getHomeStratum();
    		if (replaceHome.isReadOnly() && replaceHome.isCheckOnceIfReadOnly())
    			errors.addError(
    					new ErrorLocation(element), ErrorCatalog.CANNOT_REPLACE_ELEMENT_IN_CHECK_ONCE_STRATUM);
    	}
    }
    
  	// any stereotype must be visible
		for (DEAppliedStereotype stereos : element.getRawAppliedStereotypes())
		{
			if (!perspective.getCanSeePlusMe().contains(stereos.getStereotype().getHomeStratum()))
	      errors.addError(
	          new ErrorLocation(perspective, element), ErrorCatalog.STEREOTYPE_NOT_VISIBLE);  				
		}
		
		// stereotype of any added or replaced constituents must be visible
		for (ConstituentTypeEnum type : ConstituentTypeEnum.values())
		{
			for (DeltaPair pair : element.getDeltas(type).getAddObjects())
			{
				if (pair.getConstituent().getAppliedStereotypes() != null)
					for (DEAppliedStereotype stereos : pair.getConstituent().getAppliedStereotypes())
					{
						if (!perspective.getCanSeePlusMe().contains(stereos.getStereotype().getHomeStratum()))
						{
				      errors.addError(
				          new ErrorLocation(element, pair.getConstituent()), ErrorCatalog.STEREOTYPE_NOT_VISIBLE);
						}
					}				
			}
			for (DeltaPair pair : element.getDeltas(type).getReplaceObjects())
			{
				if (pair.getConstituent().getAppliedStereotypes() != null)
					for (DEAppliedStereotype stereos : pair.getConstituent().getAppliedStereotypes())
					{
						if (!perspective.getCanSeePlusMe().contains(stereos.getStereotype().getHomeStratum()))
				      errors.addError(
				          new ErrorLocation(element, pair.getConstituent()), ErrorCatalog.STEREOTYPE_NOT_VISIBLE);  				
					}				
			}
		}
  }

  private void checkReferenceToSubstitution(DEElement referenced, DEObject element)
  {
    if (referenced.isReplacement())
      errors.addError(
          new ErrorLocation(element), ErrorCatalog.CANNOT_REFER_TO_SUBSTITUTION);          
  }
  
  public static boolean isValidClassName(String full)
  {
  	int length = full.length();
  	if (full == null || length == 0)
  		return false;
  	
  	if (!Character.isJavaIdentifierPart(full.charAt(0)) || !Character.isJavaIdentifierPart(full.charAt(length-1)))
  		return false;
  	
  	StringTokenizer tok = new StringTokenizer(full, ".");
  	while (tok.hasMoreTokens())
  	{
  		String t = tok.nextToken();
  		if (!isValidJavaPart(t))
  			return false;
  	}
  	return true;
  }
  
	private static boolean isValidJavaPart(String part)
	{
		if (part == null || part.length() == 0)
			return false;
		
		// must start properly
		if (!Character.isJavaIdentifierStart(part.charAt(0)))
			return false;
		
		// check the rest
		int length = part.length();
		for (int lp = 1; lp < length; lp++)
			if (!Character.isJavaIdentifierPart(part.charAt(lp)))
				return false;
		
		return true;
	}

	public static void checkStratumPackageExists(ErrorRegister errors, DEElement element)
	{
		DEStratum home = element.getHomeStratum();
		String pkg = home.getJavaPackage();
    if ((pkg == null || pkg.length() == 0) && !element.hasForcedImplementationClass(home))
    	errors.addError(new ErrorLocation(home), ErrorCatalog.NO_STRATUM_PACKAGE);
	}
}

