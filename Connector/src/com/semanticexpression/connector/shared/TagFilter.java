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

import com.semanticexpression.connector.shared.TagConstants.TagVisibility;

public class TagFilter implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  private boolean isAuthor;
  private boolean isContent;
  private boolean isInclude;
  private boolean isMyPrivate;
  private boolean isMyPublic;
  private boolean isOtherPublic;
  private boolean isSemantic;
  private String name;

  public TagFilter()
  {
  }

  public TagFilter(Association tag, TagFilterType tagFilterType)
  {
    TagVisibility tagVisibility = tag.get(Keys.TAG_VISIBILITY);

    name = tag.get(Keys.NAME);
    isInclude = true;
    isAuthor = tagFilterType == TagFilterType.AUTHOR;
    isContent = tagFilterType == TagFilterType.CONTENT;
    isSemantic = tagFilterType == TagFilterType.SEMANTIC;
    isMyPrivate = tagVisibility == TagVisibility.MY_PRIVATE;
    isMyPublic = tagVisibility == TagVisibility.MY_PUBLIC;
    isOtherPublic = tagVisibility == TagVisibility.OTHER_PUBLIC;
  }

  public TagFilter(String tagName, boolean isPrivate)
  {
    name = tagName;
    isInclude = true;
    isAuthor = true;
    isContent = true;
    isSemantic = true;
    isMyPrivate = isPrivate;
    isMyPublic = !isPrivate;
    isOtherPublic = !isPrivate;
  }

  public String getName()
  {
    return name;
  }

  public boolean isAuthor()
  {
    return isAuthor;
  }

  public boolean isContent()
  {
    return isContent;
  }

  public boolean isInclude()
  {
    return isInclude;
  }

  public boolean isMyPrivate()
  {
    return isMyPrivate;
  }

  public boolean isMyPublic()
  {
    return isMyPublic;
  }

  public boolean isOtherPublic()
  {
    return isOtherPublic;
  }

  public boolean isSemantic()
  {
    return isSemantic;
  }

  public void setAuthor(boolean isAuthor)
  {
    this.isAuthor = isAuthor;
  }

  public void setContent(boolean isContent)
  {
    this.isContent = isContent;
  }

  public void setInclude(boolean isInclude)
  {
    this.isInclude = isInclude;
  }

  public void setMyPrivate(boolean isMyPrivate)
  {
    this.isMyPrivate = isMyPrivate;
  }

  public void setMyPublic(boolean isMyPublic)
  {
    this.isMyPublic = isMyPublic;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public void setOtherPublic(boolean isOtherPublic)
  {
    this.isOtherPublic = isOtherPublic;
  }

  public void setSemantic(boolean isSemantic)
  {
    this.isSemantic = isSemantic;
  }

  public enum TagFilterType
  {
    AUTHOR, CONTENT, SEMANTIC
  }
}
