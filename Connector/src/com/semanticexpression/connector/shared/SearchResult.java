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

public class SearchResult extends Content implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public SearchResult()
  {
  }

  public SearchResult(Id contentId)
  {
    super(contentId);
  }

  @Override
  public boolean isChanged()
  {
    return false;
  }

  @Override
  public String toString()
  {
    return "SearchResult [contentId=" + id + ", properties=" + getProperties() + "]";
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> X get(String name)
  {
    X value;
    if (Keys.CONTENT_ID.equals(name))
    {
      value = (X)getId();
    }
    else
    {
      value = super.get(name);
    }
    return value;
  }

}