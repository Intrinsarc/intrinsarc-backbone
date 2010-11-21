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

public class E
{
// start generated code
// attributes
	private int ageX;
// required ports
	private test.IFace out;
	private com.intrinsarc.backbone.runtime.api.ICreate create;
// provided ports
	private IRunPortImpl port_IRunProvided = new IRunPortImpl();
	private IFaceInImpl in_IFaceProvided = new IFaceInImpl();
// setters and getters
	public int getAgeX() { return ageX; }
	public void setAgeX(int ageX) { this.ageX = ageX;}
	public void setOut_IFace(test.IFace out) { this.out = out; }
	public void setCreate_ICreate(com.intrinsarc.backbone.runtime.api.ICreate create) { this.create = create; }
	public com.intrinsarc.backbone.runtime.api.IRun getPort_IRun(Class<?> required) { return port_IRunProvided; }
	public test.IFace getIn_IFace(Class<?> required) { return in_IFaceProvided; }
// end generated code

	private class IFaceInImpl implements test.IFace
	{

		public void test()
		{
			System.out.println("$$ back to source!");
		}
	}

	private class IRunPortImpl implements IRun
	{

		public boolean run(String[] args)
		{
			System.out.println("$$ running, about to create: ageX = " + ageX);
			create.create(null);
			out.test();
			return true;
		}
	}
}
