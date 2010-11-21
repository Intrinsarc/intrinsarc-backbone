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
package com.intrinsarc.backbone.nodes.insides;

import java.lang.reflect.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.simple.*;
import com.intrinsarc.deltaengine.base.*;

public class ReflectivePort
{ 
	private PortMethodHelper helper;
	private BBSimplePort port;
	private String portName;
	private BBSimpleComponent partType;
	private Class<?> partTypeClass;
	private boolean isLegacyBean;
  
  public ReflectivePort(
  		DEStratum perspective,
  		BBSimpleComponent partType,
      BBSimplePort port,
      Class<?> ifaceClass,
      boolean required) throws BBImplementationInstantiationException
  {
  	this.port = port;
    this.portName = port.getName();
    this.partType = partType;
    partTypeClass = partType.getImplementationClass();
    
  	helper = new PortMethodHelper(perspective, port, ifaceClass);
    isLegacyBean = partType.isLegacyBean();
    if (!required)
    	helper.resolveGetMethods();
    else
      helper.resolveSetMethods();
  }

  public Object getSingle(Class<?> required, Object target) throws BBRuntimeException
  {
    // if this is main, return the target
    if (helper.isBeanMain())
      return target;
    
    try
    {
      if (helper.getGetSingle() != null)
        return helper.getGetSingle().invoke(target, required);
      else
      if (helper.getGetSingleSimple() != null)
        return helper.getGetSingleSimple().invoke(target, (Object[]) null);
      else
        throw new BBRuntimeException("No single getXXX(...) method or field for port " + portName + " in class " + partTypeClass, partType);
    }
    catch (IllegalArgumentException e)
    {
      throw new BBRuntimeException("Illegal argument error when getting port " + portName + " of " + partTypeClass, partType);
    }
    catch (IllegalAccessException e)
    {
      throw new BBRuntimeException("Illegal access exception when getting port " + portName + " of " + partTypeClass, partType);
    }
    catch (InvocationTargetException e)
    {
      throw new BBRuntimeException("Invocation target exception when getting port " + portName + " of " + partTypeClass, partType, e.getCause());
    }
  }
  
  
  public Object getIndexed(Class<?> required, Object target, int index) throws BBRuntimeException
  {
    // can't use an index on a bean
    if (isLegacyBean)
      throw new BBRuntimeException("Cannot use an index with a bean getXXX(...) method", port.getOwner());                

    try
    {
    	if (helper.getGetIndexed() != null)
    		return helper.getGetIndexed().invoke(target, required, index);
    	else
    	if (helper.getGetIndexedSimple() != null)
    		return helper.getGetIndexedSimple().invoke(target, index);
    	else
        throw new BBRuntimeException("No indexed getXXX(...) method or field for port " + portName + " in class " + partTypeClass, partType);
		}
    catch (IllegalArgumentException e)
    {
      throw new BBRuntimeException("Illegal argument error when setting port " + portName + " of " + partTypeClass, partType);
    }
    catch (IllegalAccessException e)
    {
      throw new BBRuntimeException("Illegal access exception when setting port " + portName + " of " + partTypeClass, partType);
    }
    catch (InvocationTargetException e)
    {
      throw new BBRuntimeException("Invocation target exception when setting port " + portName + " of " + partTypeClass, partType, e.getCause());
    }
  }

  /**
   * set a single value
   * @param target
   * @param value
   * @throws BBRuntimeException
   */
  public void setSingle(Object target, Object value) throws BBRuntimeException
  {
    try
    {
      if (helper.getSetSingle() != null)
      {
      	helper.getSetSingle().invoke(target, value);
      }
      else
        throw new BBRuntimeException("No single setXXX(...) method or field for port " + portName + " in class " + partTypeClass, partType);
    }
    catch (IllegalArgumentException e)
    {
      throw new BBRuntimeException("Illegal argument error when setting port " + portName + " of " + partTypeClass, partType);
    }
    catch (IllegalAccessException e)
    {
      throw new BBRuntimeException("Illegal access exception when setting port " + portName + " of " + partTypeClass, partType);
    }
    catch (InvocationTargetException e)
    {
      throw new BBRuntimeException("Invocation target exception when setting port " + portName + " of " + partTypeClass, partType, e.getCause());
    }
  }
  
  public void remove(Object target, Object value) throws BBRuntimeException
  {  	
    try
    {
    	if (helper.getRemoveMany() != null)
    		helper.getRemoveMany().invoke(target, value);
      else
      if (helper.getSetSingle() != null)
      	helper.getSetSingle().invoke(target, (Object[])(null));
      else
        throw new BBRuntimeException("No removeXXX(...) method for port " + portName + " in class " + partTypeClass, partType);
    }
    catch (IllegalArgumentException e)
    {
      throw new BBRuntimeException("Illegal argument error when removing from port " + portName + " of " + partTypeClass, partType);
    }
    catch (IllegalAccessException e)
    {
      throw new BBRuntimeException("Illegal access exception when removing from port " + portName + " of " + partTypeClass, partType);
    }
    catch (InvocationTargetException e)
    {
      throw new BBRuntimeException("Invocation target exception when removing from port " + portName + " of " + partTypeClass, partType, e.getCause());
    }
  }
  
  /**
   * add a value to the list at the index provided
   * @param target
   * @param value
   * @throws BBRuntimeException
   */
  public void addIndexed(Object target, Object value, int index) throws BBRuntimeException
  {
    // can't use an index on a bean
    if (isLegacyBean && index != -1)
      throw new BBRuntimeException("Cannot use an index with a bean addXXX(...) method: index = " + index + " of port " + port.getName() + ", when upper bound is " + port.getUpperBound(), port.getOwner());

    try
    {
      if (index != -1 && helper.getAddIndexed() != null)
      	helper.getAddIndexed().invoke(target, value, index);
      else
      if (helper.getAddNoIndexed() != null)
      	helper.getAddNoIndexed().invoke(target, value);
      else
        throw new BBRuntimeException("No addXXX(...) method for port " + portName + " in class " + partTypeClass, partType);
    }
    catch (IllegalArgumentException e)
    {
      throw new BBRuntimeException("Illegal argument error when adding to port " + portName + " of " + partTypeClass, partType);
    }
    catch (IllegalAccessException e)
    {
      throw new BBRuntimeException("Illegal access exception when adding to port " + portName + " of " + partTypeClass, partType);
    }
    catch (InvocationTargetException e)
    {
      throw new BBRuntimeException("Invocation target exception when adding to port " + portName + " of " + partTypeClass, partType, e.getCause());
    }
  }
}
