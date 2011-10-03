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

public final class SafeHtml
{
  public static String escape(String text)
  {
    if (text == null)
    {
      return null;
    }

    StringBuilder safeHtml = null;

    int length = text.length();

    for (int i = 0; i < length; i++)
    {
      char c = text.charAt(i);
      if (c == '<')
      {
        if (safeHtml == null)
        {
          safeHtml = new StringBuilder(text.substring(0, i));
        }
        safeHtml.append("&lt;");
      }
      else if (c == '>')
      {
        if (safeHtml == null)
        {
          safeHtml = new StringBuilder(text.substring(0, i));
        }
        safeHtml.append("&gt;");
      }
      else if (c == '&')
      {
        if (safeHtml == null)
        {
          safeHtml = new StringBuilder(text.substring(0, i));
        }
        safeHtml.append("&amp;");
      }
      else if (safeHtml != null)
      {
        safeHtml.append(c);
      }
    }

    return safeHtml == null ? text : safeHtml.toString();
  }

  // &amp;&lt; -> &amp;amp;&amp;lt;
  // &amp;amp;&amp;lt; -> &amp;&lt;

  public static String unescape(String safeHtml)
  {
    if (safeHtml == null)
    {
      return null;
    }

    StringBuilder text = null;

    int length = safeHtml.length();

    for (int i = 0; i < length; i++)
    {
      char c = safeHtml.charAt(i);
      if (safeHtml.startsWith("&lt;", i))
      {
        if (text == null)
        {
          text = new StringBuilder(safeHtml.substring(0, i));
        }
        text.append("<");
        i += 3;
      }
      else if (safeHtml.startsWith("&gt;", i))
      {
        if (text == null)
        {
          text = new StringBuilder(safeHtml.substring(0, i));
        }
        text.append(">");
        i += 3;
      }
      else if (safeHtml.startsWith("&amp;", i))
      {
        if (text == null)
        {
          text = new StringBuilder(safeHtml.substring(0, i));
        }
        text.append("&");
        i += 4;
      }
      else if (text != null)
      {
        text.append(c);
      }
    }

    return text == null ? safeHtml : text.toString();
  }

}
