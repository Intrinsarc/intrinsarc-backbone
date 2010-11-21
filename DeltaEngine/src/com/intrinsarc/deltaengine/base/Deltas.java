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

import static com.intrinsarc.deltaengine.base.DeltaIdTypeEnum.*;
import static com.intrinsarc.deltaengine.base.DeltaPairTypeEnum.*;

import java.util.*;

/**
 * the Deltas sig in the Alloy model.  Allows the expanded object to be determined.
 * @author andrew
 */

public class Deltas implements IDeltas
{
  /** the element in question */
  private DEElement element;

  /** the inputs: adds, deletes and replacements */
  private Set<DeltaPair> addObjects;
  private Set<String> deleteObjects;
  private Set<DeltaPair> replaceObjects;
  
  /** the type of constituent: port, part, connector, attribute */
  private ConstituentTypeEnum constituentType;
  
  /** the expanded constituents --> the results are stored in a map for easy processing */
  private Map<DEStratum, DeltaPairs> pairs = new LinkedHashMap<DEStratum, DeltaPairs>();
  private Map<DEStratum, DeltaIds> ids = new LinkedHashMap<DEStratum, DeltaIds>();
  
  public Deltas(
      DEElement element,
      Set<DeltaPair> addObjects,
      Set<String> deleteObjects,
      Set<DeltaPair> replaceObjects,
      ConstituentTypeEnum constituentType)
  {
    this.element = element;
    this.addObjects = addObjects;
    this.deleteObjects = deleteObjects;
    this.replaceObjects = replaceObjects;
    this.constituentType = constituentType;
  }
  
  /* (non-Javadoc)
	 * @see com.intrinsarc.deltaengine.base.IDeltas#isWellFormed(com.intrinsarc.deltaengine.base.DEPackage)
	 */
  public Set<ErrorDescription> isWellFormed(DEStratum perspective)
  {
  	// expand before checking well-formedness
  	GlobalDeltaEngine.engine.expandForStereotypesAndFactories(perspective, element);
  	
    Set<ErrorDescription> errors = new LinkedHashSet<ErrorDescription>();
    
    // no overlap between deleted and replaced ids
    Set<String> overlap = new LinkedHashSet<String>();
    for (String uuid : deleteObjects)
    	overlap.add(uuid);
    Set<String> replaceIds = extractIdsFromPairs(replaceObjects);
    overlap.retainAll(replaceIds);
    if (!overlap.isEmpty())
      errors.add(ErrorCatalog.NO_DELTA_DELETE_REPLACE_OVERLAP);
    
    // delete and replace ids must be present in the underlying objects
    Set<String> originalIds = extractIdsFromPairs(getPairs(perspective, ORIGINAL_OLD_OBJECTS_E));
    if (!originalIds.containsAll(deleteObjects))
      errors.add(ErrorCatalog.DELETE_MATCHES_NO_CONSTITUENT);
    if (!originalIds.containsAll(replaceIds))
      errors.add(ErrorCatalog.REPLACE_MATCHES_NO_CONSTITUENT);
    
    return errors;
  }
  
  /* (non-Javadoc)
	 * @see com.intrinsarc.deltaengine.base.IDeltas#getConstituents(com.intrinsarc.deltaengine.base.DEPackage)
	 */
  public Set<DeltaPair> getConstituents(DEStratum perspective)
  {
    return getPairs(perspective, OBJECTS_E);
  }

	/* (non-Javadoc)
	 * @see com.intrinsarc.deltaengine.base.IDeltas#getConstituents(com.intrinsarc.deltaengine.base.DEPackage, boolean)
	 */
	public Set<DeltaPair> getConstituents(DEStratum perspective, boolean omitSynthetics)
  {
  	if (!omitSynthetics)
  		return getPairs(perspective, OBJECTS_E);
  	else
  	{
  		Set<DeltaPair> real = new LinkedHashSet<DeltaPair>();
  		for (DeltaPair r : getPairs(perspective, OBJECTS_E))
  			if (!r.getConstituent().isSynthetic())
  				real.add(r);
  		return real;
  	}  	
  }

  public Set<String> getIds(DEStratum perspective, DeltaIdTypeEnum toGet)
  {
    if (toGet == null)
      return new LinkedHashSet<String>();
    
  	// expand before checking well-formedness
  	GlobalDeltaEngine.engine.expandForStereotypesAndFactories(perspective, element);
  	
    if (toGet == DELETE_OBJECTS)
    {
    	Set<String> deleted = new LinkedHashSet<String>();
    	for (String uuid : deleteObjects)
    		deleted.add(uuid);
    	return deleted;
    }
    
    // if cached, return immediately
    Set<String> cached = getDeltaIds(perspective, toGet);
    if (cached != null)
      return cached;
    
    switch (toGet)
    {
      case DELETED_OBJECTS_E:
        cached = determineIds(
            perspective,
            element.getTopmost(perspective),
            ORIGINAL_DELETED_OBJECTS_E,
            ORIGINAL_REPLACED_OBJECTS_E,
            null);
        break;
      case ORIGINAL_DELETED_OBJECTS_E:
        cached = determineIds(
            perspective,
            element.getFilteredResembles_e(perspective), 
            ORIGINAL_DELETED_OBJECTS_E,
            ORIGINAL_REPLACED_OBJECTS_E,
            DELETE_OBJECTS);
        break;
    }
    
    // store in the map
    getDeltaIds(perspective, toGet);
    ids.get(perspective).put(toGet, cached);
    
    return cached;
  }
  
