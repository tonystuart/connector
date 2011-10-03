// Copyright 2011 Semantic Expression, Inc. All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the GNU General Public License, either version 3 or (at your option)
// any later version. The terms of this license may be found at
// http://www.gnu.org/copyleft/gpl.html
//
// This program is made available on an "as is" basis, without warranties or
// conditions of any kind, either express or implied.
//
// Please contact us for other licensing options.
//
// Contributors:
//
// Anthony F. Stuart - Initial implementation
//
//
//

package com.semanticexpression.connector.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.dev.util.collect.HashSet;
import com.semanticexpression.connector.server.Utility;

public class WgetConverter
{
  public static void main(String[] args) throws Exception
  {
    String input = "input";
    String output = "output";

    for (String arg : args)
    {
      String[] pairs = arg.split("=");
      String name = pairs[0];
      String value = pairs.length == 1 ? null : pairs[1];
      if (name.equalsIgnoreCase("--input"))
      {
        input = value;
      }
      else if (name.equalsIgnoreCase("--output"))
      {
        output = value;
      }
      else
      {
        System.err.println("Usage: WgetConverter <options>");
        System.err.println("Options: --input=p --output=p");
        System.err.println("Where: p is a path name");
        System.exit(1);
      }
    }

    WgetConverter wgetConverter = new WgetConverter();
    wgetConverter.convert(input, output);
  }

  private void convert(File inputRootFile, File outputRootFile) throws Exception
  {
    Map<String, String> nameMap = new HashMap<String, String>();
    Set<String> titles = new HashSet<String>();
    
    File[] inputFiles = inputRootFile.listFiles();
    for (File inputFile : inputFiles)
    {
      String inputFileName = inputFile.getName();
      inputFileName = inputFileName.replace("%2F", "/");
      String outputFileName = convertName(inputFileName, titles);
      nameMap.put(inputFileName, outputFileName);
      System.out.println(inputFileName + " ->\n  " + outputFileName);
    }

    for (File inputFile : inputFiles)
    {
      String inputPathName = inputFile.getPath();
      String inputFileName = inputFile.getName();
      inputFileName = inputFileName.replace("%2F", "/");
      String outputFileName = nameMap.get(inputFileName);
      String outputPathName = outputRootFile.getPath() + "/" + outputFileName;
      File outputFile = new File(outputPathName);
      File outputDirectory = outputFile.getParentFile();
      outputDirectory.mkdirs();
      int level = countLevel(outputFileName);
      String relativeRoot = createRelativeRoot(level);
      if (inputFileName.contains("?path="))
      {
        String inputContents = Utility.readFile(inputPathName);
        String outputContents = inputContents;
        for (Entry<String, String> entry : nameMap.entrySet())
        {
          String inputName = entry.getKey();
          inputName = "/" + inputName; // match and replace leading slash on URLs
          String outputName = entry.getValue();
          outputName = relativeRoot + outputName;
          outputContents = outputContents.replace(inputName, outputName);
          // Also handle hyperlink styles with URLs of the form: content?path=141c-9f0c-58d9-ee68/439e-9348-55e0-41c4&amp;title=terms-of-use
          inputName = inputName.replace("&", "&amp;");
          outputContents = outputContents.replace(inputName, outputName);
        }
        Utility.writeFile(outputPathName, outputContents.getBytes());
      }
      else
      {
        Utility.copyFile(inputPathName, outputPathName);
      }
    }

  }

  private void convert(String inputRootName, String outputRootName) throws Exception
  {
    File inputRootFile = new File(inputRootName);
    File outputRootFile = new File(outputRootName);
    convert(inputRootFile, outputRootFile);
  }

  @SuppressWarnings("unused")
  private String convertName(String inputPathName)
  {
    String outputPathName;
    if (inputPathName.contains("?path="))
    {
      outputPathName = inputPathName.replace("content?path=", "");
      outputPathName = outputPathName.replace("&title=", "/");
    }
    else
    {
      outputPathName = inputPathName.replace("content?id=", "");
    }
    return outputPathName;
  }

  private String convertName(String inputPathName, Set<String> titles)
  {
    String outputPathName;
    if (inputPathName.contains("?path="))
    {
      int parameterOffset = inputPathName.indexOf("&title=");
      if (parameterOffset == -1)
      {
        throw new IllegalStateException("Missing &title= in " + inputPathName);
      }
      int titleOffset = parameterOffset + "&title=".length();
      String baseTitle = inputPathName.substring(titleOffset);
      outputPathName = createUniqueOutputPathName(baseTitle, titles) + ".html";
      titles.add(outputPathName);
    }
    else
    {
      outputPathName = inputPathName.replace("content?id=", "") + ".png";
    }
    return outputPathName;
  }

  private String createUniqueOutputPathName(String baseTitle, Set<String> titles)
  {
    int counter = 0;
    String outputPathName = baseTitle;
    while (titles.contains(outputPathName))
    {
      outputPathName = baseTitle + (++counter);
    }
    return outputPathName;
  }

  private int countLevel(String name)
  {
    int level = 0;
    int length = name.length();
    for (int i = 0; i < length; i++)
    {
      if (name.charAt(i) == '/')
      {
        level++;
      }
    }
    return level;
  }

  private String createRelativeRoot(int level)
  {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < level; i++)
    {
      s.append("../");
    }
    return s.toString();
  }

}
