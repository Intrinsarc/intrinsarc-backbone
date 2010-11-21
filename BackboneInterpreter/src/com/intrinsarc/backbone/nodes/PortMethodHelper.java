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
package com.intrinsarc.backbone.nodes;

import java.lang.reflect.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.simple.*;
import com.intrinsarc.deltaengine.base.*;

/**
 * encapsulates all of the logic about how ports and provided & required interfaces turn into method names
 * @author andrew
 */
public class PortMethodHelper
{
	private BBSimplePort port;
  private DEComponent partType;
  private String ifaceSimpleClassName;
  private String partTypeClassName;
  private String portName;
  private boolean many;

  private Class<?> partTypeClass; // only needed if methods are actually resolved
  private Class<?> ifaceClass;    // only needed if methods are actually resolved
  
  /** so we don't do it many times over */
  private boolean haveGetNames;
  private boolean haveGetMethods;
  private boolean haveSetNames;
  private boolean haveSetMethods;
  
  /** for things that resolve to methods */
  private Method getSingle;
  private Method getIndexed;
  private Method getSingleSimple;
  private Method getIndexedSimple;
  private Method setSingle;
  private Method addNoIndexed;
  private Method addIndexed;
  private Method removeMany;
  
  /** for names of methods */
  private String getSingleName;
  private String getIndexedName;
  private String setSingleName;
  private String addNoIndexedName;
  private String addIndexedName;
  private String removeManyName;
	
	public PortMethodHelper(DEStratum perspective, DEComponent partType, DEPort cport, DEInterface iface)
	{
		this.partType = partType;
    this.ifaceSimpleClassName = simplify(iface.getImplementationClass(perspective));
		port = new BBSimplePort(new BBSimpleElementRegistry(perspective, null), partType, cport, null);
		partTypeClassName = partType.getImplementationClass(perspective);
    portName = port.getRawName();
    many = port.isIndexed();
	}
	
	public PortMethodHelper(DEStratum perspective, BBSimplePort port, Class<?> ifaceClass)
	{
		this.port = port;
    this.ifaceClass = ifaceClass;
    this.ifaceSimpleClassName = simplify(ifaceClass.getSimpleName());
		partType = port.getOwner().getComplex().asComponent();
    partTypeClass = port.getOwner().getImplementationClass();
		partTypeClassName = partType.getImplementationClass(perspective);
    portName = port.getRawName();
    many = port.isIndexed();
	}
  
	public PortMethodHelper(DEStratum perspective, BBSimplePort port, String ifaceClassName)
	{
		this.port = port;
    this.ifaceSimpleClassName = simplify(ifaceClassName);
		partType = port.getOwner().getComplex().asComponent();
    partTypeClass = port.getOwner().getImplementationClass();
		partTypeClassName = partType.getImplementationClass(perspective);
    portName = port.getRawName();
    many = port.isIndexed();
	}
	
	private String simplify(String clsName)
	{
		int index = clsName.lastIndexOf('.');
		if (index == -1)
			return clsName;
		return clsName.substring(index + 1);
	}
  
  public void resolveGetMethodNames()
  {
  	if (haveGetNames)
  		return;
  	
    // look for a method if this is a bean
    boolean cp = port.isComplexProvided();
    String extra = cp ? "_" + ifaceSimpleClassName : "";

    if (!many)
    {
    	getSingleName = "get" + upperFirst(portName) + extra + "_Provided";
    }
    else
    {
    	getIndexedName = "get" + makeSingular(upperFirst(portName)) + extra + "_Provided";
      removeManyName = "remove" + (port.isBeanNoName() ? "" : makeSingular(upperFirst(portName)));
    }

    haveGetNames = true;
  }

  public void resolveGetMethods() throws BBImplementationInstantiationException
  {
  	if (haveGetMethods)
  		return;
  	resolveGetMethodNames();
  	
    // main is just the object itself
    if (port.isBeanMain())
      return;
    
    String tried[] = {""};

    if (!many)
    {
    	if (port.isWantsRequiredWhenProviding())
	    	getSingle = resolveMethod(
	    			getSingleName,
	    			new Class<?>[]{Class.class},
	    			tried);
    	else
	    	getSingleSimple = resolveMethod(
	    			getSingleName,
	    			new Class<?>[]{},
	    			tried);
    }
    else
    {
    	if (port.isWantsRequiredWhenProviding())
	      getIndexed = resolveMethod(
	          getIndexedName,
	          new Class<?>[]{Class.class, int.class},
	          tried);
    	else
	      getIndexedSimple = resolveMethod(
	          getIndexedName,
	          new Class<?>[]{int.class},
	          tried);
      removeMany = resolveMethod(
          removeManyName,
          new Class<?>[]{ifaceClass},
          tried);
    }

    if (getSingle == null && getIndexed == null && getSingleSimple == null && getIndexedSimple == null)
      throw new BBImplementationInstantiationException("Cannot find method for getting port " + portName + " of " + partTypeClassName + ", tried " + tried[0], partType);
    haveGetMethods = true;
  }

