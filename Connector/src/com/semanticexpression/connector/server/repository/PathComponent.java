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

package com.semanticexpression.connector.server.repository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathComponent
{
  private static final Pattern PATTERN = Pattern.compile("(([0-9]{1,9})|(\\*)|([a-zA-Z]{1,}[a-zA-Z0-9_\\.]{0,}))(\\[([0-9]{1,9})\\]){0,1}");
  private ComponentType componentType;

  private int id;
  private int index;
  private boolean isIndexed;
  private String name;

  public PathComponent(String value)
  {
    Matcher matcher = PATTERN.matcher(value);
    if (!matcher.matches())
    {
      throw new IllegalArgumentException("value=" + value + ", pattern=" + PATTERN);
    }

    name = matcher.group(1);

    String idString = matcher.group(2);
    if (idString != null)
    {
      componentType = ComponentType.ENTITY_ID;
      id = Integer.parseInt(idString);
    }
    else if (matcher.group(3) != null)
    {
      componentType = ComponentType.WILDCARD;
    }
    else
    {
      componentType = ComponentType.PROPERTY_NAME;
    }

    String indexString = matcher.group(6);
    if (indexString != null)
    {
      isIndexed = true;
      index = Integer.parseInt(indexString);
    }
  }

  public ComponentType getComponentType()
  {
    return componentType;
  }

  public int getEntityId()
  {
    return id;
  }

  public int getIndex()
  {
    return index;
  }

  public String getName()
  {
    return name;
  }

  public boolean isIndexed()
  {
    return isIndexed;
  }

  public enum ComponentType
  {
    ENTITY_ID, PROPERTY_NAME, WILDCARD
  }

}
