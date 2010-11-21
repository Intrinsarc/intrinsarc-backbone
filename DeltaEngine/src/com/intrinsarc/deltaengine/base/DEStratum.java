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

import java.util.*;

public abstract class DEStratum extends DEObject
{
	/** abstract functions that are repository specific */
  public abstract boolean isRelaxed();
  public abstract boolean isDestructive();
  public abstract boolean isReadOnly();
  public abstract boolean isCheckOnceIfReadOnly();
  /** get the raw dependency list */
  public abstract List<? extends DEStratum> getRawDependsOn();
  /** get any packages or strata that are nested directly in this */
  public abstract List<? extends DEStratum> getDirectlyNestedPackages();
	public abstract List<DEElement> getChildElements();
	public abstract String getJavaPackage();

  /**
   * flattened is the full transformation down to a flat package structure where every package
   * is explicit about its full dependency set.  Used to check circularity.
   */
  private Set<DEStratum> transitive;

  /**
   * this is the same as transitive, but also includes this package
   */
  private Set<DEStratum> transitivePlusMe;

  /**
   * stratumVisibleFlattened is the full transformation down to a flat package structure, as above, but respecting the
   * strict/relaxed stratum rules. 
   */
  private Set<DEStratum> canSee;
  private Set<DEStratum> canSeePlusMe;

  /**
   * visibleDistinctStratumFlattened is the visible stratum from this one, removing any stratum of direct dependencies 
   */
  private Set<DEStratum> simpleDependsOn;

  /**
   * this is all the strata that a dependency to this stratum will expose the client to
   */
  private Set<DEStratum> exposesStrata;

  /**
   * need an engine to locate objects during the course of computation
   */
  public DEStratum()
  {
    super();
  }

  public DEStratum asStratum()
  {
    return this;
  }
  
  /**
   * (formal)
   * returns anything we directly depend on, taking away what any children depend on
   * NOTE: only valid for a stratum
   * @return
   */
  public Set<DEStratum> getSimpleDependsOn()
  {
    if (simpleDependsOn == null)
    {
      simpleDependsOn = new HashSet<DEStratum>(getTransitive());
      // take away the dependencies of any nested children, and other strata we depend on
      for (DEStratum child : getDirectlyNestedPackages())
        simpleDependsOn.removeAll(child.getTransitive());
      for (DEStratum dep : getRawDependsOn())
        simpleDependsOn.removeAll(dep.getTransitive());
    }
    return simpleDependsOn;
  }
  
  /**
   * (formal)
   * same as canSee, but add this also...
   * @return
   */
  public Set<? extends DEStratum> getCanSee()
  {
    if (canSee == null)
    {
      canSee = new LinkedHashSet<DEStratum>();
      collectTransitiveDependencies(canSee, false);
    }
    return canSee;
  }
  
  /**
   * (formal)
   * same as canSee, but add this also...
   * @return
   */
  public Set<? extends DEStratum> getCanSeePlusMe()
  {
    if (canSeePlusMe == null)
    {
      canSeePlusMe = new LinkedHashSet<DEStratum>(getCanSee());
      canSeePlusMe.add(this);
    }
    return canSeePlusMe;
  }
  
  /**
   * same as getVisibleFlattenedDependencies, but does not respect visibility.  i.e. use this for circularity checking
   * @return
   */
  public Set<DEStratum> getTransitive()
  {
    if (transitive == null)
    {
      transitive = new LinkedHashSet<DEStratum>();
      collectTransitiveDependencies(transitive, true);
    }
    return transitive;
  }
  
  /**
   * same as getTransitive((), but includes this also
   * @return
   */
  public Set<DEStratum> getTransitivePlusMe()
  {
    if (transitivePlusMe == null)
    {
      transitivePlusMe = new LinkedHashSet<DEStratum>(getTransitive());
      transitivePlusMe.add(this);
    }
    return transitivePlusMe;
  }
  
  /**
   * works out what strata will be exposed by this is another stratum depends on this.
   * not intended to be called for packages.
   * @return
   */
  public Collection<? extends DEStratum> getExposesStrata()
  {
    if (exposesStrata == null)
    {
      exposesStrata = new LinkedHashSet<DEStratum>();
      collectExposesDependencies(exposesStrata, false);
    }
    return exposesStrata;
  }  
  
  /**
   * is this circular?  i.e. is this contained in the full set of dependencies
   * @return
   */
  public boolean isCircular()
  {
    return getTransitive().contains(this);
  }
  
  ////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// private methods ////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////
  
