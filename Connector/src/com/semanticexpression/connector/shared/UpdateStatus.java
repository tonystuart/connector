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
import java.util.Date;

public class UpdateStatus extends Status implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public UpdateStatus()
  {
  }

  public UpdateStatus(Date date, Id contentId, String userName, String title, String action)
  {
    setCreatedAt(date);
    setContentId(contentId);
    setUserName(userName);
    setTitle(title);
    set(Keys.ACTION, action);
  }

  public long getCreatedAt()
  {
    return get(Keys.CREATED_AT);
  }

  public Id getContentId()
  {
    return get(Keys.CONTENT_ID);
  }

  public String getTitle()
  {
    return get(Keys.TITLE);
  }

  public String getUserName()
  {
    return get(Keys.CREATED_BY);
  }

  public void setCreatedAt(Date date)
  {
    set(Keys.CREATED_AT, date);
  }

  public void setContentId(Id contentId)
  {
    set(Keys.CONTENT_ID, contentId);
  }

  public void setTitle(String title)
  {
    set(Keys.TITLE, title);
  }

  public void setUserName(String userName)
  {
    set(Keys.CREATED_BY, userName);
  }

  @Override
  public String toString()
  {
    return "ContentStatus [properties=" + getProperties() + "]";
  }

}
