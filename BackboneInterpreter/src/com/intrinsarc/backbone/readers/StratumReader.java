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
package com.intrinsarc.backbone.readers;

import java.io.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parser.*;
import com.intrinsarc.backbone.parserbase.*;

/**
 * reads a stratum in from a directory.  will look through all child directories for child strata
 * @author andrew
 */
public class StratumReader
{
  private File directory;
  
  public StratumReader(File directory)
  {
    this.directory = directory;
  }
  
  public BBStratum readStratum() throws StratumLoadingException, BBNodeNotFoundException
  {
  	File fileList[] = directory.listFiles();
  	if (fileList != null)
	  	for (File child : fileList)
	  		if (child.getName().endsWith(".bb"))
	  			return readStratum(child);

  	// if we get here, then we have found no .bb files
  	throw new StratumLoadingException("Found no .bb files in " + directory);
  }
  
  private BBStratum readStratum(File stratum) throws StratumLoadingException, BBNodeNotFoundException
  {
    if (!stratum.exists())
      throw new StratumLoadingException("Cannot read " + stratum);
    
  	Expect expect = LoadListReader.makeExpect(stratum);
  	try
  	{
  		long start = System.currentTimeMillis();
  		BBStratum st = new StratumParser(expect).parse();
  		long end = System.currentTimeMillis();
  		return st;
  	}
  	catch (ParseException ex)
  	{
      throw new StratumLoadingException("Parsing problem with: " + stratum + ", message is: " + ex.getMessage());    	
  	}
  }
}
