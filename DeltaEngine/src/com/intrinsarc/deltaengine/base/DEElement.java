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

public abstract class DEElement extends DEObject
{
	public static final String IMPLEMENTATION_STEREOTYPE_PROPERTY = "force-implementation";

	private Map<DEStratum, Set<DEElement>> cachedResembles_e;
  private Map<DEStratum, Set<DEElement>> cachedFilteredResembles_e;
  private Map<DEStratum, Set<DEElement>> cachedResemblance_eClosure;
  private Map<DEStratum, Set<DEElement>> cachedFilteredResemblance_eClosure;
  private Map<DEStratum, Boolean> cachedDirectlyCircular;
  private Map<DEStratum, Boolean> cachedIndirectlyCircular;
  private Map<DEStratum, List<DEElement>> cachedTopmost;
  private HashSet<DEElement> cachedReplaces;

	private Map<DEStratum, Set<DEElement>> superCachedImmediate;
	private Map<DEStratum, Set<DEElement>> superCachedClosure;
	private Map<DEStratum, Set<DEElement>> subCachedImmediate;
	private Map<DEStratum, Set<DEElement>> subCachedClosure;
	private Map<DEStratum, Set<DEElement>> subWithoutCachedClosure;
	private Map<DEStratum, Set<DEElement>> superWithoutCachedClosure;
	private Map<DEStratum, Set<DEElement>> inheritanceTree;
  private Map<ConstituentTypePerspective, Set<DEConstituent>> cachedCollect; 
	private Boolean shallowDestructive;
	private Set<DEStratum> expanded;
	

  public abstract List<? extends DEElement> getRawResembles();
  public abstract List<? extends DEElement> getRawReplaces();
  public abstract List<DEElement> getReplacers();
  public abstract Set<DEElement> getPossibleImmediateSubElements();
  public abstract Set<String> getReplaceUuidsOnly();
  public abstract boolean isRawRetired();
  public abstract boolean isRawAbstract();
  public abstract String getRawName();
    
  public DEElement()
  {
  	clearCollections();
  }
  
	public void clearCache(DEStratum perspective)
	{
		clearCollections(perspective);
	}  

  public synchronized IDeltas getDeltas(ConstituentTypeEnum type)
  {
  	// possibly retired?
  	if (type == ConstituentTypeEnum.DELTA_MISC)
  	{
    	Set<DeltaPair> rets = new LinkedHashSet<DeltaPair>();
    	if (isRawRetired())
    		rets.add(new DeltaPair("retired", new TagConstituent("retired", "r")));
  		rets.add(new DeltaPair("abstract", new TagConstituent("abstract", isRawAbstract() ? "a" : "")));
    	return new Deltas(
      		this,
      		new HashSet<DeltaPair>(),
      		new HashSet<String>(),
      		rets,
      		ConstituentTypeEnum.DELTA_MISC);
  	}
  	return new DummyDeltas();
  }

  private void clearCollections()
	{
    cachedResembles_e = new HashMap<DEStratum, Set<DEElement>>();
    cachedFilteredResembles_e = new HashMap<DEStratum, Set<DEElement>>();
    cachedResemblance_eClosure = new HashMap<DEStratum, Set<DEElement>>();
    cachedFilteredResemblance_eClosure = new HashMap<DEStratum, Set<DEElement>>();
    cachedDirectlyCircular = new HashMap<DEStratum, Boolean>();
    cachedIndirectlyCircular = new HashMap<DEStratum, Boolean>();
    cachedTopmost = new HashMap<DEStratum, List<DEElement>>();
    
  	superCachedImmediate = new HashMap<DEStratum, Set<DEElement>>();
  	superCachedClosure = new HashMap<DEStratum, Set<DEElement>>();
  	subCachedImmediate = new HashMap<DEStratum, Set<DEElement>>();
  	subCachedClosure = new HashMap<DEStratum, Set<DEElement>>();
  	subWithoutCachedClosure = new HashMap<DEStratum, Set<DEElement>>();
  	superWithoutCachedClosure = new HashMap<DEStratum, Set<DEElement>>();
  	inheritanceTree = new HashMap<DEStratum, Set<DEElement>>();
  	cachedCollect = new HashMap<ConstituentTypePerspective, Set<DEConstituent>>();
  	shallowDestructive = null;		
	}
	
