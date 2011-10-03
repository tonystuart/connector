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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Scanner;

public class Utility
{
  private static Random random = new Random();

  public static String capitalize(String text)
  {
    int length = text.length();
    StringBuilder s = new StringBuilder(length);

    for (int i = 0; i < length; i++)
    {
      char c = text.charAt(i);
      if (i == 0)
      {
        c = Character.toUpperCase(c);
      }
      else
      {
        c = Character.toLowerCase(c);
      }
      s.append(c);
    }
    return s.toString();
  }

  public static void copyFile(File inputFile, File outputFile)
  {
    try
    {
      InputStream inputStream = new FileInputStream(inputFile);
      try
      {
        OutputStream outputStream = new FileOutputStream(outputFile);
        try
        {
          copyFile(inputStream, outputStream);
        }
        finally
        {
          outputStream.close();
        }
      }
      finally
      {
        inputStream.close();
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
  }

  public static void copyFile(InputStream inputStream, OutputStream outputStream)
  {
    try
    {
      int rc;
      byte[] buffer = new byte[32768];
      while ((rc = inputStream.read(buffer)) != -1)
      {
        outputStream.write(buffer, 0, rc);
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static void copyFile(String inputPath, String outputPath)
  {
    File inputFile = new File(inputPath);
    File outputFile = new File(outputPath);
    copyFile(inputFile, outputFile);
  }

  static String createRandomString(String charSet, int requiredLength)
  {
    int charSetSize = charSet.length();
    StringBuilder s = new StringBuilder(requiredLength);
    for (int i = 0; i < requiredLength; i++)
    {
      int charSetOffset = random.nextInt(charSetSize);
      s.append(charSet.charAt(charSetOffset));
    }
    return s.toString();
  }

  public static boolean fuzzyEquals(String left, String right)
  {
    left = getAlphaNumeric(left);
    right = getAlphaNumeric(right);
    return left.equals(right);
  }

  public static String getAlphaNumeric(String value)
  {
    StringBuilder s = new StringBuilder();
    int length = value.length();
    for (int i = 0; i < length; i++)
    {
      char c = value.charAt(i);
      if (Character.isUpperCase(c))
      {
        s.append(Character.toLowerCase(c));
      }
      else if (Character.isLetterOrDigit(c))
      {
        s.append(c);
      }
    }
    return s.toString();
  }

  public static String getCurrentDirectory()
  {
    try
    {
      return new File(".").getCanonicalPath();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static int getIntegerLength(File file)
  {
    long length = file.length();
    if (length >= Integer.MAX_VALUE)
    {
      throw new IllegalArgumentException(file.getName() + " is too large");
    }
    return (int)length;
  }

  public static String getString(InputStream inputStream)
  {
    return new Scanner(inputStream).useDelimiter("\\A").next();
  }

  private static char lookup(int i)
  {
    return "0123456789abcdef".charAt(i);
  }

  public static String readFile(File file)
  {
    byte[] buffer = readFileBytes(file);
    String rawContents = new String(buffer);
    String normalizedContents = rawContents.replace("\r\n", "\n");
    return normalizedContents;
  }

  public static String readFile(String pathName)
  {
    File file = new File(pathName);
    String contents = readFile(file);
    return contents;
  }

  public static byte[] readFileBytes(File file)
  {
    try
    {
      int length = getIntegerLength(file);
      byte[] buffer = new byte[length];
      InputStream inputStream = new FileInputStream(file);
      try
      {
        inputStream.read(buffer);
      }
      finally
      {
        inputStream.close();
      }
      return buffer;
    }
    catch (FileNotFoundException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static byte[] readFileBytes(String pathName)
  {
    return readFileBytes(new File(pathName));
  }

  public static String toHex(byte[] bytes)
  {
    StringBuilder s = new StringBuilder(bytes.length * 2);
    for (int i = 0; i < bytes.length; i++)
    {
      s.append(lookup((bytes[i] >>> 4) & 0xf));
      s.append(lookup((bytes[i]) & 0xf));
    }
    return s.toString();
  }

  public static void writeFile(File file, byte[] contents)
  {
    try
    {
      OutputStream outputStream = new FileOutputStream(file);
      try
      {
        outputStream.write(contents);
      }
      finally
      {
        outputStream.close();
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
  }

  public static void writeFile(String pathName, byte[] contents)
  {
    File file = new File(pathName);
    writeFile(file, contents);
  }

}
