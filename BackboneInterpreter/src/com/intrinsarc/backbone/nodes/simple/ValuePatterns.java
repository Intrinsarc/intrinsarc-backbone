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

import java.util.regex.*;

public class ValuePatterns
{
	public static final String INTERNAL_PATTERN = ">[^\\{]*";
	public static final String STRING_PATTERN = "(?:(?:\\\\\")|[^\"])*";
	public static final String PLUS_MINUS="[\\+\\-]?";
	public static final String DOUBLE_PATTERN = PLUS_MINUS + "[0-9]*\\.[0-9]+(?:[eE]?[-+][0-9]+)?";
	public static final String FLOAT_PATTERN = DOUBLE_PATTERN + "[fF]";
	public static final String CHAR_PATTERN = "'[\\\\]?.'";
	public static final String INT_PATTERN = PLUS_MINUS + "[0-9]+";
	public static final String BOOLEAN_PATTERN = "true|false";
	public static final String NULL_PATTERN = "null";
	public static final String VAR_PATTERN = "[a-zA-Z_$\\!\\?][a-zA-Z0-9_$\\!\\?]*";
	public static final int NUM_PATTERNS = 9;
	public static final Pattern OR_PATTERN = Pattern.compile(
			"(\\s*(?:(?:\"(" +
				STRING_PATTERN +")\")|(" +
				FLOAT_PATTERN + ")|(" +
				DOUBLE_PATTERN + ")|(" +
				CHAR_PATTERN + ")|(" +
				INT_PATTERN + ")|(" +
				BOOLEAN_PATTERN + ")|(" +
				NULL_PATTERN + ")|(" +
				VAR_PATTERN + ")|(" +
				INTERNAL_PATTERN + "))).*");

	public static final String DEFAULT_PATTERN = "default";
}
