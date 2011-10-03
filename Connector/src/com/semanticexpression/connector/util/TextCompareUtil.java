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
import java.io.FileInputStream;
import java.io.IOException;

import com.semanticexpression.connector.shared.TextCompare;

public class TextCompareUtil
{
  public static void main(String[] args)
  {
    if (args.length != 2)
    {
      System.err.println("Usage: java TextCompare <old> <new>");
      System.exit(1);
    }

    String[] x = read(args[0]);
    String[] y = read(args[1]);

    TextCompare textCompare = new TextCompare(0);
    String result = textCompare.compare(x, y, null);
    System.out.println(result);
  }

  protected static String[] read(String fileName)
  {
    try
    {
      File file = new File(fileName);
      int length = (int)file.length();
      byte[] buffer = new byte[length];
      FileInputStream fileInputStream = new FileInputStream(file);
      try
      {
        fileInputStream.read(buffer);
        String contents = new String(buffer);
        String[] tokens = contents.split("\\s+");
        return tokens;
      }
      finally
      {
        fileInputStream.close();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}
