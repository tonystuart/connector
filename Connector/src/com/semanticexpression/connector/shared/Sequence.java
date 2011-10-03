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

import java.util.Iterator;
import java.util.LinkedList;

public class Sequence<T> extends LinkedList<T> implements Tracker, Copyable
{
  private static final long serialVersionUID = 1L;
  
  private transient LinkedList<T> snapshot;

  public Sequence()
  {
  }

  public Sequence(boolean isTrackChanges)
  {
    setTrackChanges(isTrackChanges);
  }

  @Override
  public void commit()
  {
    if (snapshot != null)
    {
      snapshot = new LinkedList<T>(this);
    }

    for (T value : this)
    {
      if (value instanceof Tracker)
      {
        ((Tracker)value).commit();
      }
    }
  }

  @Override
  public boolean isChanged()
  {
    if (snapshot != null)
    {
      Iterator<T> newIterator = iterator();
      Iterator<T> oldIterator = snapshot.iterator();

      while (newIterator.hasNext())
      {
        if (!oldIterator.hasNext())
        {
          return true;
        }

        T newValue = newIterator.next();
        T oldValue = oldIterator.next();

        if (newValue != oldValue)
        {
          return true;
        }

        if (newValue instanceof Tracker && ((Tracker)newValue).isChanged())
        {
          return true;
        }
      }

      if (oldIterator.hasNext())
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean isTrackChanges()
  {
    return snapshot != null;
  }

  @Override
  public void rollback()
  {
    if (snapshot != null)
    {
      clear();
      addAll(snapshot);
    }

    for (T value : this)
    {
      if (value instanceof Tracker)
      {
        ((Tracker)value).rollback();
      }
    }
  }

  @Override
  public void setReadOnly(boolean isReadOnly)
  {
    for (Object value : this)
    {
      if (value instanceof Tracker)
      {
        ((Tracker)value).setReadOnly(isReadOnly);
      }
    }
  }

  @Override
  public void setTrackChanges(boolean isTrackChanges)
  {
    if (isTrackChanges)
    {
      if (snapshot == null)
      {
        snapshot = new LinkedList<T>(this);
      }
    }
    else
    {
      snapshot = null;
    }

    for (Object value : this)
    {
      if (value instanceof Tracker)
      {
        ((Tracker)value).setTrackChanges(isTrackChanges);
      }
    }
  }

  @Override
  public Object copy(boolean isTrackChanges)
  {
    Sequence<T> newSequence = new Sequence<T>(isTrackChanges);
    copy(newSequence, isTrackChanges);
    return newSequence;
  }

  @SuppressWarnings("unchecked")
  protected void copy(Sequence<T> newSequence, boolean isTrackChanges)
  {
    for (T oldValue : this)
    {
      T newValue;
      if (oldValue instanceof Copyable)
      {
        newValue = (T)((Copyable)oldValue).copy(isTrackChanges);
      }
      else
      {
        newValue = oldValue;
      }
      newSequence.add(newValue);
    }
  }

}
