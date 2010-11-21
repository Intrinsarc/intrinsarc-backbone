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
import java.util.Map.*;

import com.intrinsarc.deltaengine.base.*;

/**
 * a central list of the issues that have been recorded by the last check.
 * NOTE: issue is a general term for both errors and warnings.
 * @author andrew
 */
/**
 * a central list of the issues that have been recorded by the last check.
 * NOTE: issue is a general term for both errors and warnings.
 * @author andrew
 */
public class ErrorRegister
{
  private Map<ErrorLocation, Set<ErrorDescription>> locationToIssues = new HashMap<ErrorLocation, Set<ErrorDescription>>();
  private boolean enabled;
  
  public ErrorRegister()
  {
  }

  public void clear()
  {
    locationToIssues.clear();
  }
  
  public Map<ErrorLocation, Set<ErrorDescription>> getErrors(ErrorLocation location, boolean matchDirectly, boolean omitDiagramErrors)
  {
    Map<ErrorLocation, Set<ErrorDescription>> errors = new HashMap<ErrorLocation, Set<ErrorDescription>>();
    addAll(errors, location, omitDiagramErrors);
    
    // get anything where we are the final destination -- only really good for strata/packages
    // as almost anything requires more visual context
    if (matchDirectly)
      for (ErrorLocation error : locationToIssues.keySet())
      {
        if (error.matchesFinalDestination(location.getFirstUuid()))
        	addAll(errors, error, omitDiagramErrors);
      }

    return errors;
  }
  
  private void addAll(Map<ErrorLocation, Set<ErrorDescription>> errors, ErrorLocation location, boolean omitDiagramErrors)
  {
  	// try different levels of "resolution"
  	Set<ErrorDescription> descriptions = locationToIssues.get(location);
  	if (descriptions == null)
  		descriptions = locationToIssues.get(location.firstAndSecondOnly());
  	if (descriptions == null)
  		descriptions = locationToIssues.get(location.firstOnly());
  	
  	if (descriptions != null)
  	{
	  	Set<ErrorDescription> filtered = new LinkedHashSet<ErrorDescription>();
	  	for (ErrorDescription description : descriptions)
	  		if (!description.isDiagramOnly() || !omitDiagramErrors)
	  			filtered.add(description);
	  	if (!filtered.isEmpty())
	  		errors.put(location, filtered);
  	}
  }

  public Map<ErrorLocation, Set<ErrorDescription>> getErrors(String uuid)
  {
    // only gets direct errors, used for browser
    Map<ErrorLocation, Set<ErrorDescription>> errors = new HashMap<ErrorLocation, Set<ErrorDescription>>();
    for (Entry<ErrorLocation, Set<ErrorDescription>> error : locationToIssues.entrySet())
    {
      if (error.getKey().matchesFinalDestination(uuid))
      {
        // if the location doesn't exist create it
        ErrorLocation loc = error.getKey();
        Set<ErrorDescription> errs = error.getValue();
        
        Set<ErrorDescription> existing = errors.get(loc);
        if (existing == null)
        {
          existing = new LinkedHashSet<ErrorDescription>();
          errors.put(loc, existing);
        }
        existing.addAll(errs);
      }
    }
    return errors;
  }
  
  public void addError(ErrorLocation location, ErrorDescription issue)
  {
    Set<ErrorDescription> issues = locationToIssues.get(location);
    if (issues == null)
    {
      issues = new LinkedHashSet<ErrorDescription>();
      locationToIssues.put(location, issues);
    }
    issues.add(issue);
  }
  
  public Map<ErrorLocation, Set<ErrorDescription>> getAllErrors()
  {
    return locationToIssues;
  }
  
  public boolean isInHierarchyOfErrors(String uuid)
  {
    for (ErrorLocation error : locationToIssues.keySet())
    {
      if (error.elementIsInErrorHierarchy(uuid))
        return true;
    }
    return false;
  }
  
  public boolean matchesDirectly(String uuid)
  {
    for (ErrorLocation error : locationToIssues.keySet())
    {
      if (error.matchesFinalDestination(uuid))
        return true;
    }
    return false;
  }
  
  public boolean isEnabled()
  {
    return enabled;
  }
  
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

	public int countErrors()
	{
		int count = 0;
		for (Set<ErrorDescription> description : locationToIssues.values())
			count += description.size();
		return count;
	}

	public void printAll()
	{
    for (Entry<ErrorLocation, Set<ErrorDescription>> error : locationToIssues.entrySet())
    {
    	System.out.println("Error location = " + error.getKey());
    	for (ErrorDescription desc : error.getValue())
    		System.out.println("   > " + desc.toString());
    }
	}
}