  private void clearCollections(DEStratum perspective)
	{
    cachedResembles_e.remove(perspective);
    cachedFilteredResembles_e.remove(perspective);
    cachedResemblance_eClosure.remove(perspective);
    cachedFilteredResemblance_eClosure.remove(perspective);
    cachedDirectlyCircular.remove(perspective);
    cachedIndirectlyCircular.remove(perspective);
    cachedTopmost.remove(perspective);
    
  	superCachedImmediate.remove(perspective);
  	superCachedClosure.remove(perspective);
  	subCachedImmediate.remove(perspective);
  	subCachedClosure.remove(perspective);
  	subWithoutCachedClosure.remove(perspective);
  	superWithoutCachedClosure.remove(perspective);
  	inheritanceTree.remove(perspective);
  	cachedCollect.remove(perspective);
  	shallowDestructive = null;		
	}
	
  public String getName()
  {
  	return getRawName();
  }
  
  public boolean isRetired(DEStratum perspective)
  {
  	for (DeltaPair p : getDeltas(ConstituentTypeEnum.DELTA_MISC).getConstituents(perspective))
  		if (p.getConstituent().asTag().getName().equals("r"))
  			return true;
  	return false;
  }
  
  public boolean isAbstract(DEStratum perspective)
  {
  	for (DeltaPair p : getDeltas(ConstituentTypeEnum.DELTA_MISC).getConstituents(perspective))
  		if (p.getConstituent().asTag().getName().equals("a"))
  			return true;
  	return false;
  }
  
  public String getTopName()
  {
  	// find the very topmost perspective
  	DEStratum top = getHomeStratum();
  	while (top.getParent() != null)
  		top = top.getParentStratum();
  	return getName(top);
  }
  
  public String getName(DEStratum perspective)
  {
  	if (isReplacement())
  	{
  		if (getRawName().length() > 0)
  			return getRawName() + "`";
  		return getName(perspective, null) + "`";
  	}
  	return getName(perspective, null);
  }
  
  public String getReplacesForName(DEStratum perspective)
  {
  	if (isReplacement())
  		return getReplaces().iterator().next().getFullyQualifiedName();
  	return "";
  }
  
  public String getName(DEStratum perspective, Set<DEStratum> lookFrom)
  {
  	if (isReplacement())
  	{
  		if (getRawName().length() > 0)
  			return getRawName();
  		return getReplaces().iterator().next().getName(perspective, perspective.getSimpleDependsOn());
  	}
  	
  	// this is not a replacement, so look for possible replacers
  	List<DEElement> tops = lookFrom == null ? getTopmost(perspective) : getTopmost(lookFrom);
  	String name = "";
  	for (DEElement elem : tops)
  		if (elem.isReplacement())
  			if (elem.getName().length() > 0)
  			{
  				if (name.length() != 0)
  					name += " / ";
  				name += elem.getName(perspective, perspective.getSimpleDependsOn());
  			}

  	return name.length() == 0 ? getName() : name;
  }
  
  /**
   * if we replace something, we are a replacement
   * @return
   */
  public boolean isReplacement()
  {
    return !getReplaces().isEmpty();
  }
  
  public Set<DEElement> getReplaces()
  {
    // return a cached value if we have it
    if (cachedReplaces != null)
      return cachedReplaces;
    
    cachedReplaces = new LinkedHashSet<DEElement>();
    for (DEElement element : getRawReplaces())
      if (element.getHomeStratum() != getHomeStratum())
        cachedReplaces.add(element);
    return cachedReplaces;
  }
  
  /**
   * is this referring to something which has circular resemblance?
   * note: anything directly circular will also return true for this call.
   * @param perspective
   * @return
   */
  public boolean hasIndirectCircularResemblance(DEStratum perspective)
  {
    Boolean indirect = cachedIndirectlyCircular.get(perspective);
    if (indirect == null)
    {
      // calculate whether this is indirectly circular
      if (hasDirectCircularResemblance(perspective))
        indirect = true;
      else
        for (DEElement r : getResembles_e(perspective))
        {
          if (r.hasIndirectCircularResemblance(perspective))
          {
            indirect = true;
            break;
          }
        }
      
      if (indirect == null)
        indirect = false;
      
      cachedIndirectlyCircular.put(perspective, indirect);
    }
    
    return indirect;
  }
  
