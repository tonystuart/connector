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

package com.semanticexpression.connector.client.frame.editor.text;

import com.semanticexpression.connector.client.Utility;


public class HtmlNormalizer
{

  public String normalize(String html)
  {
    int length = html.length();
    State state = State.INITIAL;
    StringBuilder s = new StringBuilder();
    char quote = 0;
   
    for (int i = 0; i < length; i++)
    {
      char c = html.charAt(i);
      switch (state)
      {
        case INITIAL:
          s.append(c);
          if (c == '<')
          {
            state = State.LT;
          }
          break;
        case LT:
          if (Utility.isUpperCase(c))
          {
            c = Utility.toLowerCase(c);
          }
          s.append(c);
          state = State.TAG_NAME;
          break;
        case TAG_NAME:
          if (c == '>')
          {
            state = State.INITIAL;
          }
          else if (Utility.isUpperCase(c))
          {
            c = Utility.toLowerCase(c);
          }
          else if (Utility.isWhitespace(c))
          {
            state = State.TAG_WS;
          }
          s.append(c);
          break;
        case TAG_WS:
          s.append(c);
          if (c == '>')
          {
            state = State.INITIAL;
          }
          else if (!Utility.isWhitespace(c))
          {
            state = State.ATTRIBUTE_NAME;
          }
          break;
        case ATTRIBUTE_NAME:
          s.append(c);
          if (c == '=')
          {
            state = State.ATTRIBUTE_EQ;
          }
          break;
        case ATTRIBUTE_EQ:
          if (c == '\'' || c == '\"')
          {
            quote = c;
            state = State.ATTRIBUTE_QUOTED_VALUE;
          }
          else
          {
            s.append('"');
            state = State.ATTRIBUTE_UNQUOTED_VALUE;
          }
          s.append(c);
          break;
        case ATTRIBUTE_QUOTED_VALUE:
          s.append(c);
          if (c == quote)
          {
            state = State.TAG_WS;
          }
          break;
        case ATTRIBUTE_UNQUOTED_VALUE:
          if (c == '>')
          {
            s.append('"');
            state = State.INITIAL;
          }
          else if (Utility.isWhitespace(c))
          {
            s.append('"');
            state = State.TAG_WS;
          }
          s.append(c);
      }
    }
    return s.toString();
  }
  
  public enum State
  {
    INITIAL, LT, TAG_NAME, TAG_WS, ATTRIBUTE_NAME, ATTRIBUTE_EQ, ATTRIBUTE_QUOTED_VALUE, ATTRIBUTE_UNQUOTED_VALUE
  }

}
