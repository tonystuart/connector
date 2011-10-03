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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.semanticexpression.connector.shared.Id;

public class Parameters
{
  private Map<String, String> map = new HashMap<String, String>();

  public Parameters(String queryString) throws ServletException
  {
    map = parseQueryString(queryString);
  }

  public Date getDate(String name, Date defaultValue) throws ServletException
  {
    Date date;
    String dateString = map.get(name);
    if (dateString == null)
    {
      date = defaultValue;
    }
    else
    {
      date = parseDate(dateString);
    }
    return date;
  }

  public Id getId(String name) throws ServletException
  {
    String idString = map.get(name);
    Id id = parseId(idString);
    return id;
  }

  public String getString(String name, String defaultValue)
  {
    String value = map.get(name);
    if (value == null)
    {
      value = defaultValue;
    }
    return value;
  }

  private Date parseDate(String dateString) throws ServletException
  {
    try
    {
      long dateLong = Long.parseLong(dateString);
      Date date = new Date(dateLong);
      return date;
    }
    catch (NumberFormatException e)
    {
      throw new ServletException("Cannot convert " + dateString + " to date");
    }
  }

  private Id parseId(String idString) throws ServletException
  {
    Id id = new Id();
    if (!id.parseString(idString))
    {
      throw new ServletException("Cannot convert " + idString + " to id");
    }
    return id;
  }

  private Map<String, String> parseQueryString(String queryString) throws ServletException
  {
    Map<String, String> parameters = new HashMap<String, String>();

    String[] options = queryString.split("&");
    for (String option : options)
    {
      String[] nameValuePair = option.split("=");
      if (nameValuePair.length != 2)
      {
        throw new ServletException("Invalid query string: " + queryString);
      }
      parameters.put(nameValuePair[0], nameValuePair[1]);
    }

    return parameters;
  }

}