  /**
   * is the expanded resemblance circular from the stratum perspective?
   * @param myHome
   * @return
   */
  public boolean hasDirectCircularResemblance(DEStratum perspective)
  {
    if (cachedDirectlyCircular.get(perspective) == null)
      cachedDirectlyCircular.put(perspective, getResemblance_eClosure(perspective).contains(this));
    return cachedDirectlyCircular.get(perspective);
  }
  
  public Set<DEElement> getResembles(DEStratum perspective, boolean nonCircular, boolean allowFirstLevelCycles)
  {
  	// get topmost
  	List<DEElement> tops = getTopmost(perspective);
  	
  	// for each thing we resemble directly, add it in
  	// unless it is "ourself" and we are replacing.  if that is the case, add what we resemble directly
  	if (tops.size() == 1 && tops.get(0) == this)
  	{
  		Set<DEElement> raw =
  			nonCircular ?
  					getFilteredResembles_e(perspective, allowFirstLevelCycles) : getResembles_e(perspective);
  		Set<DEElement> result = new HashSet<DEElement>();
  		for (DEElement e : raw)
  			result.addAll(e.getReplacesOrSelf());
  		
  		return result;
  	}
  	
  	Set<DEElement> resembles = new HashSet<DEElement>();
  	for (DEElement top : tops)
  	{
  		for (DEElement res : nonCircular ? top.getFilteredResembles_e(perspective, allowFirstLevelCycles) : top.getResembles_e(perspective))
  		{
  			if (res == this)
  			{
  				for (DEElement down : res.getResembles_e(perspective))
  					resembles.addAll(down.getReplacesOrSelf());
  			}
  			else
  				resembles.addAll(res.getReplacesOrSelf());
  		}
  	}
  	
  	return resembles;  	
  }
  
  /**
   * returns a sensible graph with all circularities and redundancies eliminated.
   * aims to keep as much of the rest of the graph as possible.
   * @param perspective
   * @param allowFirstLevelCycles (optional) -- only used for the gui to show cycles in the resemblance graph.
   * @return
   */
  Set<DEElement> getFilteredResembles_e(DEStratum perspective)  
  {
    return getFilteredResembles_e(perspective, false);    
  }
  
  public Set<DEElement> getFilteredResembles_e(DEStratum perspective, boolean allowFirstLevelCycles)
  {
    // if we have this, reply immediately
    Set<DEElement> fromHere = cachedFilteredResembles_e.get(perspective);
    if (!allowFirstLevelCycles)
    {
      if (fromHere != null)
        return fromHere;
    }
    
  	// start with the raw resembles_e
    fromHere = new LinkedHashSet<DEElement>(getResembles_e(perspective));

    // eliminate any reference to self
    fromHere.remove(this);
    
    // remove any circular references
    for (DEElement r : new LinkedHashSet<DEElement>(fromHere))
      if (!allowFirstLevelCycles && r.hasDirectCircularResemblance(perspective))
        fromHere.remove(r);
    
    // to eliminate redundancy, remove any resembance_eClosure of resembles_e
    // to eliminate circularity, remove any resemblance that is circular in itself
    Set<DEElement> fullClosure = new LinkedHashSet<DEElement>();
    for (DEElement r : fromHere)
      fullClosure.addAll(r.getFilteredResemblance_eClosure(perspective));
    
    for (DEElement r : new LinkedHashSet<DEElement>(fromHere))
      if (fullClosure.contains(r))
        fromHere.remove(r);
    
    // only memoize if we are not allowing cycles
    if (!allowFirstLevelCycles)
      cachedFilteredResembles_e.put(perspective, fromHere);
    
    return fromHere;
  }
  
  /**
   * Resembles_e takes replacements and stratum perspective into account when calculating the resemblance graph.
   * Makes no effort to remove redundant paths, so can be used to check circularity.
   * @param perspective
   * @return
   */
  public Set<DEElement> getResembles_e(DEStratum perspective)
  {
    // if we have this, reply immediately
    Set<DEElement> fromHere = cachedResembles_e.get(perspective);
    if (fromHere != null)
      return fromHere;
    
    fromHere = new LinkedHashSet<DEElement>();
    
    // handle any redef/resemblance intersection by looking from the owner down (because a redef can't be redef'ed)
    Set<DEElement> both = new LinkedHashSet<DEElement>(getReplaces());
    both.retainAll(getRawResembles());
    for (DEElement b : both)
      fromHere.addAll(b.getTopmost(getHomeStratum().getSimpleDependsOn()));
    
    // handle any pure resemblance by looking from here down
    Set<DEStratum> here = new LinkedHashSet<DEStratum>();
    here.add(perspective);
    Set<DEElement> pure = new LinkedHashSet<DEElement>(getRawResembles());
    pure.removeAll(both);
    for (DEElement p : pure)
      fromHere.addAll(p.getTopmost(here));
    
    cachedResembles_e.put(perspective, fromHere);
    return fromHere;
  }
  
