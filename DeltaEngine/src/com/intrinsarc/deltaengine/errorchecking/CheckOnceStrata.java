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

public class CheckOnceStrata
{
	private static Set<Object> checkedOk = new HashSet<Object>();
	
	public static void clear()
	{
		checkedOk.clear();
	}
	
	public static boolean isOmitCheck(DEStratum s)
	{
		if (isCheckOkStratum(s) && checkedOk.contains(s.getRepositoryObject()))
			return true;
		// remove from the check list as we are no longer "check once"
		checkedOk.remove(s.getRepositoryObject());
		return false;
	}
	
	public static void isNowWriteable(Package pkg)
	{
		checkedOk.remove(pkg);
	}
	
	public static void possiblySetOmitCheck(DEStratum s)
	{
		if (isCheckOkStratum(s))
			checkedOk.add(s.getRepositoryObject());
	}
	
	public static boolean isCheckOkStratum(DEStratum s)
	{
		return s.isCheckOnceIfReadOnly() && s.isReadOnly();
	}
}
