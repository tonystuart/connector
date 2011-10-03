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
import java.util.List;

public final class SearchRequest implements Serializable
{
  private Date fromDate;
  private boolean isAuthorTags;
  private boolean isCaption;
  private boolean isChart;
  private boolean isContainingDocuments;
  private boolean isContent;
  private boolean isContentTags;
  private boolean isDateCreated;
  private boolean isDateModified;
  private boolean isDatePublished;
  private boolean isDateViewed;
  private boolean isDocument;
  private boolean isImage;
  private boolean isMyPrivateTags;
  private boolean isMyPublicTags;
  private boolean isOtherPublicTags;
  private boolean isPublished;
  private boolean isSemanticTags;
  private boolean isStyle;
  private boolean isTable;
  private boolean isText;
  private boolean isTitle;
  private boolean isUnpublished;
  private boolean isWorkflow;
  private List<TagFilter> tagFilters;
  private String terms;
  private Date toDate;

  public SearchRequest()
  {
  }

  public Date getFromDate()
  {
    return fromDate;
  }

  public List<TagFilter> getTagFilters()
  {
    return tagFilters;
  }

  public String getTerms()
  {
    return terms;
  }

  public Date getToDate()
  {
    return toDate;
  }

  public boolean isAuthorTags()
  {
    return isAuthorTags;
  }

  public boolean isCaption()
  {
    return isCaption;
  }

  public boolean isChart()
  {
    return isChart;
  }

  public boolean isContainingDocuments()
  {
    return isContainingDocuments;
  }

  public boolean isContent()
  {
    return isContent;
  }

  public boolean isContentTags()
  {
    return isContentTags;
  }

  public boolean isDateCreated()
  {
    return isDateCreated;
  }

  public boolean isDateModified()
  {
    return isDateModified;
  }

  public boolean isDatePublished()
  {
    return isDatePublished;
  }

  public boolean isDateViewed()
  {
    return isDateViewed;
  }

  public boolean isDocument()
  {
    return isDocument;
  }

  public boolean isImage()
  {
    return isImage;
  }

  public boolean isMyPrivateTags()
  {
    return isMyPrivateTags;
  }

  public boolean isMyPublicTags()
  {
    return isMyPublicTags;
  }

  public boolean isOtherPublicTags()
  {
    return isOtherPublicTags;
  }

  public boolean isPublished()
  {
    return isPublished;
  }

  public boolean isSemanticTags()
  {
    return isSemanticTags;
  }

  public boolean isStyle()
  {
    return isStyle;
  }

  public boolean isTable()
  {
    return isTable;
  }

  public boolean isText()
  {
    return isText;
  }

  public boolean isTitle()
  {
    return isTitle;
  }

  public boolean isUnpublished()
  {
    return isUnpublished;
  }

  public boolean isWorkflow()
  {
    return isWorkflow;
  }

  public void setAuthorTags(boolean isAuthorTags)
  {
    this.isAuthorTags = isAuthorTags;
  }

  public void setCaption(boolean isCaption)
  {
    this.isCaption = isCaption;
  }

  public void setChart(boolean isChart)
  {
    this.isChart = isChart;
  }

  public void setContainingDocuments(boolean isContainingDocuments)
  {
    this.isContainingDocuments = isContainingDocuments;
  }

  public void setContent(boolean isContent)
  {
    this.isContent = isContent;
  }

  public void setContentTags(boolean isContentTags)
  {
    this.isContentTags = isContentTags;
  }

  public void setDateCreated(boolean isDateCreated)
  {
    this.isDateCreated = isDateCreated;
  }

  public void setDateModified(boolean isDateModified)
  {
    this.isDateModified = isDateModified;
  }

  public void setDatePublished(boolean isDatePublished)
  {
    this.isDatePublished = isDatePublished;
  }

  public void setDateViewed(boolean isDateViewed)
  {
    this.isDateViewed = isDateViewed;
  }

  public void setDocument(boolean isDocument)
  {
    this.isDocument = isDocument;
  }

  public void setFromDate(Date fromDate)
  {
    this.fromDate = fromDate;
  }

  public void setImage(boolean isImage)
  {
    this.isImage = isImage;
  }

  public void setMyPrivateTags(boolean isGetMyPrivateTags)
  {
    this.isMyPrivateTags = isGetMyPrivateTags;
  }

  public void setMyPublicTags(boolean isGetMyPublicTags)
  {
    this.isMyPublicTags = isGetMyPublicTags;
  }

  public void setOtherPublicTags(boolean isGetOtherPublicTags)
  {
    this.isOtherPublicTags = isGetOtherPublicTags;
  }

  public void setPublished(boolean isPublished)
  {
    this.isPublished = isPublished;
  }

  public void setSemanticTags(boolean isSemanticTags)
  {
    this.isSemanticTags = isSemanticTags;
  }

  public void setStyle(boolean isStyle)
  {
    this.isStyle = isStyle;
  }

  public void setTable(boolean isTable)
  {
    this.isTable = isTable;
  }

  public void setTagFilters(List<TagFilter> tagFilters)
  {
    this.tagFilters = tagFilters;
  }

  public void setTerms(String terms)
  {
    this.terms = terms;
  }

  public void setText(boolean isText)
  {
    this.isText = isText;
  }

  public void setTitle(boolean isTitle)
  {
    this.isTitle = isTitle;
  }

  public void setToDate(Date toDate)
  {
    this.toDate = toDate;
  }

  public void setUnpublished(boolean isUnpublished)
  {
    this.isUnpublished = isUnpublished;
  }

  public void setWorkflow(boolean isWorkflow)
  {
    this.isWorkflow = isWorkflow;
  }

}