  private Set<DEElement> getResemblance_eClosure(DEStratum perspective)
  {
    Set<DEElement> closure = cachedResemblance_eClosure.get(perspective);
    if (closure != null)
      return closure;
    
    closure = new LinkedHashSet<DEElement>();
    collectResemblance_eClosure(perspective, closure, false);
    cachedResemblance_eClosure.put(perspective, closure);
    return closure;
  }

  private void collectResemblance_eClosure(DEStratum perspective, Set<DEElement> closure, boolean addThis)
  {
    boolean already = closure.contains(this);
    
    if (addThis)
      closure.add(this);
    if (!already)
    {
      for (DEElement r : getResembles_e(perspective))
        r.collectResemblance_eClosure(perspective, closure, true);
    }
  }
  
  public Set<DEElement> getFilteredResemblance_eClosure(DEStratum perspective)
  {
    Set<DEElement> closure = cachedFilteredResemblance_eClosure.get(perspective);
    if (closure != null)
      return closure;
    
    closure = new LinkedHashSet<DEElement>();
    collectFilteredResemblance_eClosure(perspective, closure, false);
    cachedFilteredResemblance_eClosure.put(perspective, closure);
    return closure;
  }
    
  private void collectFilteredResemblance_eClosure(DEStratum perspective, Set<DEElement> closure, boolean addThis)
  {
    boolean already = closure.contains(this);
    
    if (addThis)
      closure.add(this);
    if (!already)
      for (DEElement r : getResembles_e(perspective))
      {
        if (!r.hasDirectCircularResemblance(perspective))
          r.collectResemblance_eClosure(perspective, closure, true);
      }
  }
  
  public List<DEElement> getTopmost(DEStratum perspective)
  {
    // see if this has been cached
    List<DEElement> tops = cachedTopmost.get(perspective);    
    if (tops != null)
      return tops;
    
    Set<DEStratum> lookFrom = new LinkedHashSet<DEStratum>();
    lookFrom.add(perspective);
    tops = getTopmost(lookFrom);
    cachedTopmost.put(perspective, tops);
    return tops;
  }
  
  
  /** not cached, can be a bit slow */
  public List<DEElement> getTopmost(Set<DEStratum> lookFrom)
  {
  	// if we are at home, return just this
  	if (lookFrom.contains(getHomeStratum()))
  	{
  		List<DEElement> me = new ArrayList<DEElement>();
  		me.add(this);
  		return me;
  	}
  	
    // find the topmost replacements based on where we are looking from
    List<? extends DEElement> replacers = getReplacers();
    
    // find all strata we need to consider
    Set<DEStratum> all = new LinkedHashSet<DEStratum>();
    for (DEStratum look : lookFrom)
      all.addAll(look.getTransitivePlusMe());
    
    // find all substituters -- should only be in the packages
    Set<DEElement> relevantReplacers = new LinkedHashSet<DEElement>();
    for (DEElement r : replacers)
      if (all.contains(r.getHomeStratum()))
        relevantReplacers.add(r);
    
    // only use those that aren't covered elsewere
    Set<DEElement> topmostReplacers = new LinkedHashSet<DEElement>();
    for (DEElement r : relevantReplacers)
    {
      boolean contains = false;
      for (DEElement other : relevantReplacers)
      {
        if (r == other)
          continue;
        if (other.getHomeStratum().getTransitive().contains(r.getHomeStratum()))
          contains = true;
      }
      if (!contains)
      {
        topmostReplacers.add(r);
      }
    }
    
    if (topmostReplacers.isEmpty())
      topmostReplacers.add(this);
    
    List<DEElement> sorted = new ArrayList<DEElement>(topmostReplacers);
    Collections.sort(sorted, new Comparator<DEElement>()
    {
      public int compare(DEElement o1, DEElement o2)
      {
        return o1.getUuid().compareTo(o2.getUuid());
      }
    });
    
    return sorted;
  }
  
  
  public Set<DEElement> getImmediateSubElements(DEStratum perspective, boolean inheritanceOnly)
  {
  	// have we cached this?
  	Set<DEElement> cached = subCachedImmediate.get(perspective);
  	if (cached != null)
  		return cached;
  	
  	Set<? extends DEElement> possible = getPossibleImmediateSubElements();
  	cached = new HashSet<DEElement>();
  	
  	for (DEElement iface : possible)
  	{
  		// must be visible from this perspective
  		// also -- does "supers" include this, from this perspective?
  		if (perspective.getTransitivePlusMe().contains(iface.getHomeStratum()) &&
  				iface.getSuperElementClosure(perspective, inheritanceOnly).contains(this))
  		{
  			cached.add(iface);
  		}
  	}
  	subCachedImmediate.put(perspective, cached);
  	return cached;
  }
  
