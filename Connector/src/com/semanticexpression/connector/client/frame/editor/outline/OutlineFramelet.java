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

import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Framelet;
import com.semanticexpression.connector.shared.Keys;

public class OutlineFramelet extends Framelet
{
  private EditorFrame editorFrame;
  private Menu frameletMenu;
  private Menu outlineContextMenu;
  private OutlineTreePanel outlineTreePanel;
  private OutlineTreeStore outlineTreeStore;

  public OutlineFramelet(EditorFrame editorFrame)
  {
    super("Outline", Resources.OUTLINE);
    this.editorFrame = editorFrame;
    add(getOutlineTreePanel());
    setFrameletMenu(getFrameletMenu());
  }

  public Menu getFrameletMenu()
  {
    if (frameletMenu == null)
    {
      frameletMenu = new OutlineMenu(editorFrame);
    }
    return frameletMenu;
  }

  public Menu getOutlineContextMenu()
  {
    if (outlineContextMenu == null)
    {
      outlineContextMenu = new OutlineMenu(editorFrame);
    }
    return outlineContextMenu;
  }

  public OutlineTreePanel getOutlineTreePanel()
  {
    if (outlineTreePanel == null)
    {
      outlineTreePanel = new OutlineTreePanel(getOutlineTreeStore());
      outlineTreePanel.setAutoExpand(true);
      outlineTreePanel.setAutoLoad(true);
      outlineTreePanel.setDisplayProperty(Keys.TITLE);
      outlineTreePanel.setContextMenu(getOutlineContextMenu());
      outlineTreePanel.getSelectionModel().addSelectionChangedListener(new OutlineTreePanelSelectionChangedListener());
      outlineTreePanel.getDragSource().addDNDListener(new DNDListener()
      {
        @Override
        public void dragStart(DNDEvent e)
        {
          editorFrame.onOutlineDragStart();
        }

      });
    }
    return outlineTreePanel;
  }

  public OutlineTreeStore getOutlineTreeStore()
  {
    if (outlineTreeStore == null)
    {
      outlineTreeStore = new OutlineTreeStore();
      outlineTreeStore.setMonitorChanges(true);
      outlineTreeStore.addListener(Store.Add, new OutlineTreeStoreAddListener());
      outlineTreeStore.addListener(Store.Remove, new OutlineTreeStoreRemoveListener());
      outlineTreeStore.addListener(Store.Update, new OutlineTreeStoreUpdateListener());
    }
    return outlineTreeStore;
  }

  private final class OutlineTreePanelSelectionChangedListener extends SelectionChangedListener<ContentReference>
  {
    @Override
    public void selectionChanged(SelectionChangedEvent<ContentReference> selectionChangedEvent)
    {
      ContentReference contentReference = selectionChangedEvent.getSelectedItem();
      editorFrame.onOutlineSelectionChanged(contentReference);
    }

  }

  private final class OutlineTreeStoreAddListener implements Listener<TreeStoreEvent<ContentReference>>
  {
    @Override
    public void handleEvent(TreeStoreEvent<ContentReference> treeStoreEvent)
    {
      editorFrame.onOutlineTreeStoreAdd(treeStoreEvent);
    }
  }

  private final class OutlineTreeStoreRemoveListener implements Listener<TreeStoreEvent<ContentReference>>
  {
    @Override
    public void handleEvent(TreeStoreEvent<ContentReference> treeStoreEvent)
    {
      editorFrame.onOutlineTreeStoreRemove(treeStoreEvent);
    }
  }

  private final class OutlineTreeStoreUpdateListener implements Listener<TreeStoreEvent<ContentReference>>
  {
    @Override
    public void handleEvent(TreeStoreEvent<ContentReference> storeEvent)
    {
      editorFrame.onOutlineTreeStoreUpdate(storeEvent);
    }
  }

}
