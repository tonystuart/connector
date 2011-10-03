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

package com.semanticexpression.connector.client.frame.editor.workflow;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.frame.editor.GridEditor;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ListStoreEditorWindow;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public final class WorkflowEditor extends GridEditor
{
  private EditorFrame editorFrame;
  private MenuItem frameletUpdateMenuItem;
  private MenuItem gridContextUpdateMenuItem;
  private SelectionListener<MenuEvent> updateMenuItemListener;
  private UpdateWorkflowTaskWindow updateWorkflowTaskWindow;

  public WorkflowEditor(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    super("Workflow", Resources.WORKFLOW, Resources.WORKFLOW_ADD, Resources.WORKFLOW_EDIT, Resources.WORKFLOW_REMOVE, Keys.WORKFLOW, modificationContext);
    this.editorFrame = editorFrame;
  }

  @Override
  public void display(ContentReference contentReference)
  {
    super.display(contentReference);
    setDescription(contentReference.getId().formatString());
  }

  @Override
  public ColumnModel getColumnModel()
  {
    if (columnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      columnConfigs.add(new ColumnConfig(Keys.WORKFLOW_TASK, "Task", 100));
      columnConfigs.add(new ColumnConfig(Keys.WORKFLOW_ASSIGNED_TO_NAME, "Assigned To", 100));
      columnConfigs.add(Utility.createDateColumnConfig(Keys.WORKFLOW_DUE_DATE, "Due Date", 100));
      columnConfigs.add(WorkflowModelData.getAccessColumnConfig());
      columnConfigs.add(WorkflowModelData.getOrderColumnConfig());
      columnConfigs.add(WorkflowModelData.getStatusColumnConfig());
      columnConfigs.add(new ColumnConfig(Keys.WORKFLOW_COMPLETED_BY_NAME, "Completed By", 100));
      columnConfigs.add(Utility.createDateColumnConfig(Keys.WORKFLOW_COMPLETION_DATE, "Completion Date", 100));
      columnConfigs.add(new ColumnConfig(Keys.WORKFLOW_REMARKS, "Remarks", 100));
      columnModel = new ColumnModel(columnConfigs);
    }
    return columnModel;
  }

  @Override
  public ListStoreEditorWindow getEditorWindow()
  {
    if (editorWindow == null)
    {
      editorWindow = new TaskEditorWindow(getEditorStore());
    }
    return editorWindow;
  }

  @Override
  public Menu getFrameletMenu()
  {
    if (frameletMenu == null)
    {
      super.getFrameletMenu();
      frameletMenu.add(new SeparatorMenuItem());
      frameletMenu.add(getFrameletUpdateMenuItem());
    }
    return frameletMenu;
  }

  private MenuItem getFrameletUpdateMenuItem()
  {
    if (frameletUpdateMenuItem == null)
    {
      frameletUpdateMenuItem = new MenuItem("Update Status");
      frameletUpdateMenuItem.setIcon(Resources.WORKFLOW_UPDATE);
      frameletUpdateMenuItem.addSelectionListener(getUpdateMenuItemListener());
    }
    return frameletUpdateMenuItem;
  }

  @Override
  public Menu getGridContextMenu()
  {
    if (gridContextMenu == null)
    {
      super.getGridContextMenu();
      gridContextMenu.add(new SeparatorMenuItem());
      gridContextMenu.add(getGridContextUpdateMenuItem());
    }
    return gridContextMenu;
  }

  private MenuItem getGridContextUpdateMenuItem()
  {
    if (gridContextUpdateMenuItem == null)
    {
      gridContextUpdateMenuItem = new MenuItem("Update Status");
      gridContextUpdateMenuItem.setIcon(Resources.WORKFLOW_UPDATE);
      gridContextUpdateMenuItem.addSelectionListener(getUpdateMenuItemListener());
    }
    return gridContextUpdateMenuItem;
  }

  private SelectionListener<MenuEvent> getUpdateMenuItemListener()
  {
    if (updateMenuItemListener == null)
    {
      updateMenuItemListener = new UpdateMenuItemSelectionListener();
    }
    return updateMenuItemListener;
  }

  private UpdateWorkflowTaskWindow getUpdateWorkflowTaskWindow()
  {
    if (updateWorkflowTaskWindow == null)
    {
      updateWorkflowTaskWindow = new UpdateWorkflowTaskWindow(editorFrame);
    }
    return updateWorkflowTaskWindow;
  }

  public void updateStatus()
  {
    Association selectedTask = getGrid().getSelectionModel().getSelectedItem();
    if (selectedTask == null)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    getUpdateWorkflowTaskWindow().display(contentReference.getId(), selectedTask);
    getUpdateWorkflowTaskWindow().show();
    getUpdateWorkflowTaskWindow().toFront();
    getUpdateWorkflowTaskWindow().alignTo(EditorFrame.getAlignToElement(this), "c-c?", null);
  }

  private final class UpdateMenuItemSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      updateStatus();
    }
  }

}
