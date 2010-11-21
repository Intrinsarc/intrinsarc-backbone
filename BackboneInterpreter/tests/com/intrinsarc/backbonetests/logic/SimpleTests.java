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

import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parserbase.*;
import com.intrinsarc.deltaengine.base.*;

public class SimpleTests extends TestBase
{
	@Test
	public void resemblanceWithOneAttribute()
	{
		// create a component with a composite and an attribute, and resemble it to see if we pick up the attributes
		BBComponent base = new BBComponent(new UuidReference("base"));
		top.settable_getElements().add(base);
		BBAttribute attr = new BBAttribute(new UuidReference("attr")); 
		base.settable_getAddedAttributes().add(attr);
		BBComponent extension = new BBComponent(new UuidReference("extension"));
		extension.settable_getRawResembles().addObject(base);
		
		// test that the attributes are present
		testConstituents("Testing base attribute", base, ConstituentTypeEnum.DELTA_ATTRIBUTE, top, new DEConstituent[]{attr}, null);
		testConstituents("Testing extension attribute", extension, ConstituentTypeEnum.DELTA_ATTRIBUTE, top, new DEConstituent[]{attr}, null);
	}
	
	@Test
	public void resemblanceWithDeletedAttribute()
	{
		// create a component with a composite and an attribute, and resemble it to see if we pick up the attributes
		BBComponent base = new BBComponent(new UuidReference("base"));
		top.settable_getElements().add(base);
		BBAttribute attr = new BBAttribute(new UuidReference("attr")); 
		base.settable_getAddedAttributes().add(attr);
		BBAttribute attr2 = new BBAttribute(new UuidReference("attr2")); 
		base.settable_getAddedAttributes().add(attr2);
		BBComponent extension = new BBComponent(new UuidReference("extension"));
		extension.settable_getRawResembles().addObject(base);
		extension.settable_getDeletedAttributes().add(attr.getUuid());
		
		// test that the attributes are present
		testConstituents("Testing base attribute", base, ConstituentTypeEnum.DELTA_ATTRIBUTE, top, new DEConstituent[]{attr, attr2}, null);
		testConstituents("Testing extension attribute", extension, ConstituentTypeEnum.DELTA_ATTRIBUTE, top, new DEConstituent[]{attr2}, null);
	}

	@Test
	public void resemblanceWithReplacedAttribute()
	{
		// create a component with a composite and an attribute, and resemble it to see if we pick up the attributes
		BBComponent base = new BBComponent(new UuidReference("base"));
		top.settable_getElements().add(base);
		BBAttribute attr = new BBAttribute(new UuidReference("attr")); 
		base.settable_getAddedAttributes().add(attr);
		BBAttribute attr2 = new BBAttribute(new UuidReference("attr2")); 
		base.settable_getAddedAttributes().add(attr2);
		BBComponent extension = new BBComponent(new UuidReference("extension"));
		extension.settable_getRawResembles().addObject(base);
		BBAttribute repAttr = new BBAttribute(new UuidReference("repAttr"));
		BBReplacedAttribute replaced = new BBReplacedAttribute(new UuidReference(attr.getUuid()), repAttr);
		extension.settable_getReplacedAttributes().add(replaced);
		
		// test that the attributes are present
		testConstituents("Testing base attribute", base, ConstituentTypeEnum.DELTA_ATTRIBUTE, top, new DEConstituent[]{attr, attr2}, null);
		testConstituents("Testing extension attribute", extension, ConstituentTypeEnum.DELTA_ATTRIBUTE, top, new DEConstituent[]{attr2}, new BBReplacedConstituent[]{replaced});
	}
}
