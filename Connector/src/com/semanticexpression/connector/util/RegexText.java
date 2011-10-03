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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexText
{
  public static void main(String[] args)
  {
    new RegexText().test();
  }

  private void test()
  {
    // String regexp = "((\\*)|([a-zA-Z]{1,}[a-zA-Z0-9_\\.]{0,}))(\\[([0-9]{1,9})\\]){0,1}";
    //String regexp = "^.*\\([0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}\\).*$";
    //String input = "/content?time=1313828970548&id=0867-53e8-e9e3-899f";
    String regexp = "^.*([0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}).*$";
    String input = "/content?time=1313828970548&id=0867-53e8-e9e3-899f";
    Pattern pattern = Pattern.compile(regexp);
    Matcher matcher = pattern.matcher(input);
    if (matcher.matches())
    {
      System.out.println("Match");
      int groupCount = matcher.groupCount();
      for (int i = 0; i <= groupCount; i++)
      {
        String group = matcher.group(i);
        System.out.println(i + "=" + group);
      }
    }
    else
    {
      System.out.println("No match");
    }
  }

}
