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
package com.intrinsarc.backbone.gwt.generators;

import java.io.*;

import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.rebind.*;

public class TerminalGenerator extends Generator
{
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException
	{
		try
		{
			TypeOracle typeOracle = context.getTypeOracle();
			JClassType classType = typeOracle.getType(typeName);
			JParameterizedType parameterizedType = classType.getSuperclass().isParameterized();
			JClassType T = parameterizedType.getTypeArgs()[0];
			String packageName = classType.getPackage().getName();
			String proxyClassName = classType.getSimpleSourceName() + "Dispatcher";
			String qualifiedProxyClassName = packageName + "." + proxyClassName;

			SourceWriter writer = getSourceWriter(logger, context, packageName, proxyClassName);
			if (writer != null)
			{
				writeToStringMethod(logger, proxyClassName, T, writer);
				writer.commit(logger);
			}
			return qualifiedProxyClassName;
		}
		catch (NotFoundException e)
		{
			logger.log(TreeLogger.ERROR, "Class '" + typeName + "' Not Found", e);
			throw new UnableToCompleteException();
		}
	}

	protected SourceWriter getSourceWriter(TreeLogger logger, GeneratorContext context, String packageName, String className)
	{
		PrintWriter printWriter = context.tryCreate(logger, packageName, className);
		if (printWriter == null)
			return null;
		ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(packageName, className);
		composerFactory.addImplementedInterface("com.intrinsarc.backbone.runtime.api.IStateTerminalComponent");
		composerFactory.addImplementedInterface("com.intrinsarc.backbone.runtime.api.ITerminal");
		composerFactory.addImport("com.intrinsarc.backbone.runtime.api.*");
		return composerFactory.createSourceWriter(context, printWriter);
	}

	protected void writeToStringMethod(TreeLogger logger, String proxyClassName, JClassType T, SourceWriter writer)
	{
		writer.println("private ITransition out_ITransitionRequired;"); 
		writer.println("public void setOut(ITransition out) { out_ITransitionRequired = out; }");
		writer.println("private boolean current;");
		writer.println();
		writer.println("public boolean isCurrent() { return current; }");
		writer.println("public void moveToNextState() { if (out_ITransitionRequired != null) current = !out_ITransitionRequired.enter(); }");
		writer.println("");
		writer.println("public ITransition getIn(Class<?> required)");
		writer.println("{");
		writer.println("  return new ITransition()");
		writer.println("  {");
		writer.println("  	public boolean enter()");
		writer.println("  	{");
		writer.println("  		current = true;");
		writer.println("  		return true;");
		writer.println("  	}");
		writer.println("  };");
		writer.println("};");
	}
}