  /** collect transitive dependencies for this strata */
  private void collectTransitiveDependencies(Set<DEStratum> strata, boolean ignoreRelaxed)
  {
    // if we are already present, don't bother
    if (strata.contains(this))
      return;

    // we can directly see any children
    addExposesDependencies(strata, getDirectlyNestedPackages(), true, ignoreRelaxed);
    
    // we can see anything that we depend on
    addExposesDependencies(strata, getRawDependsOn(), false, ignoreRelaxed);
    
    // we can see anything that our parents directly depend on
    DEObject parent = getParent();
    while (parent != null)
    {
      DEStratum pkg = parent.asStratum();
      pkg.addExposesDependencies(strata, pkg.getRawDependsOn(), false, ignoreRelaxed);
      parent = parent.getParent();
    }
  }

  /** collect exposures for this strata */
  private void collectExposesDependencies(Set<DEStratum> strata, boolean ignoreRelaxed)
  {
    // if we are already present, don't bother
    if (strata.contains(this))
      return;

    // we expose ourself always...
    strata.add(this);
    
    // we expose any directly nested strata that we explicitly depend on
    
    // if we are ignoring relaxed/strict rules, then take anything nested and anything we depend on
    // otherwise, if we are relaxed expose only nested packages, nested stratum we depend on and external dependencies
    //            if we are strict, expose only nested packages and nested stratum we depend on
    for (DEStratum pkg: getDirectlyNestedPackages())
    	if (ignoreRelaxed)
    		pkg.collectExposesDependencies(strata, ignoreRelaxed);
    for (DEStratum pkg: getRawDependsOn())
    	if (ignoreRelaxed || isRelaxed() || isNested(pkg))
    		pkg.collectExposesDependencies(strata, ignoreRelaxed);
  }
  
  /** recursively follow any dependencies */
  private void addExposesDependencies(Set<DEStratum> strata, List<? extends DEStratum> dependsOn, boolean nested, boolean ignoreRelaxed)
  {
    for (DEStratum pkg: dependsOn)
    {
      if (isNested(pkg) == nested)
        pkg.collectExposesDependencies(strata, ignoreRelaxed);
    }
  }
  
  /** is the package one of our (possibly indirectly) nested children? */
  private boolean isNested(DEStratum pkg)
  {
    // can we travel up to meet ourselves?
    DEObject parent = pkg.getParent();
    while (parent != null)
    {
      if (parent == this)
        return true;
      parent = parent.getParent();
    }
    return false;
  }
  
  // utility functions
  public List<DEStratum> determineOrderedPackages(boolean onlyNested, boolean tolerateCircularity)
  {
  	List<DEStratum> starts = new ArrayList<DEStratum>();
  	
  	starts.add(this);
  	return determineOrderedPackages(starts, onlyNested, tolerateCircularity);
  }
  
  public static List<DEStratum> determineOrderedPackages(List<DEStratum> start, boolean onlyNested, boolean tolerateCircularity)
  {
  	// sort the packages based on the transitive closure
  	if (onlyNested)
  		return new ArrayList<DEStratum>(expandNestedPackages(start));
  	else
  	{  		
  		Set<DEStratum> pkgs = new HashSet<DEStratum>();
  		for (DEStratum pkg : start)
  		  pkgs.addAll(pkg.getTransitivePlusMe());
  		
    	// create a graph and topologically sort
    	SimpleDirectedGraph<DEStratum> graph = new SimpleDirectedGraph<DEStratum>();
    	for (DEStratum p : pkgs)
    		graph.addNode(p);    	
    	for (DEStratum p : pkgs)
    		for (DEStratum dep : p.getTransitive())
    			graph.addEdge(p, dep);

    	List<DEStratum> sorted = graph.makeTopologicalSort(tolerateCircularity);
    	return sorted;
  	}
  }
  
	private static Set<DEStratum> expandNestedPackages(List<DEStratum> pkgs)
	{
  	Set<DEStratum> expanded = new HashSet<DEStratum>(pkgs);
  	int size;
  	do
  	{
  		size = expanded.size();
  		for (DEStratum pkg : new ArrayList<DEStratum>(expanded))
  			expanded.addAll(pkg.getDirectlyNestedPackages());
  	}
  	while (size != expanded.size());
  	
  	return expanded;
	}
	
	public static List<DEStratum> filterOutChildren(List<DEStratum> all)
	{
		List<DEStratum> filtered = new ArrayList<DEStratum>();
		for (DEStratum a : all)
		{
			boolean parent = false;
			for (DEStratum b : all)
				if (a.getParent() == b)
				{
					parent = true;
					break;
				}
			if (!parent)
				filtered.add(a);
		}
		return filtered;
	}
	
	public abstract void forceParent(DEStratum root);
}
