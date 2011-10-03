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

public class StringAppender
{
  private String delimiter;
  private int maximumLength;
  private StringBuilder stringBuilder = new StringBuilder();

  public StringAppender(String delimiter)
  {
    this(delimiter, Integer.MAX_VALUE);
  }

  public StringAppender(String delimiter, int maximumLength)
  {
    this.delimiter = delimiter;
    this.maximumLength = maximumLength;
  }

  public void append(String text)
  {
    boolean isDelimiterRequired = stringBuilder.length() > 0;
    int optionalDelimiterLength = isDelimiterRequired ? delimiter.length() : 0;
    int substringLength = Math.min(text.length(), maximumLength - (stringBuilder.length() + optionalDelimiterLength));
    if (substringLength > 0)
    {
      if (isDelimiterRequired)
      {
        stringBuilder.append(delimiter);
      }
      stringBuilder.append(text.substring(0, substringLength)); // substring returns "this" if possible
    }
  }

  public int length()
  {
    return stringBuilder.length();
  }

  public String toString()
  {
    return stringBuilder.toString();
  }

  public boolean isFull()
  {
    return stringBuilder.length() >= maximumLength;
  }

}
