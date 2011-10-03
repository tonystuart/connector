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

package com.semanticexpression.connector.client.frame.editor;

import java.util.List;

import com.extjs.gxt.ui.client.dnd.DND.Feedback;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.widget.ListStoreEditorWindow;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.IdManager;

public abstract class GridEditor extends BaseEditor implements DetailsPanelComponent
{
  private AbstractImagePrototype addIcon;
  protected ColumnModel columnModel;
  protected ContentReference contentReference;
  private AbstractImagePrototype deleteIcon;
  private AbstractImagePrototype editIcon;
  protected EditorStore editorStore;
  protected ListStoreEditorWindow editorWindow;
  protected Menu frameletMenu;
  private Grid<Association> grid;
  protected Menu gridContextMenu;
  private String keyName;
  protected ModificationContext modificationContext;

  public GridEditor(String title, AbstractImagePrototype frameletIcon, AbstractImagePrototype addIcon, AbstractImagePrototype editIcon, AbstractImagePrototype deleteIcon, String keyName, ModificationContext modificationContext)
  {
    super(title, frameletIcon);

    this.addIcon = addIcon;
    this.editIcon = editIcon;
    this.deleteIcon = deleteIcon;
    this.keyName = keyName;
    this.modificationContext = modificationContext;

    setFrameletMenu(getFrameletMenu());

    add(getGrid());
  }

  protected void addItem()
  {
    Association association = new Association(IdManager.createIdentifier());
    association.setTrackChanges(true);
    addItem(association);
  }

  protected void addItem(Association association)
  {
    int index = findAddIndex(association);
    getEditorStore().insert(association, index);
    getGrid().getView().focusRow(index);
    getGrid().getSelectionModel().select(association, false);
    edit(association, true);
  }

  protected void deleteItem(Association association)
  {
    getEditorStore().remove(association);
  }

  protected void deleteSelectedItems()
  {
    List<Association> selectedItems = getGrid().getSelectionModel().getSelectedItems();
    if (selectedItems.size() == 0)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    for (Association association : selectedItems)
    {
      deleteItem(association);
    }
  }

  @Override
  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    getEditorStore().initialize(contentReference, keyName);
    setReadOnly(contentReference.isReadOnly());
  }

  public void edit(Association association, boolean isRemoveOnCancel)
  {
    getEditorWindow().edit(association, isRemoveOnCancel);
    getEditorWindow().show();
    getEditorWindow().alignTo(EditorFrame.getAlignToElement(this), "c-c?", null);
    getEditorWindow().toFront();
  }

  protected void editItem(Association association)
  {
    edit(association, false);
  }

  protected void editSelectedItem()
  {
    Association selectedItem = getGrid().getSelectionModel().getSelectedItem();
    if (selectedItem == null)
    {
      MessageBox.info("Nothing Selected", "Please select an item and try again", null);
    }
    else
    {
      editItem(selectedItem);
    }
  }

  public int findAddIndex(Association newItem)
  {
    int index;
    Association selectedItem = getGrid().getSelectionModel().getSelectedItem();
    if (selectedItem != null)
    {
      index = getEditorStore().indexOf(selectedItem) + 1;
    }
    else
    {
      index = getEditorStore().getCount();
    }
    return index;
  }

  protected abstract ColumnModel getColumnModel();

  public EditorStore getEditorStore()
  {
    if (editorStore == null)
    {
      editorStore = new EditorStore(modificationContext);
      editorStore.addListener(Store.BeforeRemove, new BeforeRemoveListener());
    }
    return editorStore;
  }

  public abstract ListStoreEditorWindow getEditorWindow();

  public Menu getFrameletMenu()
  {
    if (frameletMenu == null)
    {
      frameletMenu = new BaseEditorMenu();
    }
    return frameletMenu;
  }

  public Grid<Association> getGrid()
  {
    if (grid == null)
    {
      grid = new Grid<Association>(getEditorStore(), getColumnModel());
      grid.setContextMenu(getGridContextMenu());
      grid.setBorders(false);
      grid.getView().setAutoFill(true);
      grid.getView().setForceFit(true);
      grid.getView().setEmptyText("Select Add from context menu to create a new item.");
      grid.addListener(Events.RowDoubleClick, new RowDoubleClickListener());

      new GridDragSource(grid);

      GridDropTarget target = new GridDropTarget(grid);
      target.setAllowSelfAsSource(true);
      target.setFeedback(Feedback.INSERT);
    }
    return grid;
  }

  public Menu getGridContextMenu()
  {
    if (gridContextMenu == null)
    {
      gridContextMenu = new BaseEditorMenu();
    }
    return gridContextMenu;
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  @Override
  protected void onMinimizeOrClose()
  {
    if (editorWindow != null)
    {
      editorWindow.cancel();
    }
  }

  @Override
  public void saveChanges()
  {
    // No action required, changes are made directly to contentReference 
  }

  public void selectNextItemBeforeRemove(StoreEvent<Association> be)
  {
    if (getGrid().getSelectionModel().isSelected(be.getModel()))
    {
      int index = be.getIndex();
      int count = getEditorStore().getCount();
      int lastIndex = count - 1;
      if (index < lastIndex)
      {
        // Select next if there is one
        index++;
      }
      else
      {
        // Otherwise select previous
        index--;
      }
      if (index >= 0)
      {
        getGrid().getSelectionModel().select(index, false);
      }
    }
  }

  private final class AddListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      addItem();
    }
  }

  private final class BaseEditorMenu extends Menu
  {
    protected MenuItem addMenuItem;
    protected MenuItem deleteMenuItem;
    protected MenuItem editMenuItem;

    public BaseEditorMenu()
    {
      add(getAddMenuItem());
      add(getEditMenuItem());
      add(getDeleteMenuItem());
    }

    public MenuItem getAddMenuItem()
    {
      if (addMenuItem == null)
      {
        addMenuItem = new MenuItem("Add");
        addMenuItem.setIcon(addIcon);
        addMenuItem.addSelectionListener(new AddListener());
      }
      return addMenuItem;
    }

    public MenuItem getDeleteMenuItem()
    {
      if (deleteMenuItem == null)
      {
        deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setIcon(deleteIcon);
        deleteMenuItem.addSelectionListener(new DeleteListener());
      }
      return deleteMenuItem;
    }

    public MenuItem getEditMenuItem()
    {
      if (editMenuItem == null)
      {
        editMenuItem = new MenuItem("Edit");
        editMenuItem.setIcon(editIcon);
        editMenuItem.addSelectionListener(new EditListener());
      }
      return editMenuItem;
    }

  }

  private final class BeforeRemoveListener implements Listener<StoreEvent<Association>>
  {
    @Override
    public void handleEvent(StoreEvent<Association> be)
    {
      selectNextItemBeforeRemove(be);
    }
  }

  private final class DeleteListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      deleteSelectedItems();
    }
  }

  private final class EditListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      editSelectedItem();
    }
  }

  private final class RowDoubleClickListener implements Listener<GridEvent<Association>>
  {
    @Override
    public void handleEvent(GridEvent<Association> be)
    {
      Association association = be.getModel();
      if (association != null)
      {
        editItem(association);
      }
    }
  }

}