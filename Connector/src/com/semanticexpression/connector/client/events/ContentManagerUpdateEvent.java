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

package com.semanticexpression.connector.client.events;

import java.util.List;

import com.semanticexpression.connector.client.wiring.EventNotification;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Id;

public class ContentManagerUpdateEvent implements EventNotification
{
  private List<Content> contentList;

  public ContentManagerUpdateEvent(List<Content> contentList)
  {
    this.contentList = contentList;
  }

  public List<Content> getContentList()
  {
    return contentList;
  }

  public Id getContentId()
  {
    // contentList only contains more than one item if content is a Document that has parts
    return contentList.get(0).getId();
  }
}
