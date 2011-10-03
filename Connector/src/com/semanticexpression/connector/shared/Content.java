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

import java.io.Serializable;
import java.util.Map.Entry;


public class Content extends Association implements Serializable, Copyable
{
  private static final long serialVersionUID = 1L;
  
  protected Content()
  {
  }

  public Content(Id id)
  {
    super(id);
  }

  public Content(Id id, boolean isTrackChanges)
  {
    this(id);
    setTrackChanges(isTrackChanges);
  }

  @Override
  public Content copy(boolean isTrackChanges)
  {
    Content newContent = new Content(IdManager.createIdentifier(), isTrackChanges);
    copy(newContent, isTrackChanges);
    return newContent;
  }

  public Content getChanges()
  {
    Content contentEntity = null;

    for (Entry<String, Object> entry : map.entrySet())
    {
      String name = entry.getKey();
      Object newValue = entry.getValue();
      if (newValue instanceof Tracker && ((Tracker)newValue).isChanged())
      {
        if (contentEntity == null)
        {
          System.out.println("Content.getChanges: id=" + id);
          contentEntity = new Content(id);
        }

        System.out.println("Content.getChanges: name=" + name + ", value=" + newValue);
        contentEntity.set(name, newValue);
      }
      else
      {
        Object oldValue = getOriginalValue(name);
        if (isChanged(newValue, oldValue))
        {
          if (contentEntity == null)
          {
            System.out.println("Content.getChanges: id=" + id);
            contentEntity = new Content(id);
          }

          System.out.println("Content.getChanges: name=" + name + ", oldValue=" + oldValue);
          System.out.println("Content.getChanges: name=" + name + ", newValue=" + newValue);
          contentEntity.set(name, newValue);
        }
      }
    }

    return contentEntity;
  }

}