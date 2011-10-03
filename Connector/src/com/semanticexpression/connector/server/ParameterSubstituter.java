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

import java.util.Map;

public class ParameterSubstituter
{
  public String substitute(Map<String, Object> parameters, String text)
  {
    State state = State.INITIAL;

    StringBuilder p = null;
    StringBuilder s = null;
    int parameterOffset = 0;

    int length = text.length();

    for (int i = 0; i < length; i++)
    {
      char c = text.charAt(i);
      switch (state)
      {
        case INITIAL:
          if (c == '{')
          {
            parameterOffset = i;
            state = State.OPEN;
          }
          else if (s != null)
          {
            s.append(c);
          }
          break;
        case OPEN:
          if (c == '{')
          {
            p = new StringBuilder();
            state = State.PARAMETER;
          }
          else
          {
            if (s != null)
            {
              s.append('{');
              s.append(c);
            }
            state = State.INITIAL;
          }
          break;
        case PARAMETER:
          if (c == '}')
          {
            state = State.CLOSE;
          }
          else
          {
            p.append(c);
          }
          break;
        case CLOSE:
          if (c == '}')
          {
            String name = p.toString();
            Object value = parameters.get(name);
            if (value != null)
            {
              if (s == null)
              {
                s = new StringBuilder(length);
                s.append(text.substring(0, parameterOffset));
              }
              s.append(value.toString().trim());
            }
          }
          else
          {
            if (s != null)
            {
              s.append("{{");
              s.append(p);
              s.append('}');
              s.append(c);
            }
          }
          p = null;
          state = State.INITIAL;
          break;

      }
    }

    String result;

    if (s == null)
    {
      result = text;
    }
    else
    {
      if (p != null)
      {
        s.append(p);
      }
      result = s.toString();
    }

    return result;
  }

  public enum State
  {
    CLOSE, INITIAL, OPEN, PARAMETER

  }
}
