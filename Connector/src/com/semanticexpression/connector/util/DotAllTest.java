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

public class DotAllTest
{
  public static void main(String[] args)
  {
    String x = "Here           is a <multiline\r\ntag> Did it get             stripped?";
    System.out.println(x.replaceAll("(?s)(\\s*\\<.+?\\>\\s*)|(\\s+)", " ")); // note the order of alternation is important
    System.out.println(x.replaceAll("\\<.+?\\>", ""));
    System.out.println(x.replaceAll("(\\s*\\<(.|\r|\n)+?\\>\\s*)|(\\s+)", " "));
  }
}
