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
package com.intrinsarc.backbone.nodes.simple;

import java.lang.reflect.*;
import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.simple.internal.*;
import com.intrinsarc.deltaengine.base.*;

public class BBSimpleAttribute extends BBSimpleObject
{	
	private String name;
	private String rawName;
  private BBSimpleElement type;
  private List<BBSimpleParameter> defaultValue;
  private int factory;
  private boolean resolved;
  private DEAttribute complex;
  private Constructor<?> constructor;
  private PositionEnum position;
  private boolean isDefault;
  private String originalName;
  private BBSimpleElement owner;
  private String uuid;
  private boolean readOnly;
  private boolean writeOnly;
	private boolean isNull;
	private BBSimpleAttribute copy;
  	
	public BBSimpleAttribute(BBSimpleElementRegistry registry, DEAttribute complex, List<BBSimpleAttribute> attributes, BBSimpleElement owner)
	{
		rawName = complex.getName();
		originalName = rawName;
		name = registry.makeName(rawName);
		if (!complex.getDefaultValue().isEmpty())
		{
			defaultValue = new ArrayList<BBSimpleParameter>();
			for (DEParameter p : complex.getDefaultValue())
			{
				DEAttribute attr = p.getAttribute(registry.getPerspective(), owner.getComplex());
				if (attr != null)
				{
					BBSimpleAttribute simple = translateAttribute(attributes, attr);
					if (simple != null)
						defaultValue.add(new BBSimpleParameter(simple));
				}
				else
					defaultValue.add(new BBSimpleParameter(p.getLiteral()));
			}
		}
			
		type = registry.retrieve(complex.getType().asElement());
		this.complex = complex;
		position = PositionEnum.TOP;
		this.owner = owner;
		uuid = complex.getUuid();
		readOnly = complex.isReadOnly();
		writeOnly = complex.isWriteOnly();
	}

	private BBSimpleAttribute translateAttribute(List<BBSimpleAttribute> attributes, DEAttribute attribute)
	{
		for (BBSimpleAttribute attr : attributes)
			if (attr.getComplex() == attribute)
				return attr;
		return null;
	}

	public BBSimpleAttribute(BBSimpleElementRegistry registry, BBSimpleAttribute old, int factory, int partFactory, String full, PositionEnum position, Map<BBSimpleAttribute, BBSimpleAttribute> copiedFromFrame)
	{
		rawName = old.rawName;
		originalName = old.originalName;
		name = registry.makeName(full + " :: " + rawName);
		defaultValue = translateDefaultValues(copiedFromFrame, old.defaultValue);
		type = old.type;
		complex = old.complex;
		uuid = complex.getUuid();
		
		this.factory = old.complex.isPullUp() ? factory - 1 : factory;
		this.position = position;
		setFactory(old, factory, partFactory);
		readOnly = old.isReadOnly();
	}

	public BBSimpleAttribute(BBSimpleElementRegistry registry, BBSimpleSlot slot, int factory, int partFactory, String full, PositionEnum position, Map<BBSimpleAttribute, BBSimpleAttribute> copiedFromFrame)
	{
		BBSimpleAttribute old = slot.getAttribute();
		rawName = old.rawName;
		originalName = old.originalName;
		name = registry.makeName(full + " :: " + rawName);
		defaultValue = translateDefaultValues(copiedFromFrame, slot.getValue());
		type = old.type;
		complex = old.complex;
		uuid = old.uuid;
		readOnly = old.readOnly;
		this.copy = slot.getEnvironmentAlias();
		this.position = position;
		setFactory(slot.getAttribute(), factory, partFactory);
	}

