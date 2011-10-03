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

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.dnd.DND.Feedback;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.widget.Editor;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.enums.ContentType;

public final class OutlineTreePanel extends TreePanel<ContentReference>
{
  private OutlineDragSource dragSource;
  private OutlineDropTarget dropTarget;

  OutlineTreePanel(OutlineTreeStore outlineTreeStore)
  {
    super(outlineTreeStore);

    setIconProvider(new OutlineTreeIconProvider());
    dragSource = new OutlineDragSource(this);
    dragSource.addDNDListener(new OutlineTreePanelDndListener());

    dropTarget = new OutlineDropTarget(this);
    dropTarget.setAllowSelfAsSource(true);
    dropTarget.setFeedback(Feedback.BOTH);
    dropTarget.setAutoScroll(true);
  }

  public TreePanelDragSource getDragSource()
  {
    return dragSource;
  }

  public TreePanelDropTarget getDropTarget()
  {
    return dropTarget;
  }

  /**
   * Enables drag and drop support to drop on an empty document card. Also
   * provides a visual indication that document cards can contain children.
   */
  @Override
  public boolean hasChildren(ContentReference contentReference)
  {
    if (contentReference.get(Keys.CONTENT_TYPE) == ContentType.DOCUMENT)
    {
      return true;
    }
    return super.hasChildren(contentReference);
  }

  public boolean isCyclic(ContentReference dragContentReference, ContentReference overContentReference, DNDEvent event)
  {
    ContentReference testOverContentReference = overContentReference;
    while (testOverContentReference != null)
    {
      if (dragContentReference.getId().equals(testOverContentReference.getId()))
      {
        return true;
      }
      else
      {
        testOverContentReference = getStore().getParent(testOverContentReference);
      }
    }

    OutlineTreePanel dragOutlineTreePanel = (OutlineTreePanel)event.getDragSource().getComponent(); // Not equal to this.dragSource unless source == target
    int childCount = dragOutlineTreePanel.getStore().getChildCount(dragContentReference);
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      ContentReference childContentReference = dragOutlineTreePanel.getStore().getChild(dragContentReference, childOffset);
      if (isCyclic(childContentReference, overContentReference, event))
      {
        return true;
      }
    }

    return false;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  @Override
  protected void onDoubleClick(TreePanelEvent treePanelEvent)
  {
    TreeNode treeNode = treePanelEvent.getNode();
    if (treeNode != null)
    {
      ContentReference contentReference = treeNode.getModel();
      String title = contentReference.get(Keys.TITLE);

      SafeTextField<String> titleSafeTextField = new SafeTextField<String>();
      Editor editor = new DetachableEditor(titleSafeTextField);
      editor.setCompleteOnEnter(true);
      editor.setCancelOnEsc(true);
      editor.addListener(Events.Complete, new EditingCompleteListener(titleSafeTextField, contentReference));

      Element element = getView().getElementContainer(treeNode);
      editor.startEdit(element, title);
    }
  }

  @Override
  protected void onRender(Element target, int index)
  {
    super.onRender(target, index);
  }

  @Override
  public void refresh(ContentReference contentReference)
  {
    super.refresh(contentReference);
  }
  
  public void selectBestLeaf()
  {
    ContentReference root = getStore().getChild(0);
    selectBestLeaf(root, FoundStatus.NOTHING);
  }

  private FoundStatus selectBestLeaf(ContentReference parent, FoundStatus foundStatus)
  {
    if (parent != null)
    {
      int childCount = getStore().getChildCount(parent);
      if (childCount == 0)
      {
        boolean isText = parent.get(Keys.CONTENT_TYPE) == ContentType.TEXT;
        if (isText)
        {
          getSelectionModel().select(parent, false);
          return FoundStatus.TEXT;
        }
        else if (foundStatus == FoundStatus.NOTHING)
        {
          getSelectionModel().select(parent, false);
          foundStatus = FoundStatus.SOMETHING;
        }
      }
      else
      {
        for (int childOffset = 0; childOffset < childCount; childOffset++)
        {
          ContentReference child = getStore().getChild(parent, childOffset);
          foundStatus = selectBestLeaf(child, foundStatus);
          if (foundStatus == FoundStatus.TEXT)
          {
            return foundStatus;
          }
        }
      }
    }
    return foundStatus;
  }

  public void selectRoot()
  {
    getSelectionModel().select(getStore().getChild(0), false);
  }

  /**
   * Works around a problem with RootPanel: when the browser refresh button is
   * pressed, RootPanel invokes detach even though an earlier onHide invoked by
   * Editor has already detached us.
   */
  private final class DetachableEditor extends Editor
  {
    private DetachableEditor(Field<String> field)
    {
      super(field);
    }

    @Override
    protected void onDetach()
    {
      if (isAttached())
      {
        super.onDetach();
      }
    }
  }

  private final class EditingCompleteListener implements Listener<BaseEvent>
  {
    private final ContentReference contentReference;
    private final SafeTextField<String> titleSafeTextField;

    private EditingCompleteListener(SafeTextField<String> titleSafeTextField, ContentReference contentReference)
    {
      this.titleSafeTextField = titleSafeTextField;
      this.contentReference = contentReference;
    }

    @Override
    public void handleEvent(BaseEvent be)
    {
      contentReference.set(Keys.TITLE, titleSafeTextField.getValue());
    }
  }

  private enum FoundStatus
  {
    NOTHING, SOMETHING, TEXT
  }

  public class OutlineTreeIconProvider implements ModelIconProvider<ContentReference>
  {
    @Override
    public AbstractImagePrototype getIcon(ContentReference contentReference)
    {
      AbstractImagePrototype icon = null;
      ContentType contentType = contentReference.get(Keys.CONTENT_TYPE);
      if (contentType != null)
      {
        switch (contentType)
        {
          case CHART:
            icon = Resources.CHART;
            break;
          case DOCUMENT:
            icon = getStore().getParent(contentReference) == null && getStore().indexOf(contentReference) == 0 ? Resources.DOCUMENT : Resources.SECTION;
            break;
          case IMAGE:
            icon = Resources.IMAGE;
            break;
          case STYLE:
            icon = Resources.STYLE;
            break;
          case TABLE:
            icon = Resources.TABLE;
            break;
          case TEXT:
            icon = Resources.TEXT;
            break;
          case WORKFLOW:
            icon = Resources.WORKFLOW;
            break;
        }
      }
      return icon;
    }
  }

  private final class OutlineTreePanelDndListener extends DNDListener
  {
    @Override
    public void dragStart(DNDEvent e)
    {
      ContentReference contentReference = getSelectionModel().getSelectedItem();
      if (contentReference != null && contentReference == getStore().getRootItems().get(0))
      {
        e.setCancelled(true);
        e.getStatus().setStatus(false);
      }
    }
  }
}