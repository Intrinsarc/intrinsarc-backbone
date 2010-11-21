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
package com.intrinsarc.backbone.parserbase;


public class ParameterPatternMatcher implements IPatternMatcher
{
	public ParameterPatternMatcher()
	{
	}

	public boolean matches(Token tok)
	{
		TokenType t = tok.getType();
		return
			t.equals(TokenType.CHAR) ||
			t.equals(TokenType.INTEGER) ||
			t.equals(TokenType.DOUBLE) ||
			t.equals(TokenType.STRING) ||
			t.equals(TokenType.LITERAL);
	}

	public String getDescription()
	{
		return "int, char, double, string or variable parameter";
	}
}
