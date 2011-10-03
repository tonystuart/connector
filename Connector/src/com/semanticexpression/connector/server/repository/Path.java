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

public class Path
{
  private int componentCount;
  private PathComponent[] pathComponents;
  private String name;

  public Path(String name)
  {
    this.name = name;

    String[] componentNames = name.split("/");
    componentCount = componentNames.length;
    pathComponents = new PathComponent[componentCount];

    for (int i = 0; i < componentCount; i++)
    {
      pathComponents[i] = new PathComponent(componentNames[i]);
    }
  }

  public int getComponentCount()
  {
    return componentCount;
  }

  public PathComponent[] getComponents()
  {
    return pathComponents;
  }

  public String getName()
  {
    return name;
  }
}