  private Set<DEElement> getSubElementClosureWithoutDeletesOrReplaces(DEStratum perspective)
	{  	
  	// have we cached this?
  	Set<DEElement> cached = subWithoutCachedClosure.get(perspective);
  	if (cached != null)
  		return cached;
  	
  	cached = new HashSet<DEElement>();
  	
  	// look downstream to see if the constituents are still present...
		for (DEElement element : getSubElementClosure(perspective, false))
		{
			boolean ok = true;
    	for (ConstituentTypeEnum constituentType : ConstituentTypeEnum.values())
    	{
    		Set<DEConstituent> constituents = collect(perspective, constituentType);
    		// having a different stereotype
  			if (constituentType != ConstituentTypeEnum.DELTA_APPLIED_STEREOTYPE &&
  					!element.collect(perspective, constituentType).containsAll(constituents))
  				ok = false;
  		}
    	if (ok)
    		cached.add(element);
		}
  	
  	subWithoutCachedClosure.put(perspective, cached);
  	return cached;
	}

  
  private Set<DEElement> getSuperElementClosureForInheritance(DEStratum perspective)
	{ 
  	// have we cached this?
  	Set<DEElement> cached = superWithoutCachedClosure.get(perspective);
  	if (cached != null)
  		return cached;
  	
  	cached = new HashSet<DEElement>();
  	
  	// look upstream to see if the constituents are still present...
		for (DEElement element : getSuperElementClosure(perspective, false))
		{
			boolean ok = true;
    	for (ConstituentTypeEnum constituentType : ConstituentTypeEnum.values())
    	{
    		Set<DEConstituent> constituents = collect(perspective, constituentType);
    		// having a different stereotype doesn't break subtyping
  			if (constituentType != ConstituentTypeEnum.DELTA_APPLIED_STEREOTYPE &&
  					!constituents.containsAll(element.collect(perspective, constituentType)))
  				ok = false;
  		}
    	if (ok)
    		cached.add(element);
		}
  	
  	superWithoutCachedClosure.put(perspective, cached);
  	return cached;
	}

  private Set<DEConstituent> collect(DEStratum perspective, ConstituentTypeEnum constituentType)
	{
  	// look in the cache first
  	ConstituentTypePerspective key = new ConstituentTypePerspective(perspective, constituentType);
  	Set<DEConstituent> constituents = cachedCollect.get(key);
  	if (constituents != null)
  		return constituents;
  	
  	// not found in cache, so compute
  	constituents = new HashSet<DEConstituent>();
  	for (DeltaPair pair : getDeltas(constituentType).getConstituents(perspective))
  		constituents.add(pair.getConstituent());
  	cachedCollect.put(key, constituents);
  	return constituents;
	}
  
  /** takes replacement into account */
	public Set<DEElement> getSubElementClosure(DEStratum perspective, boolean inheritanceOnly)
  {
		if (inheritanceOnly)
			return getSubElementClosureWithoutDeletesOrReplaces(perspective);

		// have we cached this?
  	Set<DEElement> cached = subCachedClosure.get(perspective);
  	if (cached != null)
  		return cached;
  	cached = new HashSet<DEElement>();
  	for (DEElement element : getImmediateSubElements(perspective, false))
  	{
  		cached.add(element);
  		cached.addAll(element.getSubElementClosure(perspective, false));
  	}

  	subCachedClosure.put(perspective, cached);
  	return cached;
  }
  
