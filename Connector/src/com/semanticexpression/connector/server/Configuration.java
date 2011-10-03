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

package com.semanticexpression.connector.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Configuration
{
  private long cacheLastModified;
  private LinkedHashMap<String, String> configurationCache;
  private File file;

  public Configuration(String fileName)
  {
    file = new File(fileName);
    if (!file.canRead())
    {
      try
      {
        Log.error("Connector.Connector: cannot read configuration file, canonicalPath=%s", file.getCanonicalPath());
      }
      catch (IOException e)
      {
        Log.error("Connector.Connector: cannot read configuration file, nor get canonical path, e=%s", e);
      }
      throw new IllegalArgumentException("Cannot read file, fileName=" + fileName);
    }
  }

  public String get(String name, String defaultValue)
  {
    validateCache();
    String value = configurationCache.get(name);
    if (value == null)
    {
      value = defaultValue;
    }
    Log.debug("Configuration.get: name=%s, value=%s", name, value);
    return value;
  }

  public String get(String name)
  {
    String value = get(name, null);
    if (value == null)
    {
      throw new IllegalArgumentException("Undefined key, name=" + name);
    }
    return value;
  }

  public LinkedHashMap<String, String> getAll(String regularExpression)
  {
    Pattern pattern = Pattern.compile(regularExpression);
    LinkedHashMap<String, String> treeMap = new LinkedHashMap<String, String>();
    for (Entry<String, String> entry : configurationCache.entrySet())
    {
      String name = entry.getKey();
      if (pattern.matcher(name).matches())
      {
        String value = entry.getValue();
        treeMap.put(name, value);
      }
    }
    return treeMap;
  }

  public String getFormatted(String name, Object... arguments)
  {
    String pattern = get(name);
    String value = MessageFormat.format(pattern, arguments);
    return value;
  }

  public int getInt(String name)
  {
    String stringValue = get(name);
    int value = Integer.parseInt(stringValue);
    return value;
  }

  public boolean getBoolean(String name)
  {
    String stringValue = get(name);
    boolean value = Boolean.parseBoolean(stringValue);
    return value;
  }

  public void loadCache(File file)
  {
    ConfigurationFileReader configurationFileReader = new ConfigurationFileReader(file);
    configurationCache = configurationFileReader.read();
  }

  private void validateCache()
  {
    long fileLastModified = file.lastModified();
    if (fileLastModified > cacheLastModified)
    {
      loadCache(file);
      cacheLastModified = fileLastModified;
    }
  }

  private class ConfigurationFileReader
  {
    private File file;
    private LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
    private String name = null;
    private StringBuilder value = null;

    private ConfigurationFileReader(File file)
    {
      this.file = file;
    }

    private int findFirstDelimiter(String record)
    {
      int length = record.length();
      int delimiter = -1;
      for (int i = 0; i < length && delimiter == -1; i++)
      {
        char c = record.charAt(i);
        if (c == ':' || c == '=')
        {
          delimiter = i;
        }
      }
      return delimiter;
    }

    private void parse(String record)
    {
      if (record.length() > 0)
      {
        char c = record.charAt(0);
        if (c == '#')
        {
        }
        else if (Character.isWhitespace(c))
        {
          parseContinuationRecord(record);
        }
        else
        {
          parseDefinitionRecord(record);
        }
      }
    }

    private void parseContinuationRecord(String record)
    {
      if (name == null)
      {
        throw new IllegalStateException("Missing name and = delimiter, record=" + record);
      }
      String trimmedRecord = record.trim();
      if (trimmedRecord.length() > 0)
      {
        if (value.length() > 0)
        {
          value.append("\n");
        }
        value.append(trimmedRecord);
      }
    }

    private void parseDefinitionRecord(String record)
    {
      int delimiter = findFirstDelimiter(record);
      if (delimiter == -1)
      {
        throw new IllegalArgumentException("Missing = delimiter, record=" + record);
      }
      saveDefinition();
      name = record.substring(0, delimiter).trim();
      value = new StringBuilder(record.substring(delimiter + 1).trim());
    }

    private LinkedHashMap<String, String> read()
    {
      try
      {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try
        {
          String record;
          while ((record = reader.readLine()) != null)
          {
            parse(record);
          }
          saveDefinition();
        }
        finally
        {
          reader.close();
        }
      }
      catch (FileNotFoundException e)
      {
        throw new RuntimeException(e);
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
      return map;
    }

    public void saveDefinition()
    {
      if (name != null)
      {
        String stringValue = value.toString();
        Log.debug("ConfigurationFileReader.saveDefinition: name=%s, value=%s", name, stringValue);
        map.put(name, stringValue);
      }
    }
  }

}
