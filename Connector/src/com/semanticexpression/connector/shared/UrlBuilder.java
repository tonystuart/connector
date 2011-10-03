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

import java.util.Date;

public abstract class UrlBuilder
{
  private int parameterCount;
  private StringBuilder stringBuilder = new StringBuilder();

  public UrlBuilder(String base)
  {
    stringBuilder.append(base);
  }
  public void addParameter(String name, Date value)
  {
    if (value != null)
    {
      addParameter(name, value.getTime());
    }
  }
 
  public void addParameter(String name, Id value)
  {
    addParameter(name, value.formatString());
  }

  private void addParameter(String name, long value)
  {
    addParameter(name, Long.toString(value));
  }

  public void addParameter(String name, String value)
  {
    if (parameterCount++ == 0)
    {
      stringBuilder.append("?");
    }
    else
    {
      stringBuilder.append("&");
    }
    stringBuilder.append(name);
    stringBuilder.append("=");
    stringBuilder.append(value);
  }

  public void addQueryString(String queryString)
  {
    if (parameterCount++ > 0)
    {
      throw new IllegalStateException("Cannot add query after parameters");
    }
    stringBuilder.append("?");
    stringBuilder.append(queryString);
  }

  public void addTitle(String title)
  {
    String normalizedTitle = normalizeTitle(title);
    addParameter(UrlConstants.PARAMETER_TITLE, normalizedTitle);
  }

  /**
   * Note: Handles ISO8859-1 7 bit characters only.
   */
  private String normalizeTitle(String title)
  {
    char lastChar = 0;
    StringBuilder s = new StringBuilder();
    int titleLength = title == null ? 0 : title.length();
    for (int i = 0; i < titleLength; i++)
    {
      char c = title.charAt(i);
      if ('A' <= c && c <= 'Z')
      {
        s.append((char)(c + ('a' - 'A')));
      }
      else if ('a' <= c && c <= 'z')
      {
        s.append(c);
      }
      else if ('0' <= c && c <= '9')
      {
        s.append(c);
      }
      else if (lastChar != '-')
      {
        s.append('-');
      }
      lastChar = c;
    }
    return s.toString();
  }

  public String toString()
  {
    return stringBuilder.toString();
  }
}
