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

import com.semanticexpression.connector.shared.Id;

public class IdHexTest
{
  private static final long[] test = new long[] {
    0,
    1,
    2,
    0x123456789abcdefL,
    0xf,
    0x10,
    0xff,
    0x100,
    0xffff,
    0x10000,
    0xfffff,
    0x100000,
    0x7fffffff,
    0x80000000L,
    0x7fffffffffffffffL,
    0x8000000000000000L,
    Long.MAX_VALUE,
    Long.MIN_VALUE,
    -1,
    -2,
  };

  public static void main(String[] args)
  {
    for (int i = 0; i < test.length; i++)
    {
      long t = test[i];
      String s = new Id(t).formatString();
      System.out.println(s);
      Id id = new Id(s);
      if (id.getId() != t)
      {
        System.out.println("fail");
      }
    }
  }
}
