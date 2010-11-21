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
package com.intrinsarc.backbonetests.logic;

import org.junit.*;

import com.intrinsarc.deltaengine.base.*;

public class StrataTests extends TestBase
{
	@Test
	public void testTransitivity()
	{
		testPackages("Transitivity of a", a.getTransitive(), new DEStratum[]{aa, ab, aba, global});
		testPackages("Transitivity of b", b.getTransitive(), new DEStratum[]{a, aa, ab, aba, global});
		testPackages("Transitivity of c", c.getTransitive(), new DEStratum[]{a, aa, ab, aba, global});
		testPackages("Transitivity of d", d.getTransitive(), new DEStratum[]{a, aa, ab, aba, c, b, global});
		testPackages("Transitivity of e", e.getTransitive(), new DEStratum[]{a, aa, ab, aba, b, global});
		testPackages("Transitivity of lonely", lonely.getTransitive(), new DEStratum[]{});		
		testPackages("Transitivity of top", top.getTransitive(), new DEStratum[]{a, aa, ab, aba, lonely, b, c, d, e, global});
	}
	
	@Test
	public void testTransitivePlusMe()
	{
		testPackages("TransitivePlusMe of d", d.getTransitivePlusMe(), new DEStratum[]{a, aa, ab, aba, c, b, d, global});		
	}
	
	/**
	 * a should expose ab and not aa
	 */
	@Test
	public void testExposingOfNested()
	{
		testPackages("Exposes of a", a.getExposesStrata(), new DEStratum[]{a, ab, aba});
	}

	@Test
	public void testChildSeesParentDependencies()
	{
		testPackages("Dependencies of aa", aa.getSimpleDependsOn(), new DEStratum[]{global});
		testPackages("Dependencies of ab", ab.getSimpleDependsOn(), new DEStratum[]{aba});
		testPackages("Dependencies of ab", aba.getSimpleDependsOn(), new DEStratum[]{global});
	}
	
	@Test
	public void testCanSee()
	{
		testPackages("Can see for c", c.getCanSee(), new DEStratum[]{a, ab, aba});
		testPackages("Can see for d", d.getCanSee(), new DEStratum[]{a, ab, aba, b, c});
	}
	
	@Test
	public void textRelaxation()
	{
		testPackages("Exposes of b", b.getExposesStrata(), new DEStratum[]{b});
		testPackages("Exposes of c", c.getExposesStrata(), new DEStratum[]{c, a, ab, aba});
	}
}
