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

import com.intrinsarc.backbone.nodes.lazy.*;

public class Expect
{
	private Tokenizer tok;
	
	public Expect(Tokenizer tok)
	{
		this.tok = tok;
	}
	
	public Expect literal(String literal)
	{
		Token t = tok.next();
		if (t == null)
			tok.throwParseException("Expected literal '" + literal + "' but found end of file", false);
		if (!t.equals(new Token(TokenType.LITERAL, literal)))
			tok.throwParseException("Expected literal '" + literal + "' but found " + t, true);
		return this;
	}
	
	public Expect string(String str[])
	{
		Token t = tok.next();
		if (t == null)
			tok.throwParseException("Expected string but found end of file", false);
		if (!t.getType().equals(TokenType.STRING))
			tok.throwParseException("Expected literal but found " + t, true);
		if (str != null)
			str[0] = t.getText();
		return this;
	}
	
	public Expect literal()
	{
		return literal((String[]) null);
	}
	
	public Expect literal(String[] literal)
	{
		Token t = tok.next();
		if (t == null)
			tok.throwParseException("Expected literal but found end of file", false);
		if (!t.getType().equals(TokenType.LITERAL))
			tok.throwParseException("Expected literal but found " + t, true);
		if (literal != null)
			literal[0] = t.getText();
		return this;
	}
	
	public Expect integer(int[] integer)
	{
		Token t = tok.next();
		if (t == null)
			tok.throwParseException("Expected integer but found end of file", false);
		if (!t.getType().equals(TokenType.INTEGER))
			tok.throwParseException("Expected integer but found " + t, true);
		if (integer != null)
			integer[0] = Integer.parseInt(t.getText());
		return this;
	}
	
	public Expect optionalLiteral(String literal, final boolean toSet[])
	{
		toSet[0] = false;
		return optional(false, null, new Match(
				new LiteralPatternMatcher(literal),
				new IAction()
				{
					public void act()
					{
						tok.next();
						toSet[0] = true;
					}
				}));
	}
	
	public Expect oneOf(Match... matches)
	{
		return optional(true, null, matches);
	}
	
	public Expect anyOf(boolean matched[], Match... matches)
	{
		return optional(false, matched, matches);
	}

	public Expect uuid(UuidReference reference)
	{
		reference.setFile(tok.getFile());
		reference.setLine(tok.getCurrentLine());
		reference.setPos(tok.getCurrentPos());
		Token t = tok.next();
		if (t == null)
			tok.throwParseException("Expected a UUID but found end of file", false);
		if (!t.getType().equals(TokenType.LITERAL))
			tok.throwParseException("Expected a UUID but found " + t, true);
		reference.setUUID(t.getText());
		
		// peek for a string
		Token p = tok.peek();
		if (p != null && p.getType().equals(TokenType.DESCRIPTIVE_NAME))
				reference.setName(tok.next().getText());
		else
			reference.setName(t.getText());
		
		return this;
	}
	
	public String makeName(String prefixIfNotGlobal, String text)
	{
		if (!text.contains("-") && !text.contains("."))
			text = prefixIfNotGlobal + "." + text;
		return text;
	}

	/**
	 * will match one or more of matches 
	 * @param continuer the string to continue when we find it
	 * @param matches
	 */
	public Expect oneOrMore(String continueLiteral, Match... matches)
	{
		Token cnt = new Token(TokenType.LITERAL, continueLiteral);
		for (;;)
		{
			oneOf(matches);
			if (!tok.peek().equals(cnt))
				break;
			tok.next();
		}
		return this;
	}

	/**
	 * will match zero or more of matches.  no continuer string allowed 
	 * @param matches
	 */
	public Expect zeroOrMore(Match... matches)
	{
		boolean matched[] = {false};
		for (;;)
		{
			anyOf(matched, matches);
			if (!matched[0])
				break;
		}
		return this;
	}

	public Expect guard(String literal, IAction ifTrue)
	{
		return guard(literal, ifTrue, null);
	}
	
	public Expect guard(String literal, IAction ifTrue, IAction ifFalse)
	{
		// peek to see if we have a match
		if (tok.peek().equals(new Token(TokenType.LITERAL, literal)))
		{
			tok.next();
			ifTrue.act();
		}
		else
		{
			if (ifFalse != null)
				ifFalse.act();
		}
		return this;
	}
	
	public Expect inside(IAction action)
	{
		action.act();
		return this;
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	private Expect optional(boolean mustHaveOne, boolean matched[], Match... matches)
	{
		if (matched != null)
			matched[0] = false;
		Token t = tok.peek();
		if (t == null)
		{
			if (mustHaveOne)
				tok.throwParseException("Expected one of the following " + getDescriptions(matches) + " but found end of file", false);
			return this;
		}
		int lp = 0;
		for (Match m : matches)
		{
			if (m.getMatcher().matches(t))
			{
				m.getAction().act();
				if (matched != null)
					matched[0] = true;
				return this;
			}
			lp++;
		}
		if (mustHaveOne)
			tok.throwParseException("Expected one of the following " + getDescriptions(matches) + " but found " + t, true);
		return this;
	}
	
	private String getDescriptions(Match[] matches)
	{
		String strs = "(";
		int lp = 0;
		for (Match m : matches)
		{
			if (lp++ != 0)
				strs += ", ";
			strs = m.getMatcher().getDescription();
		}
		return strs;
	}

	public Token next()
	{
		return tok.next();
	}


	public Token peek()
	{
		return tok.peek();
	}

	public Token parameter()
	{
		final Token t[] = {null};
		oneOf(new ParameterMatch(
			new IAction()
			{
				public void act()
				{
					t[0] = next();
				}
			}));
		return t[0];
	}

	public UuidReference nextUuid()
	{
		UuidReference reference = new UuidReference();
		uuid(reference);
		return reference;
	}
}
