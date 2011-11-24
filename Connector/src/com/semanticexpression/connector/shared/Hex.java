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

package com.semanticexpression.connector.shared;

public class Hex
{
  public static final char[] lookup = new char[] {
      '0',
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      'a',
      'b',
      'c',
      'd',
      'e',
      'f'
  };

  public static long fromHex(String buf)
  {
    long value = 0;
    for (int i = 0; i <= 18; i++)
    {
      char c = buf.charAt(i);
      if ((i + 1) % 5 == 0)
      {
        if (c != '-')
        {
          throw new NumberFormatException();
        }
      }
      else
      {
        int nibble = lookup(c);
        value = (value << 4) | nibble;
      }
    }
    return value;
  }

  private static int lookup(int nibble)
  {
    for (int i = 0; i < Hex.lookup.length; i++)
    {
      if (nibble == Hex.lookup[i])
      {
        return i;
      }
    }
    throw new NumberFormatException();
  }

  public static String toHex(long value)
  {
    char[] buf = new char[19];
    for (int i = 18; i >= 0; i--)
    {
      if ((i + 1) % 5 == 0)
      {
        buf[i] = '-';
      }
      else
      {
        int nibble = (int)(value & 0x0f);
        buf[i] = Hex.lookup[nibble];
        value >>>= 4;
      }
    }
    return new String(buf);
  }

}
