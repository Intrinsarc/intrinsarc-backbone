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
import java.util.*;

import com.intrinsarc.backbone.exceptions.*;
import com.intrinsarc.backbone.nodes.*;
import com.intrinsarc.backbone.nodes.insides.*;
import com.intrinsarc.backbone.nodes.lazy.*;
import com.intrinsarc.backbone.parser.*;
import com.intrinsarc.backbone.parserbase.*;

public class LoadListReader
{
  private File loadList;
  
  public LoadListReader(File loadList)
  {
    this.loadList = loadList;
  }
  
  public List<BBStratum> readSystem() throws StratumLoadingException, BBNodeNotFoundException, BBVariableNotFoundException 
  {
    if (!loadList.exists())
      throw new StratumLoadingException("Cannot read load list from " + loadList);
    
  	Expect expect = makeExpect(loadList);
  	BBLoadList node = null;
  	try
  	{
  		node = new LoadListParser(expect).parse();
  	}
  	catch (ParseException ex)
  	{
      throw new StratumLoadingException("Parsing problem with: " + loadList + ", message is: " + ex.getMessage());    	
  	}
  	
    List<BBStratumDirectory> directories = new ArrayList<BBStratumDirectory>(node.getTranslatedStrataDirectories());
    // need to load the last first
    Collections.reverse(directories);
    
    // look through each directory and recursively load any stratum files
    List<BBStratum> strata = new ArrayList<BBStratum>();
    for (BBStratumDirectory directory : directories)
    {
      StratumReader reader = new StratumReader(new File(directory.getPath()));
      BBStratum stratum = reader.readStratum();    
      strata.add(stratum);
    }			
    
    return strata;
  }
  
  /**
   * make an expect on a file's contents 
   */
  public static Expect makeExpect(File file) throws StratumLoadingException
  {
    StringBuffer buf = new StringBuffer();
    BufferedReader buffered = null;
    try
    {
    	buffered = new BufferedReader(new FileReader(file));
    	String line;
    	while ((line = buffered.readLine()) != null)
    	{
    		buf.append(line);
    		buf.append("\n");
    	}
    }
    catch (FileNotFoundException ex)
    {
      throw new StratumLoadingException("File not found: " + file);
    }
    catch (IOException ex)
    {
      throw new StratumLoadingException("Problem loading file during reading: " + file);    	
    }
    finally
    {
    	// close the reader
    	try
			{
    		if (buffered != null)
    			buffered.close();
			}
    	catch (IOException e)
			{
			}
    }
    
    return new Expect(new Tokenizer(file.toString(), new StringReader(buf.toString())));
  }
}