  public Set<DEElement> getImmediateSuperElements(DEStratum perspective)
  {
  	// have we cached this?
  	Set<DEElement> cached = superCachedImmediate.get(perspective);
  	if (cached != null)
  		return cached;
  	
  	cached = new HashSet<DEElement>();

		for (DEElement element : getResembles(perspective, false, false))
  		cached.add(element);

		superCachedImmediate.put(perspective, cached);
  	return cached;
  }
  
  public Set<DEElement> getInheritanceTree(DEStratum perspective)
  {
  	// have we cached this?
  	Set<DEElement> cached = inheritanceTree.get(perspective);
  	if (cached != null)
  		return cached;
  	
  	cached = new HashSet<DEElement>();
  	cached.add(this);
  	
  	// keep expanding until no more additions
  	int oldSize;
  	do
  	{
  		oldSize = cached.size();
  		for (DEElement elem : new HashSet<DEElement>(cached))
  		{
  			cached.addAll(elem.getSubElementClosure(perspective, true));
  			cached.addAll(elem.getSuperElementClosure(perspective, true));
  		}
  	} while (cached.size() != oldSize);
  	
  	inheritanceTree.put(perspective, cached);
  	return cached;

  }
  
  public Set<DEElement> getSuperElementClosure(DEStratum perspective, boolean inheritanceOnly)
  {
  	if (inheritanceOnly)
  		return getSuperElementClosureForInheritance(perspective);
  	
  	// have we cached this?
  	Set<DEElement> cached = superCachedClosure.get(perspective);
  	if (cached != null)
  		return cached;
  	
  	cached = new HashSet<DEElement>();
  	for (DEElement element : getImmediateSuperElements(perspective))
  	{
  		cached.add(element);
  		cached.addAll(element.getSuperElementClosure(perspective, false));
  	}
  	superCachedClosure.put(perspective, cached);
  	return cached;  	
  }
  
  public boolean isSubElementOf(DEStratum perspective, DEElement element, boolean inheritanceOnly)
  {
  	return getSuperElementClosure(perspective, inheritanceOnly).contains(element);
  }
  
  @Override
  public DEElement asElement()
  {
    return this;
  }
  
	public boolean isDestructive()
	{		
		// check ourselves first...
		if (isShallowDestructive() || isRawRetired())
			return true;
		// ask each element in the resembles_e hierarchy
		for (DEElement parent : getFilteredResemblance_eClosure(getHomeStratum()))
			if (parent.isShallowDestructive())
				return true;
		return false;
	}
	
	private boolean isShallowDestructive()
	{
		if (shallowDestructive != null)
			return shallowDestructive;
		
		// an element is destructive if it is a replacement,
		// and it subtracts or replaces from the thing it replaces rather than just adds
		if (!isReplacement())
			return false;
		
		Set<String> replaces = getReplaceUuidsOnly();
		
		// look to see if we delete or replace any of the constituents from any redef'ed component
		for (ConstituentTypeEnum constituentType : ConstituentTypeEnum.values())
		{
			Set<String> replDelUuids = new HashSet<String>();
			for (String uuid : getDeltas(constituentType).getDeleteObjects())
				replDelUuids.add(uuid);
			replDelUuids.addAll(replaces);
			
			// look for the topmost from the home stratum
			Set<DEElement> iRedef = getReplaces();
			for (DEElement redef : iRedef)
			{
				// for this constituent type, get all original objects
				Set<DeltaPair> pairs = redef.getDeltas(constituentType).getPairs(getHomeStratum(), DeltaPairTypeEnum.ORIGINAL_OBJECTS_E);
				for (DeltaPair pair : pairs)
					if (replDelUuids.contains(pair.getUuid()))
					{
						shallowDestructive = true;
						return true;
					}
			}
		}
		shallowDestructive = false;
		return false;		
	}