	public void resolveSetMethodNames()
  {
		if (haveSetNames)
			return;
		
  	if (!many)
  	{
  		setSingleName = "set" + upperFirst(portName);
  	}
  	else
  	{
  		addIndexedName = "set" + makeSingular(upperFirst(portName));
      addNoIndexedName = "add" + (port.isBeanNoName() ? "" : makeSingular(upperFirst(portName)));
      removeManyName = "remove" + (port.isBeanNoName() ? "" : makeSingular(upperFirst(portName)));
  	}
  	haveSetNames = true;
  }

	public void resolveSetMethods() throws BBImplementationInstantiationException
  {
		if (haveSetMethods)
			return;
		resolveSetMethodNames();
		
  	String tried[] = {""};
  	if (!many)
  	{
  		setSingle = resolveMethod(
          setSingleName,
          new Class<?>[]{ifaceClass},
          tried);
  	}
  	else
  	{
      addIndexed = resolveMethod(
          addIndexedName,
          new Class<?>[]{ifaceClass, int.class},
          tried);
      addNoIndexed = resolveMethod(
          addNoIndexedName,
          new Class<?>[]{ifaceClass},
          tried);
      removeMany = resolveMethod(
          removeManyName,
          new Class<?>[]{ifaceClass},
          tried);
  	}
 
    if (setSingle == null && addNoIndexed == null && addIndexed == null)
      throw new BBImplementationInstantiationException("Cannot find method for setting port " + portName + " of " + partTypeClassName + ", tried " + tried[0], partType);
    haveSetMethods = true;
  }

  private String makeSingular(String name)
	{
  	if (name.endsWith("s") && name.length() > 1)
  		return name.substring(0, name.length() - 1);
  	return name;
	}

  private Method resolveMethod(String name, Class<?>[] paramClasses, String[] tried) throws BBImplementationInstantiationException
  {
    try
    {
      return partTypeClass.getMethod(name, paramClasses);
    }
    catch (SecurityException e)
    {
      throw new BBImplementationInstantiationException("Security error when getting port method " + name + " of " + partTypeClass + " via method " + name + "(...)", partType);
    }
    catch (NoSuchMethodException e)
    {
    	if (tried[0].length() > 0)
    		tried[0] += " / ";
    	tried[0] += name + "(" + stringify(paramClasses) + ")";
    		
      return null;
    }
  }

  private String stringify(Class<?>[] paramClasses)
	{
  	if (paramClasses == null)
  		return "";
  	
  	String params = "";
  	for (Class<?> cls : paramClasses)
  	{
  		if (params.length() > 0)
  			params += ", ";
  		params += cls.getSimpleName();
  	}
  	return params;
	}

	private String upperFirst(String str)
  {
    if (str.length() > 1)
      return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    return str.toUpperCase();
  }
	
	///////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////

	public Method getGetSingle()
	{
		return getSingle;
	}

	public Method getGetIndexed()
	{
		return getIndexed;
	}

	public Method getGetSingleSimple()
	{
		return getSingleSimple;
	}

	public Method getGetIndexedSimple()
	{
		return getIndexedSimple;
	}

	public Method getSetSingle()
	{
		return setSingle;
	}

	public Method getAddNoIndexed()
	{
		return addNoIndexed;
	}

	public Method getAddIndexed()
	{
		return addIndexed;
	}

	public Method getRemoveMany()
	{
		return removeMany;
	}

	public String getGetSingleName()
	{
		return getSingleName;
	}

	public String getGetIndexedName()
	{
		return getIndexedName;
	}

	public String getSetSingleName()
	{
		return setSingleName;
	}

	public String getAddNoIndexedName()
	{
		return addNoIndexedName;
	}

	public String getAddIndexedName()
	{
		return addIndexedName;
	}

	public String getRemoveManyName()
	{
		return removeManyName;
	}

	///////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	
	public boolean isBeanMain()
	{
		return port.isBeanMain();
	}

	public boolean isMany()
	{
		return many;
	}
}