  /* (non-Javadoc)
	 * @see com.intrinsarc.deltaengine.base.IDeltas#getPairs(com.intrinsarc.deltaengine.base.DEPackage, com.intrinsarc.deltaengine.base.DeltaPairTypeEnum)
	 */
  public Set<DeltaPair> getPairs(DEStratum perspective, DeltaPairTypeEnum toGet)
  {
  	if (toGet == null)
      return new LinkedHashSet<DeltaPair>();
  	
  	// expand before checking well-formedness
  	GlobalDeltaEngine.engine.expandForStereotypesAndFactories(perspective, element);

    if (toGet == ADD_OBJECTS)
      return addObjects;
    if (toGet == REPLACE_OBJECTS)
      return replaceObjects;
    
    // if cached, return immediately
    Set<DeltaPair> cached = getDeltaPairs(perspective, toGet);
    // this line was the problem before when we didn't hold a separate cachedReplaceObjects cache...
    if (cached != null)
      return cached;
    
    switch (toGet)
    {
      case OLD_OBJECTS_E:
        List<DEElement> topmost = element.getTopmost(perspective);
      	if (topmost.size() == 1 && topmost.get(0) == element)
        {
        		cached = getPairs(perspective, ORIGINAL_OLD_OBJECTS_E);
        }
        else
          cached = determinePairs(
              perspective,
              topmost,
              ORIGINAL_OBJECTS_E,
              ORIGINAL_DELETED_OBJECTS_E,
              false,
              ORIGINAL_REPLACED_OBJECTS_E,
              false,
              null,
              null,
              false);
        break;
      case ORIGINAL_OLD_OBJECTS_E:
        cached = determinePairs(
            perspective,
            element.getFilteredResembles_e(perspective),
            ORIGINAL_OBJECTS_E,
            ORIGINAL_DELETED_OBJECTS_E,
            false,
            ORIGINAL_REPLACED_OBJECTS_E,
            false,
            null,
            null,
            false);
        break;
      case OBJECTS_E:
        cached = determinePairs(
            perspective,
            element.getTopmost(perspective),
            ORIGINAL_OBJECTS_E,
            DELETED_OBJECTS_E,
            true,
            REPLACED_OBJECTS_E,
            true,
            null,
            null,
            false);
        break;
      case REPLACED_OBJECTS_E:
        cached = determinePairs(
            perspective,
            element.getTopmost(perspective),
            ORIGINAL_REPLACED_OBJECTS_E,
            null,
            false,
            null,
            false,
            null,
            null,
            false);
        break;
      case ORIGINAL_OBJECTS_E:
        cached = determinePairs(
	          perspective,
	          element.getFilteredResembles_e(perspective),
	          ORIGINAL_OBJECTS_E,
	          ORIGINAL_DELETED_OBJECTS_E,
	          true,
	          ORIGINAL_REPLACED_OBJECTS_E,
	          true,
	          ADD_OBJECTS,
	          REPLACE_OBJECTS,
	          true);
        break;
      default:
      case ORIGINAL_REPLACED_OBJECTS_E:
        cached = determinePairs(
            perspective,
            element.getFilteredResembles_e(perspective),
            ORIGINAL_REPLACED_OBJECTS_E,
            DELETE_OBJECTS,
            true,
            REPLACE_OBJECTS,
            true,
            null,
            null,
            false);
        break;
    }
    
    // store in the map
    getDeltaPairs(perspective, toGet);
    pairs.get(perspective).put(toGet, cached);
    
    return cached;
  }

	private Set<String> determineIds(
      DEStratum perspective,
      Collection<? extends DEElement> resembles_e,
      DeltaIdTypeEnum original,
      DeltaPairTypeEnum delete,
      DeltaIdTypeEnum add)
  {
    Set<String> originalIds = collectIds(perspective, resembles_e, original);
    Set<DeltaPair> deletePairs = collectPairs(perspective, resembles_e, delete);
    Set<String> addPairs = getIds(perspective, add);
    
    // remove any ids that have been deleted
    originalIds.removeAll(extractIdsFromPairs(deletePairs));
    
    // add any new pairs
    originalIds.addAll(addPairs);
    return originalIds;
  }
  
