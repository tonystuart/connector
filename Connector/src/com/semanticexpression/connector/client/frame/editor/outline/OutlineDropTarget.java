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

package com.semanticexpression.connector.client.frame.editor.outline;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.search.SearchResultDragSource;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.SearchResult;

public final class OutlineDropTarget extends TreePanelDropTarget
{
  public OutlineDropTarget(OutlineTreePanel outlineTreePanel)
  {
    super(outlineTreePanel);
  }

  private List<Content> copy(List<Content> contentList)
  {
    Map<Id, Content> map = new LinkedHashMap<Id, Content>();
    List<Content> newContentList = new LinkedList<Content>();
    for (Content content : contentList)
    {
      Id id = content.getId();
      Content newContent = content.copy(true);
      map.put(id, newContent);
      newContentList.add(newContent);
    }
    for (Content content : newContentList)
    {
      List<Id> parts = content.get(Keys.PARTS);
      if (parts != null)
      {
        List<Id> newParts = new LinkedList<Id>();
        for (Id part : parts)
        {
          Content newContent = map.get(part);
          if (newContent == null)
          {
            throw new IllegalArgumentException();
          }
          Id newId = newContent.getId();
          newParts.add(newId);
        }
        content.set(Keys.PARTS, newParts);
      }
    }
    contentList = newContentList;
    return contentList;
  }

  private void copy(Object model, boolean isLink)
  {
    if (model instanceof TreeStoreModel)
    {
      TreeStoreModel treeStoreModel = (TreeStoreModel)model;
      ModelData modelData = treeStoreModel.getModel();
      if (modelData instanceof ContentReference)
      {
        ContentReference newContentReference;
        ContentReference oldContentReference = (ContentReference)modelData;
        if (isLink)
        {
          Id contentId = oldContentReference.getId();
          newContentReference = Directory.getContentManager().register(contentId);
        }
        else
        {
          Content content = oldContentReference.copy(true); // Track changes during copy before ContentManager.initialize() can be invoked
          newContentReference = Directory.getContentManager().register(content);
        }
        // See TreeStoreModel.getModel()
        treeStoreModel.set("model", newContentReference);
      }
      int count = treeStoreModel.getChildCount();
      for (int i = 0; i < count; i++)
      {
        ModelData child = treeStoreModel.getChild(i);
        copy(child, isLink);
      }
    }
  }

  private ModelData getFirstDropItem(DNDEvent event)
  {
    Object data = event.getData();
    if (data instanceof List<?>)
    {
      List<?> list = (List<?>)data;
      for (Object item : list)
      {
        if (item instanceof TreeStoreModel)
        {
          TreeStoreModel treeStoreModel = (TreeStoreModel)item;
          ModelData modelData = treeStoreModel.getModel();
          if (modelData != null)
          {
            return modelData;
          }
        }
      }
    }
    return null;
  }

  @Override
  public OutlineTreePanel getTree()
  {
    return (OutlineTreePanel)super.getTree();
  }

  @Override
  protected void onDragDrop(DNDEvent event)
  {
    DragSource dragSource = event.getDragSource();
    if (dragSource instanceof SearchResultDragSource)
    {
      SearchResultDragSource searchResultDragSource = (SearchResultDragSource)dragSource;
      boolean isCopy = !(event.isControlKey() && event.isShiftKey());
      List<SearchResult> searchResults = searchResultDragSource.getData();
      List<ContentReference> contentReferences = new LinkedList<ContentReference>();
      for (SearchResult searchResult : searchResults)
      {
        // Regardless of whether it's a copy or a link, we must create a new Id for the
        // SearchResult to avoid conflict with real Content with the same Id. We must also
        // not change the existing SearchResult Id to avoid problems with subsequent drag
        // and drop of the SearchResult. We create a temporary placeholder that is quickly
        // replaced when the real content is retrieved from the server.
        Id contentId = searchResult.getId();
        Content placeholder = searchResult.copy(false);
        ContentReference contentReference = new ContentReference(placeholder);
        contentReferences.add(contentReference);
        retrieveSearchResult(contentId, isCopy, contentReference);
      }
      event.setData(contentReferences);
    }
    else if (dragSource instanceof OutlineDragSource)
    {
      boolean isCopyOrLink = event.isControlKey();
      if (isCopyOrLink)
      {
        boolean isLink = event.isShiftKey();
        // See TreePanelDragSource.onDragStart()
        List<?> models = event.getData();
        for (Object model : models)
        {
          copy(model, isLink);
        }
      }
    }
    else
    {
      throw new IllegalArgumentException();
    }

    super.onDragDrop(event);

    ContentReference firstDropItem = (ContentReference)getFirstDropItem(event);
    if (firstDropItem != null)
    {
      getTree().getSelectionModel().select(firstDropItem, false);
      getTree().setExpanded(firstDropItem, true, true);
    }
  }

  private void retrieveSearchResult(Id contentId, final boolean isCopy, final ContentReference contentReference)
  {
    Directory.getConnectorService().retrieveContent(Utility.getAuthenticationToken(), contentId, null, true, new RetrieveSearchResultCallback(contentReference, isCopy));
  }

  protected void replaceSearchResultContentReference(ContentReference contentReference, boolean isCopy, List<Content> contentList)
  {
    EditorFrame editorFrame = EditorFrame.getEditorFrame(getTree());
    if (editorFrame != null)
    {
      if (isCopy)
      {
        contentList = copy(contentList);
      }
      editorFrame.replaceContent(contentReference, contentList);
    }
  }

  @Override
  protected void showFeedback(DNDEvent event)
  {
    if (event.isShiftKey() || !event.isControlKey()) // only check for link or move, copy is okay
    {
      com.extjs.gxt.ui.client.widget.treepanel.TreePanel<?>.TreeNode overItem = tree.findNode(event.getTarget());
      if (overItem != null)
      {
        ModelData overModelData = overItem.getModel();
        if (overModelData instanceof ContentReference)
        {
          ContentReference overContentReference = (ContentReference)overModelData;
          Object data = event.getData();
          if (data instanceof List<?>)
          {
            List<?> list = (List<?>)data;
            for (Object item : list)
            {
              if (item instanceof TreeStoreModel)
              {
                TreeStoreModel treeStoreModel = (TreeStoreModel)item;
                ModelData modelData = treeStoreModel.getModel();
                if (modelData != null)
                {
                  if (modelData instanceof ContentReference)
                  {
                    ContentReference dragContentReference = (ContentReference)modelData;
                    if (getTree().isCyclic(dragContentReference, overContentReference, event))
                    {
                      event.getStatus().setStatus(false);
                      return;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    super.showFeedback(event);
  }

  private final class RetrieveSearchResultCallback extends FailureReportingAsyncCallback<List<Content>>
  {
    private final ContentReference contentReference;
    private final boolean isCopy;

    private RetrieveSearchResultCallback(ContentReference contentReference, boolean isCopy)
    {
      this.contentReference = contentReference;
      this.isCopy = isCopy;
    }

    @Override
    public void onSuccess(List<Content> contentList)
    {
      replaceSearchResultContentReference(contentReference, isCopy, contentList);
    }
  }
}