	public BBSimpleAttribute(BBSimpleElementRegistry registry, BBSimpleAttribute attr, List<BBSimpleSlot> slots, int factory, int partFactory, Map<BBSimpleAttribute, BBSimpleAttribute> copied, String full, PositionEnum position)
	{
		rawName = attr.rawName;
		originalName = attr.rawName;
		name = registry.makeName(full + " :: " + rawName);
		type = attr.type;
		complex = attr.complex;
		readOnly = attr.readOnly;
		uuid = complex.getUuid();

		BBSimpleSlot slot = null;
		for (BBSimpleSlot s : slots)
			if (attr == s.getAttribute())
			{
				slot = s;
				break;
			}
		
		// if we have a slot, use this
		// otherwise use the default value
		if (slot != null)
		{
			defaultValue = translateDefaultValues(copied, slot.getValue());
			copy = slot.getEnvironmentAlias();
			setFactory(slot.getAttribute(), factory, partFactory);
		}
		else
		{
			defaultValue = translateDefaultValues(copied, attr.defaultValue);
			this.factory = factory;
		}
		this.position = position;
	}

	private void setFactory(BBSimpleAttribute attr, int factory, int partFactory)
	{
		// if this is a pullup, then set the literal of the factory
		boolean pullUp = attr.complex.isPullUp();
		this.factory = pullUp ? partFactory : factory;
		if (matchesName(defaultValue, "var:factory#"))
		{
			defaultValue = new ArrayList<BBSimpleParameter>();
			defaultValue.add(new BBSimpleParameter("" + factory));
		}
	}

	private boolean matchesName(List<BBSimpleParameter> defaultValue, String str)
	{
		return defaultValue != null && defaultValue.size() == 1 && str.equals(defaultValue.get(0).getLiteral());
	}

	private List<BBSimpleParameter> translateDefaultValues(
			Map<BBSimpleAttribute, BBSimpleAttribute> copied,
			List<BBSimpleParameter> values)
	{
		if (values == null)
			return null;
		
		List<BBSimpleParameter> newValues = new ArrayList<BBSimpleParameter>();
		for (BBSimpleParameter p : values)
			if (p.getAttribute() != null)
			{
				BBSimpleAttribute simple = p.getAttribute();
				if (simple != null)
				{
					newValues.add(new BBSimpleParameter(copied == null || copied.get(simple) == null ? simple : copied.get(simple)));
				}
			}
			else
				newValues.add(new BBSimpleParameter(p.getLiteral()));
		
		return newValues;
	}

	public void resolveImplementation(BBSimpleElementRegistry registry) throws BBImplementationInstantiationException
	{
		if (resolved)
			return;
		type.resolveImplementation(registry);

		if (defaultValue != null)
		{
			if (isNull(defaultValue))
			{
				isNull = true;
				return;
			}
			for (BBSimpleParameter p : defaultValue)
				p.resolveImplementation(registry, this);
	    isDefault = defaultValue.size() == 1 && defaultValue.get(0).isDefault();
	    
			// is this a copy?
			if (defaultValue.size() == 1 && defaultValue.get(0).getAttribute() != null)
			{
				BBSimpleAttribute copyAttr = defaultValue.get(0).getAttribute();
				if (copyAttr.getType() == type)
					copy = copyAttr;
			}
			
			if (copy == null)
				constructor = resolveConstructor(type.getImplementationClass(), defaultValue, this);
		}

		// place here so we aren't resolved for the above methods
		resolved = true;		
	}

