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

public class PrimitiveHelper
{
	public static String translateShortToLongPrimitive(String shortName)
	{
		if (shortName.equals("int"))
			return "java.lang.Integer";
		if (shortName.equals("boolean"))
			return "java.lang.Boolean";
		if (shortName.equals("double"))
			return "java.lang.Double";
		if (shortName.equals("long"))
			return "java.lang.Long";
		if (shortName.equals("char"))
			return "java.lang.Character";
		if (shortName.equals("short"))
			return "java.lang.Short";
		if (shortName.equals("byte"))
			return "java.lang.Byte";
		if (shortName.equals("float"))
			return "java.lang.Float";
		
		// if we got here, it isn't an inbuilt type
		return shortName;
	}
	
  public static String translateLongToShortPrimitive(String longName)
  {
  	if (longName.equals("java.lang.Integer"))
  		return "int";
  	if (longName.equals("java.lang.Boolean"))
  		return "boolean";
  	if (longName.equals("java.lang.Double"))
  		return "double";
  	if (longName.equals("java.lang.Long"))
  		return "long";
  	if (longName.equals("java.lang.Character"))
  		return "char";
  	if (longName.equals("java.lang.Short"))
  		return "short";
  	if (longName.equals("java.lang.Byte"))
  		return "byte";
  	if (longName.equals("java.lang.Float"))
  		return "float";
  	return longName;
  }
  
	private static final String JAVA_LANG = "java.lang.";
	public static String stripJavaLang(String className)
	{
		if (className.startsWith(JAVA_LANG))
			return className.substring(JAVA_LANG.length());
		return className;
	}

  public static Class<?> translateLongToShortPrimitive(Class<?> longImplClass)
	{
  	if (longImplClass == java.lang.Integer.class)
  		return int.class;
  	if (longImplClass == java.lang.Boolean.class)
  		return boolean.class;
  	if (longImplClass == java.lang.Double.class)
  		return double.class;
  	if (longImplClass == java.lang.Long.class)
  		return long.class;
  	if (longImplClass == java.lang.Character.class)
  		return char.class;
  	if (longImplClass == java.lang.Short.class)
  		return short.class;
  	if (longImplClass == java.lang.Byte.class)
  		return byte.class;
  	if (longImplClass == java.lang.Float.class)
  		return float.class;
  	return longImplClass;
	}
}