	public static Set<? extends DEElement> extractLowestCommonSubtypes(DEStratum perspective, Set<? extends DEElement> ifaces, boolean inheritanceOnly)
	{
		// optimisation: possibly only 0 or 1 elements
		if (ifaces.size() <= 1)
			return ifaces;
		
		// optimisation: see if one of the current interfaces is ok
		// -- this is far cheaper than looking at the entire sub-interface hierarchy in things like the swing beans example
		for (DEElement candidate : ifaces)
		{
			Set<DEElement> chain = new LinkedHashSet<DEElement>(candidate.getSuperElementClosure(perspective, inheritanceOnly));
			chain.add(candidate);
			if (chain.containsAll(ifaces))
			{
				Set<DEElement> ret = new LinkedHashSet<DEElement>();
				ret.add(candidate);
				return ret;
			}
		}
		
		Set<DEElement> lowest = new LinkedHashSet<DEElement>();
		Set<DEElement> running = new LinkedHashSet<DEElement>(ifaces);
		
		// find the set of interfaces to consider
		Set<DEElement> considered = new LinkedHashSet<DEElement>();
		for (DEElement iface : ifaces)
		{
			considered.add(iface);
			// expensive, consider optimisations or pre-caching as there is a single repository call
			// which can get many interface's direct sub-interfaces in 1 call
			considered.addAll(iface.getSubElementClosure(perspective, inheritanceOnly));
		}
		
		while (running.size() > 0)
		{
			// now, look for the best match over the current interface collection
			int max = 0;
			Set<DEElement> best = new LinkedHashSet<DEElement>();
			for (DEElement consider: considered)
			{
				int count = matchTo(perspective, consider, running, inheritanceOnly);
				if (count >= max)
				{
					if (count > max)
						best.clear();
					best.add(consider);
					max = count;
				}
			}

			// take the best attempt and see if we can refine it
			Set<DEElement> covers = refineBest(perspective, best, running, max, inheritanceOnly);
			lowest.addAll(covers);
			
			// remove the matched interfaces from the set
			removedMatchElements(perspective, covers, running, inheritanceOnly);
		}
		return lowest;
	}

	/** remove any matched interfaces so we don't consider them again */
	private static void removedMatchElements(DEStratum perspective, Set<DEElement> covers, Set<DEElement> running, boolean inheritanceOnly)
	{
		for (DEElement cover : covers)
		{
			running.remove(cover);
			running.removeAll(cover.getSuperElementClosure(perspective, inheritanceOnly));
		}
	}

	/** refine the best choice to ensure we have the interface least far down the inheritance tree */
	private static Set<DEElement> refineBest(DEStratum perspective, Set<DEElement> best, Set<DEElement> running, int match, boolean inheritanceOnly)
	{
		Set<DEElement> possible = new HashSet<DEElement>();
		for (DEElement iface : best)
		{
			possible.add(iface);
			possible.addAll(iface.getSuperElementClosure(perspective, inheritanceOnly));
		}
		
		Set<DEElement> matched = new HashSet<DEElement>();
		for (DEElement tryIt: possible)
			if (matchTo(perspective, tryIt, running, inheritanceOnly) == match)
				matched.add(tryIt);
		
		for (DEElement refine : new HashSet<DEElement>(matched))
			matched.removeAll(refine.getSubElementClosure(perspective, inheritanceOnly));
		
		// if matched is not set, take all -- this happens occasionally when cycles occur in the resemblance graph
		if (matched.size() == 0)
			matched.addAll(best);
		
		return matched;	
	}

	private static int matchTo(DEStratum perspective, DEElement consider, Set<DEElement> ifaces, boolean inheritanceOnly)
	{
		int count = 0;
		for (DEElement iface: ifaces)
		{
			if (consider == iface || consider.getSuperElementClosure(perspective, inheritanceOnly).contains(iface))
				count++;
		}
		return count;
	}

	public static Set<DEElement> extractHighestCommonSupertypes(DEStratum perspective, Set<DEInterface> elements, boolean inheritanceOnly)
	{
		Set<DEElement> highest = new HashSet<DEElement>();
		for (DEElement element : elements)
		{
			Set<DEElement> supers = element.getSuperElementClosure(perspective, inheritanceOnly);
			boolean consider = true;
			for (DEInterface iter : elements)
			{
				// if we find something which includes this has as a supertype, don't bother with this
				if (iter != element && supers.contains(iter))
				{
					consider = false;
					break;
				}
			}
			
			if (consider)
				highest.add(element);
		}
		return highest;
	}

	public Set<DEElement> getReplacesOrSelf()
	{
		if (isReplacement())
			return getReplaces();
		Set<DEElement> self = new HashSet<DEElement>();
		self.add(this);
		return self;
	}
	
	public boolean isExpanded(DEStratum perspective)
	{
		if (expanded == null)
			return false;
		return expanded.contains(perspective);
	}
	