	private boolean isNull(List<BBSimpleParameter> value)
	{
		return value.size() == 1 && value.get(0).getLiteral() != null && value.get(0).getLiteral().equals("null");
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getRawName()
	{
		return rawName;
	}

	public DEAttribute getComplex()
	{
		return complex;
	}

	public int getFactory()
	{
		return factory;
	}
	
	private Constructor<?> resolveConstructor(Class<?> cls, List<BBSimpleParameter> values, BBSimpleAttribute attr) throws BBImplementationInstantiationException
	{
    int size = values.size();

    // is this a default?
		if (isDefault)
			size = 0;
		
    // go through the constructors, and take the 1st match
    for (Constructor<?> cons : determineCandidateConstructors(cls, size))
    {
      Class<?> parameterClasses[] = cons.getParameterTypes();

      // build up the list
      List<Class<?>> classes = new ArrayList<Class<?>>();
      
      boolean matchedParameterTypes = true;
      int lp = 0;
      if (!isDefault)
	      for (BBSimpleParameter p : values)
	      {
	  	    // see if we have a compatible class
	      	Class<?> valueClass = p.getValueClass();
	      	Class<?> param = parameterClasses[lp];
	      	if (valueClass == null && !param.isPrimitive())
	      		classes.add(param); // null matches any non-primitive
	      	else
	      	{
		  	    Class<?> compatibleClass = extractCompatibleClass(param, valueClass);
		  	    if (compatibleClass == null)
		  	      matchedParameterTypes = false;
		  	    // add the converted object to the list
		  	    classes.add(compatibleClass);
	      	}	
	        
	        // advance the parameter counter
	        lp++;
	      }
      
      // if the types matched, invoke the constructor
      if (matchedParameterTypes)
      	return cons;
    }

    // if we got here, no valid constructor could be found
    String valueClasses = "[";
    for (BBSimpleParameter p : values)
    {
    	valueClasses += p.getValueClass().getName() + " ";
    }
    valueClasses += "]";
    throw new BBImplementationInstantiationException("Problem with attribute " + attr + " finding valid constructor for " + cls + " from parameters (" + makeClassString(attr) + "), values are " + valueClasses, owner);
	}
	
	private String makeClassString(BBSimpleAttribute attr)
	{
		
		List<BBSimpleParameter> values = attr.defaultValue;
		if (values == null)
			return "void";
		String str = "";
		int lp = 0;
    for (BBSimpleParameter p : values)
  		str += (lp++ != 0 ? ", " : "") + PrimitiveHelper.stripJavaLang(p.getValueClass().getName());
    return str;
	}

	public BBSimpleElement getType()
	{
		return type;
	}
	
  /**
   * NOTE: floats cause a problem, so we cannot move from any other numeric type to a float
   *       floats must be directly specified using the (fF) notation
   * @param parameterClass
   * @param defaultValue
   * @return
   */
  private Class<?> extractCompatibleClass(Class<?> parameterClass, Class<?> valueClass)
  {
  	// if the class is already ok, don't adjust the value
    if (parameterClass.isAssignableFrom(valueClass))
      return parameterClass;;
    
    // if a small numeric conversion is required, adjust the object
    if (valueClass == Double.class)
    {
      if (parameterClass == double.class || parameterClass == Double.class)
        return Double.class;
      // if we got here, we haven't got a match
      return null;
    }
    if (valueClass == Float.class)
    {
      if (parameterClass == float.class || parameterClass == Float.class)
        return Float.class;
      if (parameterClass == double.class || parameterClass == Double.class)
        return Double.class;
      // if we got here, we haven't got a match
      return null;
    }
    if (valueClass == Integer.class)
    {
      if (parameterClass == int.class)
        return Integer.class;
      if (parameterClass == long.class || parameterClass == Long.class)
        return Long.class;
      if (parameterClass == float.class || parameterClass == Float.class)
        return Float.class;
      if (parameterClass == double.class || parameterClass == Double.class)
        return Double.class;
      // if we got here, we haven't got a match
      return null;
    }
    if (valueClass == Long.class)
    {
      if (parameterClass == long.class)
        return Long.class;
      if (parameterClass == float.class || parameterClass == Float.class)
        return Float.class;
      if (parameterClass == double.class || parameterClass == Double.class)
        return Double.class;
      // if we got here, we haven't got a match
      return null;
    }
    if (valueClass == Boolean.class)
    	if (parameterClass == boolean.class)
    		return Boolean.class;
    
    // if we are here, there is no match between the value and the parameter class
    return null;
  }

  /**
   * @param shortClass
   * @param numberOfParameters
   * @return
   */
  private List<Constructor<?>> determineCandidateConstructors(Class<?> shortClass, int numberOfParameters)
  {
    List<Constructor<?>> candidates = new ArrayList<Constructor<?>>();
    Constructor<?> constructors[] = shortClass.getConstructors();
    for (int lp = 0; lp < constructors.length; lp++)
    {
      Constructor<?> constructor = constructors[lp];
      if (constructor.getParameterTypes().length == numberOfParameters)
      {
        candidates.add(constructor);
      }
    }
    return candidates;
  }

	public PositionEnum getPosition()
	{
		return position;
	}

	public void remapAttributes(Map<BBSimpleAttribute, BBSimpleAttribute> redundant)
	{
		if (defaultValue != null)
			for (BBSimpleParameter v : defaultValue)
				v.remapAttribute(redundant);
	}

	public void addAllDependencies(Set<BBSimpleAttribute> all)
	{
		// add an alias or anything reference from the parameters
		if (defaultValue != null)
			for (BBSimpleParameter v : defaultValue)
				v.addAllDependencies(all);
	}

	public void setPosition(PositionEnum position)
	{
		this.position = position;
	}
	
	@Override
	public String toString()
	{
		return rawName;
	}
	
	public Object instantiate(BBSimpleInstantiatedFactory context, Map<String, Object> setValues, Set<String> setNames) throws BBRuntimeException
	{
		// create using the chosen constructor
		try
		{
			// (1) has this been overridden by the program caller?
			Class<?> implClass = type.getImplementationClass();
			
			if (setNames != null && position == PositionEnum.TOP && setNames.contains(originalName))
			{
				setNames.remove(originalName);
				Object value = setValues.get(originalName);
				if (!(implClass.isAssignableFrom(value.getClass())))
					throw new BBRuntimeException("Value " + value + " supplied for attribute " + name + " is not an instance of " + implClass, owner);
				return value;
			}

			// (2) if this has a default value of null, return null
			if (isNull)
				return null;
			
			// (3) is this just a default instantiation, with no possible parameters?
			if (isDefault)
				return constructor.newInstance();

			// (4) is this a copy
			if (copy != null)
				return context.resolveAttributeValue(copy);
			
			// short cut for basic, immutable types
			if (defaultValue != null && defaultValue.size() == 1)
			{
				BBSimpleParameter first = defaultValue.get(0);
				if (first.getLiteral() != null && first.getValueClass() == implClass)
					return first.getResolvedLiteral();
			}
			
			// (5) should we instantiate from default parameters, each of which may further refer to other variables? 
			// if we don't have a default then it hasn't been set even though it should have been
			if (defaultValue == null)
				throw new BBRuntimeException("No value for attribute " + name + " in factory " + factory, owner);
			
			// otherwise, make the values
			int size = defaultValue.size();
			Object values[] = new Object[size];			
			int lp = 0;
			for (BBSimpleParameter p : defaultValue)
				values[lp++] = p.resolveValue(context);
			return constructor.newInstance(values);
		}
		catch (BBRuntimeException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new BBRuntimeException("Problem when constructing " + type.getImplementationClass() + " in factory " + factory, owner, ex);
		}
	}
	
	public List<BBSimpleParameter> getDefaultValue()
	{
		return defaultValue;
	}

	public BBSimpleElement getOwner()
	{
		return owner;
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}

  public boolean isWriteOnly()
  {
    return writeOnly;
  }
  
	@Override
	public Map<String, List<? extends BBSimpleObject>> getChildren(boolean top)
	{
		Map<String, List<? extends BBSimpleObject>> children = new LinkedHashMap<String, List<? extends BBSimpleObject>>();
		List<BBSimpleObject> t = new ArrayList<BBSimpleObject>();
		t.add(type);
		children.put("type", t);
		children.put("default value", defaultValue);
		return children;
	}

	@Override
	public String getTreeDescription()
	{
		return
			"Attribute " + name;
	}
}
