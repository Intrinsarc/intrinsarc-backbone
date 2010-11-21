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

public class A
{
// start generated code
// attributes
	private int age;
// required ports
// provided ports
	private IRunPortImpl port_IRunProvided = new IRunPortImpl();
// setters and getters
	public int getAge() { return age; }
	public void setAge(int age) { this.age = age;}
	public com.intrinsarc.backbone.runtime.api.IRun getPort_IRun(Class<?> required) { return port_IRunProvided; }
// end generated code


	private class IRunPortImpl implements IRun
	{
		public boolean run(String[] args)
		{
			System.out.println("$$ ran correctly, age = " + age);
			return true;
		}
	}

}
