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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.shared.exception.ReadOnlyUpdateException;

public class Association extends BaseModel implements Tracker, Copyable
{
  protected Id id;
  protected boolean isReadOnly;
  protected transient Map<String, Object> snapshot;
  protected transient Map<String, Object> transients;

  public Association()
  {
    allowNestedValues = false;
  }

  public Association(Id id)
  {
    this();
    this.id = id;
  }

  public Association(Id id, boolean isTrackChanges)
  {
    this();
    this.id = id;
    setTrackChanges(isTrackChanges);
  }

  public void assignFrom(Association that)
  {
    System.out.println("assignFrom: this=" + this);
    System.out.println("assignFrom: that=" + that);

    if (that.map != null)
    {
      for (Entry<String, Object> entry : that.map.entrySet())
      {
        String name = entry.getKey();
        Object value = entry.getValue();
        super.set(name, value); // override read-only
      }
    }

    if (that.transients != null)
    {
      for (Entry<String, Object> entry : that.transients.entrySet())
      {
        String name = entry.getKey();
        Object value = entry.getValue();
        setTransient(name, value);
      }
    }
  }

  @Override
  public void commit()
  {
    if (snapshot != null)
    {
      snapshot = getProperties();
    }

    if (map != null)
    {
      for (Object value : map.values())
      {
        if (value instanceof Tracker)
        {
          ((Tracker)value).commit();
        }
      }
    }
  }

  protected void copy(Association newAssociation, boolean isTrackChanges)
  {
    if (map != null)
    {
      for (Entry<String, Object> entry : map.entrySet())
      {
        Object newValue;
        Object oldValue = entry.getValue();
        if (oldValue instanceof Copyable)
        {
          newValue = ((Copyable)oldValue).copy(isTrackChanges);
        }
        else
        {
          newValue = oldValue;
        }
        String name = entry.getKey();
        newAssociation.set(name, newValue);
      }
    }

    if (transients != null)
    {
      newAssociation.transients = new HashMap<String, Object>(transients);
    }
    
    newAssociation.set(Keys.DERIVED_FROM, id);
  }

  @Override
  public Association copy(boolean isTrackChanges)
  {
    Association newAssociation = new Association(IdManager.createIdentifier(), isTrackChanges);
    copy(newAssociation, isTrackChanges);
    return newAssociation;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> X get(String name)
  {
    X value = null;
    if (transients != null)
    {
      value = (X)transients.get(name);
    }
    if (value == null)
    {
      value = super.get(name);
    }
    return value;
  }

  public Id getId()
  {
    return id;
  }

  @SuppressWarnings("unchecked")
  public <X> X getOriginalValue(String name)
  {
    return (X)snapshot.get(name);
  }

  @Override
  public boolean isChanged()
  {
    if (isReadOnly == false && map != null)
    {
      for (Entry<String, Object> entry : map.entrySet())
      {
        Object newValue = entry.getValue();
        if (newValue instanceof Tracker && ((Tracker)newValue).isChanged())
        {
          String name = entry.getKey();
          System.out.println("Association.isChanged: name=" + name + ", value=" + newValue);
          return true;
        }
        String name = entry.getKey();
        Object oldValue = getOriginalValue(name);
        if (isChanged(newValue, oldValue))
        {
          System.out.println("Association.isChanged: name=" + name + ", oldValue=" + oldValue);
          System.out.println("Association.isChanged: name=" + name + ", newValue=" + newValue);
          return true;
        }
      }
    }

    return false;
  }

  protected boolean isChanged(Object newValue, Object oldValue)
  {
    return !Utility.equalsWithNull(newValue, oldValue);
  }

  public boolean isReadOnly()
  {
    return isReadOnly;
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
      map = null;
      setProperties(snapshot);
    }

    if (map != null)
    {
      for (Object value : map.values())
      {
        if (value instanceof Tracker)
        {
          ((Tracker)value).rollback();
        }
      }
    }
  }

  @Override
  public <X> X set(String name, X value)
  {
    if (isReadOnly)
    {
      throw new ReadOnlyUpdateException();
    }

    return super.set(name, value);
  }

  public void setId(Id id)
  {
    this.id = id;
  }

  public void setReadOnly(boolean isReadOnly)
  {
    this.isReadOnly = isReadOnly;

    if (map != null)
    {
      for (Object value : map.values())
      {
        if (value instanceof Tracker)
        {
          ((Tracker)value).setReadOnly(isReadOnly);
        }
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
        snapshot = getProperties(); // returns an updateable FastMap
      }
    }
    else
    {
      snapshot = null;
    }

    if (map != null)
    {
      for (Object value : map.values())
      {
        if (value instanceof Tracker)
        {
          ((Tracker)value).setTrackChanges(isTrackChanges);
        }
      }
    }
  }

  public void setTransient(String name, Object value)
  {
    if (transients == null)
    {
      transients = new HashMap<String, Object>();
    }
    Object oldValue = transients.put(name, value);
    notifyPropertyChanged(name, value, oldValue);
  }

  @Override
  public String toString()
  {
    return "[id=" + id + ", map=" + map + (transients == null ? "" : ", transients=" + transients) + (isReadOnly == false ? "" : ", isReadOnly=" + isReadOnly) + "]";
  }

}