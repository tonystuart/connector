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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WikipediaDiff
{
  public static <E> List<E> LongestCommonSubsequence(E[] s1, E[] s2)
  {
    int[][] num = new int[s1.length + 1][s2.length + 1]; //2D array, initialized to 0

    //Actual algorithm
    for (int i = 1; i <= s1.length; i++)
      for (int j = 1; j <= s2.length; j++)
        if (s1[i - 1].equals(s2[j - 1]))
          num[i][j] = 1 + num[i - 1][j - 1];
        else
          num[i][j] = Math.max(num[i - 1][j], num[i][j - 1]);

    System.out.println("length of LCS = " + num[s1.length][s2.length]);

    int s1position = s1.length, s2position = s2.length;
    List<E> result = new LinkedList<E>();

    while (s1position != 0 && s2position != 0)
    {
      if (s1[s1position - 1].equals(s2[s2position - 1]))
      {
        result.add(s1[s1position - 1]);
        s1position--;
        s2position--;
      }
      else if (num[s1position][s2position - 1] >= num[s1position - 1][s2position])
      {
        s2position--;
      }
      else
      {
        s1position--;
      }
    }
    Collections.reverse(result);
    return result;
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
