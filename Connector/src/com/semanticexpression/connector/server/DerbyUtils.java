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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.semanticexpression.connector.shared.Hex;

public class DerbyUtils
{
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

  public static String d(long millis)
  {
    return DATE_FORMAT.format(new Date(millis));
  }

  public static long fh(String buf)
  {
    return Hex.fromHex(buf);
  }

  public static String ra(String string, String regularExpression, String replacement)
  {
    return string.replaceAll(regularExpression, replacement);
  }

  public static String th(long value)
  {
    return Hex.toHex(value);
  }
}