	public void setExpanded(DEStratum perspective)
	{
		if (expanded == null)
			expanded = new HashSet<DEStratum>();
		expanded.add(perspective);
	}

	public DEAppliedStereotype getAppliedStereotype(DEStratum perspective)
	{
		// note: we can have more than 1, but this is an error that will be picked up by the element rules
		//       return the first stereotype we find
		List<DEAppliedStereotype> stereos = getAppliedStereotypes(perspective);
		return stereos.isEmpty() ? null : stereos.get(0);
	}
	
	public List<DEAppliedStereotype> getAppliedStereotypes()
	{
		return getAppliedStereotypes(getHomeStratum());
	}
	
	public DEAppliedStereotype getRawAppliedStereotype()
	{
		return getAppliedStereotype(null);
	}

	public List<DEAppliedStereotype> getRawAppliedStereotypes()
	{
		return getAppliedStereotypes(null);
	}
	
	public List<DEAppliedStereotype> getAppliedStereotypes(DEStratum perspective)
	{
		// note: we can have more than 1, but this is an error that will be picked up by the element rules
		//       return the first stereotype we find
		Set<DeltaPair> stereos =
			perspective == null ? getDeltas(ConstituentTypeEnum.DELTA_APPLIED_STEREOTYPE).getReplaceObjects() :
														getDeltas(ConstituentTypeEnum.DELTA_APPLIED_STEREOTYPE).getConstituents(perspective);
		List<DEAppliedStereotype> applied = new ArrayList<DEAppliedStereotype>();
		for (DeltaPair pair : stereos)
			applied.add(pair.getConstituent().asAppliedStereotype());
		return applied;
	}
	
	public String getImplementationClass(DEStratum perspective)
	{
		String impl = getForcedImplementationClass(perspective);
		return impl == null ? getAutoImplementationClass(perspective, null) : impl; 
	}
	
	public boolean hasForcedImplementationClass(DEStratum perspective)
	{
		return getForcedImplementationClass(perspective) != null;
	}
	
	public String getAutoImplementationClass(DEStratum perspective, boolean multiple[])
	{
		perspective = perspective == null ? getHomeStratum() : perspective;
		List<DEElement> tops = getTopmost(perspective);
		
		// if it is just us...
		if (tops.size() == 1 && tops.get(0) == this)
		{
			perspective = getHomeStratum();
			String pkg = perspective.getJavaPackage();
			String name = getName(perspective);
			// possibly remove the prime if this is an evolution
			if (name.endsWith("`"))
				name = name.substring(0, name.length() - 1);
			if (asInterface() != null && asInterface().isLegacyBean(perspective) && name.startsWith("I"))
				name = name.substring(1);
			if (pkg.length() == 0)
				return name;
			return pkg + "." + name;
		}

		// otherwise we need to amalgamate
		String impl = "";
		Set<String> already = new HashSet<String>();
		boolean first = true;
		for (DEElement top : tops)
		{
			String next = top.getAutoImplementationClass(null, null);			
			if (!already.contains(next))
			{
				if (!first)
				{
					impl += " versus ";
					if (multiple != null)
						multiple[0] = true;
				}
				impl += next;
				already.add(next);
			}
			first = false;
		}
		return impl;
	}
	
	public String getForcedImplementationClass(DEStratum perspective)
	{
		DEAppliedStereotype stereo = getAppliedStereotype(perspective);
		if (stereo == null)
		{
		  // special cases for bootstrapping
		  if (getUuid().equals("String"))
		    return "java.lang.String";
		  if (getUuid().equals("boolean"))
		  	return "java.lang.Boolean";
			return null;
		}
		String prop = stereo.getStringProperty(IMPLEMENTATION_STEREOTYPE_PROPERTY).trim();
		if (prop.length() == 0)
			return null;
		return prop;
	}

	/**
	 * we want the implementation class that we should inherit from
	 */
	public Set<String> getImplementationInheritances(DEStratum perspective)
	{
		Set<String> inherits = new LinkedHashSet<String>();

		// look for the previous implementation classes we have inherited
		Set<DEElement> resembles_e = getResembles_e(perspective);
		for (DEElement elem : resembles_e)
		{
			String inherit = elem.getImplementationClass(elem.getHomeStratum());
			if (inherit.length() != 0)
				inherits.add(inherit);			
		}
		return inherits;
	}
}
