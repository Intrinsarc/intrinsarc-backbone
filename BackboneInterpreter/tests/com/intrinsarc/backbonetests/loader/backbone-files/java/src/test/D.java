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
package test;

import com.intrinsarc.backbone.runtime.api.*;

public class D
{
// start generated code
// attributes
	private double attr1;
	private String attr2 = "hello";
// required ports
	private test.IFace out;
// provided ports
	private IFaceInImpl in_IFaceProvided = new IFaceInImpl();
// setters and getters
	public double getAttr1() { return attr1; }
	public void setAttr1(double attr1) { this.attr1 = attr1;}
	public String getAttr2() { return attr2; }
	public void setAttr2(String attr2) { this.attr2 = attr2;}
	public void setOut_IFace(test.IFace out) { this.out = out; }
	public test.IFace getIn_IFace(Class<?> required) { return in_IFaceProvided; }
// end generated code


	private class IFaceInImpl implements test.IFace
	{
		public void test()
		{
			System.out.println("$$ inside D instance: attr1 = " + attr1 + ", attr2 = " + attr2);
			out.test();
		}
	}
}
