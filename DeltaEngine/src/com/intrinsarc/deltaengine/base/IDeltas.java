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

public interface IDeltas
{
	Set<ErrorDescription> isWellFormed(DEStratum perspective);
	Set<DeltaPair> getConstituents(DEStratum perspective);
	Set<DeltaPair> getConstituents(DEStratum perspective, boolean omitSynthetics);
	Set<DeltaPair> getPairs(DEStratum perspective, DeltaPairTypeEnum toGet);
	Collection<String> getIds(DEStratum perspective, DeltaIdTypeEnum idType);

	Set<DeltaPair> getAddObjects();
	Set<String> getDeleteObjects();
	Set<DeltaPair> getReplaceObjects();
}
