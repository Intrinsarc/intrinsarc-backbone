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
import java.util.*;

import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.rebind.*;

public class StateDispatcherGenerator extends Generator
{
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException
	{
		try
		{
			TypeOracle typeOracle = context.getTypeOracle();
			JClassType classType = typeOracle.getType(typeName);
			JParameterizedType parameterizedType = classType.getSuperclass().isParameterized();
			JClassType T = parameterizedType.getTypeArgs()[0];
			logger.log(TreeLogger.DEBUG, "Parameterised type = " + T.getName());

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
		composerFactory.addImplementedInterface("com.intrinsarc.backbone.runtime.api.IStateDispatcher");
		composerFactory.addImport("com.intrinsarc.backbone.runtime.api.*");
		return composerFactory.createSourceWriter(context, printWriter);
	}

	protected void writeToStringMethod(TreeLogger logger, String proxyClassName, JClassType T, SourceWriter writer)
	{
		writer.println("private java.util.List<IEvent> dDispatch_IEventRequired = new java.util.ArrayList<IEvent>();");
		writer.println("public void setDDispatch(IEvent event, int index) { PortHelper.fill(this.dDispatch_IEventRequired, event, index); }");	
		writer.println("public void addDDispatch(IEvent event) { PortHelper.fill(this.dDispatch_IEventRequired, event, -1); }");	
		writer.println("public void removeDDispatch(IEvent event) { PortHelper.remove(this.dDispatch_IEventRequired, event); }");	
		writer.println("private ITerminal dStart_ITerminalRequired;");
		writer.println("public void setDStart(ITerminal start) { dStart_ITerminalRequired = start; }");
		writer.println("private java.util.List<ITerminal> dEnd_ITerminalRequired = new java.util.ArrayList<ITerminal>();");
		writer.println("public void setDEnd(ITerminal end, int index) { PortHelper.fill(this.dEnd_ITerminalRequired, end, index); }");
		writer.println("private boolean currentEntered;");
		writer.println("private IEvent current;");
		writer.println("private boolean primed;");
		
		writer.println("  private void findNextState()");
		writer.println("  {");
		writer.println("    // possibly still in start or end?");
		writer.println("    if (dStart_ITerminalRequired != null && !primed)");
		writer.println("    {");
		writer.println("      dStart_ITerminalRequired.moveToNextState(); primed = true;");
		writer.println("    }");		
		writer.println("    if (dEnd_ITerminalRequired != null)");
		writer.println("      for (ITerminal end : dEnd_ITerminalRequired)");
		writer.println("        if (end.isCurrent())");
		writer.println("		       end.moveToNextState();");
		writer.println("    // find the next state");
		writer.println("    current = null;");
		writer.println("    for (IEvent e : dDispatch_IEventRequired)");
		writer.println("      if (e.isCurrent())");
		writer.println("      {");
		writer.println("		     current = e;");
		writer.println("		     break;");
		writer.println("      }");
		writer.println("  }");
		writer.println();
		
		writer.println("public IEvent getDEvents_Provided(Class<?> required)");
		writer.println("{");
		writer.println("  return new " + T.getQualifiedSourceName() + "()");
		writer.println("  {");
		Set<JMethod> all = new LinkedHashSet<JMethod>();
		getAllMethods(all, T);
		for (JMethod method : all)
		{
			String ret = method.getReturnType().getQualifiedBinaryName();
			boolean isVoid = ret.equals("void");			
			writer.print("    public " + ret + " " + method.getName() + "(");
			int lp = 0;
			String params = "";
			String cparams = "";
			for (JParameter p : method.getParameters())
			{
				if (lp != 0)
				{
					params += ", ";
					cparams += ", ";
				}
				lp++;
				params += p.getType().getQualifiedBinaryName() + " param" + lp;
				cparams += " param" + lp;
			}
			writer.println(params + ")");
			writer.println("    {");
			writer.println("      findNextState();");
			writer.println("      if (current != null)");
			writer.println("      {");
			String cast = "(" + T.getQualifiedSourceName() + ")";
			if (!isVoid)
			{
				writer.println("        " + ret + " v = (" + cast + "current)." + method.getName() + "(" + cparams + ");");
				writer.println("        findNextState();");
				writer.println("        return v;");
				writer.println("      }");
			
				if (ret.equals("boolean"))
					writer.println("      return false;");
				else
				if (ret.equals("int") || ret.equals("short") || ret.equals("long") || ret.equals("float") || ret.equals("double") || ret.equals("byte") || ret.equals("char"))
					writer.println("      return 0;");
				else
					if (ret.equals("long"))
						writer.println("      return 0;");
				else
					writer.println("      return null;");
			}
			else
			{
				writer.println("        (" + cast + "current)." + method.getName() + "(" + cparams + ");");
				writer.println("        findNextState();");
				writer.println("      }");
			}
			writer.println("    }");
		}
		writer.println("\n  public String toString() { return \"State dispatcher: current state =\" + current; }");
		writer.println("  };\n");		
		writer.println("}\n");
	}

	private void getAllMethods(Set<JMethod> methods, JClassType t)
	{
		for (JMethod m : t.getMethods())
			methods.add(m);
		for (JClassType i : t.getImplementedInterfaces())
			getAllMethods(methods, i);
	} 
}
