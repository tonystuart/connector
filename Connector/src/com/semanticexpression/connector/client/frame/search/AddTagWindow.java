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

import java.util.LinkedList;
import java.util.List;

import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.EditorStore;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.SearchResult;
import com.semanticexpression.connector.shared.TagConstants.TagType;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;

public final class AddTagWindow extends TagWindow
{
  private SearchFrame searchFrame;
  private List<SearchResult> selectedItems;
  private TagType tagType;

  public AddTagWindow(EditorStore editorStore)
  {
    super(Resources.TAG, "Add New Tag", "Enter Tag Name:", "Type characters to find matching tags", "or select existing tag that matches:", "Tag is Private (cannot be seen by others):", editorStore);
  }

  @Override
  public void okay()
  {
    final String tagName = getNameTextField().getValue();
    final TagVisibility tagVisibility = getTagVisibility();
    List<Id> ids = new LinkedList<Id>();
    for (SearchResult selectedItem : selectedItems)
    {
      ids.add(selectedItem.getId());
    }

    Directory.getConnectorService().addTag(Utility.getAuthenticationToken(), ids, tagName, tagType, tagVisibility, new FailureReportingAsyncCallback<Void>()
    {
      @Override
      public void onSuccess(Void result)
      {
        hide();
        searchFrame.updateSearchResults(selectedItems, tagType, tagName, tagVisibility);
      }
    });
  }

  public void tag(SearchFrame searchFrame, List<SearchResult> selectedItems, TagType tagType)
  {
    this.searchFrame = searchFrame;
    this.selectedItems = selectedItems;
    this.tagType = tagType;
    reset();
  }

}