  private Set<DeltaPair> determinePairs(
      final DEStratum perspective,
      final Collection<DEElement> resembles_e,
      final DeltaPairTypeEnum original,
      final DeltaIdTypeEnum delete,
      boolean deleteFromThis,
      final DeltaPairTypeEnum replace,
      boolean replaceFromThis,
      final DeltaPairTypeEnum add,
      final DeltaPairTypeEnum replace2,
      boolean setOriginal)
  {
    Set<DeltaPair> originalPairs = collectPairs(perspective, resembles_e, original);
    Set<DeltaPair> replacePairs =
      replaceFromThis ? getPairs(perspective, replace) : collectPairs(perspective, resembles_e, replace);
    Set<DeltaPair> addPairs = getPairs(perspective, add);
    Set<DeltaPair> replace2Pairs = getPairs(perspective, replace2);
    
    // remove any deleted pairs by uuid
    Set<String> deleteIds =
      deleteFromThis ? getIds(perspective, delete) : collectIds(perspective, resembles_e, delete);
    for (Iterator<DeltaPair> iter = originalPairs.iterator(); iter.hasNext();)
    {
      DeltaPair pair = iter.next();
      if (deleteIds.contains(pair.getUuid()))
        iter.remove();
    }
    
    if (setOriginal)
    {
      	// work out what the replaceObjects replaced in their home stratum --> this is the original
    // -- we can do this here because the original of a replace is always the first thing replaced...
  	for (DEElement r : element.getFilteredResembles_e(element.getHomeStratum()))
  		for (DeltaPair originalPair : r.getDeltas(constituentType).getPairs(r.getHomeStratum(), DeltaPairTypeEnum.OBJECTS_E))
      {
        // find the constituent being replaced
        for (DeltaPair pair : replaceObjects)
          if (originalPair.getUuid().equals(pair.getUuid()))
          	pair.setOriginal(originalPair.getOriginal());
      }
    }
    
    // first replace
    removePairsById(originalPairs, replacePairs);
    originalPairs.addAll(replacePairs);
    
    // then add
    originalPairs.addAll(addPairs);
    
    // second replace
    removePairsById(originalPairs, replace2Pairs);
    originalPairs.addAll(replace2Pairs);

    return originalPairs;
  }

    
  private void removePairsById(Set<DeltaPair> toRemove, Set<DeltaPair> pairs)
  {
    Set<String> ids = extractIdsFromPairs(pairs);
    for (Iterator<DeltaPair> iter = toRemove.iterator(); iter.hasNext();)
    {
      DeltaPair pair = iter.next();
      if (ids.contains(pair.getUuid()))
          iter.remove();
    }
  }

  private Set<String> extractIdsFromPairs(Set<DeltaPair> pairs)
  {
    Set<String> ids = new LinkedHashSet<String>();
    for (DeltaPair pair : pairs)
      ids.add(pair.getUuid());
    return ids;
  }

  private Set<String> collectIds(DEStratum perspective, Collection<? extends DEElement> resembles, DeltaIdTypeEnum idType)
  {
    if (idType == null)
      return new LinkedHashSet<String>();
    
    Set<String> ids = new LinkedHashSet<String>();
    for (DEElement r : resembles)
      ids.addAll(r.getDeltas(constituentType).getIds(perspective, idType));
    return ids;
  }

  private Set<DeltaPair> collectPairs(DEStratum perspective, Collection<? extends DEElement> resembles, DeltaPairTypeEnum pairType)
  {
    if (pairType == null)
      return new LinkedHashSet<DeltaPair>();
    
    Set<DeltaPair> pairs = new LinkedHashSet<DeltaPair>();
    for (DEElement r : resembles)
      pairs.addAll(r.getDeltas(constituentType).getPairs(perspective, pairType));
    return pairs;
  }
  
  private Set<DeltaPair> getDeltaPairs(DEStratum perspective, DeltaPairTypeEnum type)
  {
    // lazily create pairs
    DeltaPairs value = pairs.get(perspective);
    if (value == null)
    {
      value = new DeltaPairs();
      pairs.put(perspective, value);
    }
    return value.get(type);
  }
  
  private Set<String> getDeltaIds(DEStratum perspective, DeltaIdTypeEnum type)
  {
    // lazily create ids
    DeltaIds value = ids.get(perspective);
    if (value == null)
    {
      value = new DeltaIds();
      ids.put(perspective, value);
    }
    return value.get(type);
  }

	/* (non-Javadoc)
	 * @see com.intrinsarc.deltaengine.base.IDeltas#getAddObjects()
	 */
	public Set<DeltaPair> getAddObjects()
	{
		return addObjects;
	}

	/* (non-Javadoc)
	 * @see com.intrinsarc.deltaengine.base.IDeltas#getDeleteObjects()
	 */
	public Set<String> getDeleteObjects()
	{
		return deleteObjects;
	}

	/* (non-Javadoc)
	 * @see com.intrinsarc.deltaengine.base.IDeltas#getReplaceObjects()
	 */
	public Set<DeltaPair> getReplaceObjects()
	{
		return replaceObjects;
	}
}
