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

public class Properties
{
  public static Properties getProperties(Content content)
  {
    Sequence<Association> p = content.get(Keys.PROPERTIES);
    if (p == null)
    {
      p = new Sequence<Association>(content.isTrackChanges());
      content.set(Keys.PROPERTIES, p);
    }
    Properties properties = new Properties(p);
    return properties;
  }
 
  private boolean isTrackChanges;
  private Sequence<Association> properties;

  public Properties(boolean isTrackChanges)
  {
    this.isTrackChanges = isTrackChanges;
    this.properties = new Sequence<Association>(isTrackChanges);
  }

  public Properties(Sequence<Association> properties)
  {
    this.properties = properties == null ? new Sequence<Association>() : properties;
  }

  private Association findProperty(String name)
  {
    for (Association property : properties)
    {
      if (property.get(Keys.NAME).equals(name))
      {
        return property;
      }
    }
    return null;
  }

  public <X> X get(String name, X defaultValue)
  {
    X value;
    Association property = findProperty(name);
    if (property == null)
    {
      value = defaultValue;
    }
    else
    {
      value = property.get(Keys.VALUE, defaultValue);
    }
    return value;
  }

  public Sequence<Association> getProperties()
  {
    return properties;
  }

  public void set(String name, Object value)
  {
    Association property = findProperty(name);
    if (property == null)
    {
      property = new Association(IdManager.createIdentifier(), isTrackChanges);
      property.set(Keys.NAME, name);
      properties.add(property);
    }
    property.set(Keys.VALUE, value);
  }

  public int size()
  {
    return properties.size();
  }
}
