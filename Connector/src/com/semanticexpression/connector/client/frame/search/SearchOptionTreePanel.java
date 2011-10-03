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

package com.semanticexpression.connector.client.frame.search;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.google.gwt.user.client.Element;
import com.semanticexpression.connector.shared.SearchRequest;

public final class SearchOptionTreePanel extends OptionTreePanel
{
  private BaseTreeModel authorTags;
  private BaseTreeModel chart;
  private BaseTreeModel containingDocument;
  private BaseTreeModel content;
  private BaseTreeModel caption;
  private BaseTreeModel contentTags;
  private BaseTreeModel dateCreated;
  private BaseTreeModel dateModified;
  private BaseTreeModel datePublished;
  private BaseTreeModel dateViewed;
  private BaseTreeModel document;
  private BaseTreeModel image;
  private BaseTreeModel myPrivateTags;
  private BaseTreeModel myPublicTags;
  private BaseTreeModel otherPublicTags;
  private BaseTreeModel published;
  private BaseTreeModel root;
  private BaseTreeModel semanticTags;
  private BaseTreeModel style;
  private BaseTreeModel table;
  private BaseTreeModel tags;
  private BaseTreeModel text;
  private BaseTreeModel title;
  private BaseTreeModel unpublished;
  private BaseTreeModel what;
  private BaseTreeModel when;
  private BaseTreeModel where;
  private BaseTreeModel which;
  private BaseTreeModel workflow;

  @Override
  protected BaseTreeModel getRoot()
  {
    if (root == null)
    {
      root = createTreeModel(null, "root");

      where = createTreeModel(root, "Fields to Search");
      title = createTreeModel(where, "Title");
      content = createTreeModel(where, "Content");
      caption = createTreeModel(where, "Caption");

      what = createTreeModel(root, "Types of Content");
      chart = createTreeModel(what, "Chart");
      document = createTreeModel(what, "Document");
      image = createTreeModel(what, "Image");
      style = createTreeModel(what, "Style");
      table = createTreeModel(what, "Table");
      text = createTreeModel(what, "Text");
      workflow = createTreeModel(what, "Workflow");

      when = createTreeModel(root, "Dates to Check");
      dateCreated = createTreeModel(when, "Created");
      dateModified = createTreeModel(when, "Modified");
      datePublished = createTreeModel(when, "Published");
      dateViewed = createTreeModel(when, "Viewed");

      which = createTreeModel(root, "Content to Retrieve");
      published = createTreeModel(which, "Published");
      unpublished = createTreeModel(which, "Unpublished");
      containingDocument = createTreeModel(which, "Containing Documents");

      tags = createTreeModel(root, "Tags to Retrieve");
      BaseTreeModel tagType = createTreeModel(tags, "Type");
      authorTags = createTreeModel(tagType, "Author Tags");
      contentTags = createTreeModel(tagType, "Content Tags");
      semanticTags = createTreeModel(tagType, "Semantic Tags");

      BaseTreeModel tagVisibility = createTreeModel(tags, "Visibility");
      myPrivateTags = createTreeModel(tagVisibility, "My Private Tags");
      myPublicTags = createTreeModel(tagVisibility, "My Public Tags");
      otherPublicTags = createTreeModel(tagVisibility, "Other Public Tags");
    }
    return root;
  }

  @Override
  protected void onRender(Element target, int index)
  {
    super.onRender(target, index);
    setChecked(true);
  }

  public void prepareSearchRequest(SearchRequest searchRequest)
  {
    searchRequest.setTitle(isChecked(title));
    searchRequest.setContent(isChecked(content));
    searchRequest.setCaption(isChecked(caption));

    searchRequest.setChart(isChecked(chart));
    searchRequest.setImage(isChecked(image));
    searchRequest.setDocument(isChecked(document));
    searchRequest.setStyle(isChecked(style));
    searchRequest.setTable(isChecked(table));
    searchRequest.setText(isChecked(text));
    searchRequest.setWorkflow(isChecked(workflow));

    searchRequest.setDateCreated(isChecked(dateCreated));
    searchRequest.setDateModified(isChecked(dateModified));
    searchRequest.setDatePublished(isChecked(datePublished));
    searchRequest.setDateViewed(isChecked(dateViewed));

    searchRequest.setUnpublished(isChecked(unpublished));
    searchRequest.setPublished(isChecked(published));
    searchRequest.setContainingDocuments(isChecked(containingDocument));

    searchRequest.setMyPrivateTags(isChecked(myPrivateTags));
    searchRequest.setMyPublicTags(isChecked(myPublicTags));
    searchRequest.setOtherPublicTags(isChecked(otherPublicTags));
    
    searchRequest.setAuthorTags(isChecked(authorTags));
    searchRequest.setContentTags(isChecked(contentTags));
    searchRequest.setSemanticTags(isChecked(semanticTags));
  }

  public void resetForm()
  {
    setChecked(false);
    setChecked(true);
  }

  public void setChecked(boolean isChecked)
  {
    setChecked(where, isChecked);
    setChecked(what, isChecked);
    setChecked(when, isChecked);
    setChecked(which, isChecked);
    setChecked(tags, isChecked);
  }

}