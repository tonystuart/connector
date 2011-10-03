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

public final class Id implements Serializable, Comparable<Id>
{
  private long id;

  public Id()
  {
  }

  public Id(long id)
  {
    this.id = id;
  }

  public Id(String value)
  {
    if (!parseString(value))
    {
      throw new IllegalArgumentException("Invalid format: " + value);
    }
  }

  @Override
  public int compareTo(Id that)
  {
    return this.id < that.id ? -1 : this.id > that.id ? +1 : 0; // NB: id's are signed
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (!(obj instanceof Id))
    {
      return false;
    }
    Id other = (Id)obj;
    if (id != other.id)
    {
      return false;
    }
    return true;
  }

  public String formatString()
  {
    return Hex.toHex(id);
  }

  public boolean parseString(String buf)
  {
    boolean isValid;
    try
    {
      id = Hex.fromHex(buf);
      isValid = true;
    }
    catch (Exception e)
    {
      isValid = false;
    }
    return isValid;
  }

  public long getId()
  {
    return id;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (int)(prime * result + id);
    return result;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  @Override
  public String toString()
  {
    return formatString();
  }
}