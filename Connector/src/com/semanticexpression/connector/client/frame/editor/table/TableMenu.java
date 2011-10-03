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

package com.semanticexpression.connector.client.frame.editor.table;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.semanticexpression.connector.client.icons.Resources;

public class TableMenu extends Menu
{
  private MenuItem deleteColumnMenuItem;
  private MenuItem deleteRowMenuItem;
  private MenuItem editCellFormulaMenuItem;
  private MenuItem editColumn;
  private MenuItem editColumnFormulaMenuItem;
  private MenuItem editRow;
  private MenuItem editRowFormulaMenuItem;
  private MenuItem insertAboveMenuItem;
  private MenuItem insertBelowMenuItem;
  private MenuItem insertLeftMenuItem;
  private MenuItem insertRightMenuItem;
  private TableFramelet tableFramelet;

  public TableMenu(TableFramelet tableFramelet)
  {
    this.tableFramelet = tableFramelet;

    add(getInsertLeftMenuItem());
    add(getInsertRightMenuItem());
    add(getInsertAboveMenuItem());
    add(getInsertBelowMenuItem());
    add(new SeparatorMenuItem());
    add(getEditColumn());
    add(getEditRow());
    add(new SeparatorMenuItem());
    add(getDeleteColumnMenuItem());
    add(getDeleteRowMenuItem());
    add(new SeparatorMenuItem());
    add(getEditCellFormulaMenuItem());
    add(getEditColumnFormulaMenuItem());
    add(getEditRowFormulaMenuItem());
  }

  public MenuItem getDeleteColumnMenuItem()
  {
    if (deleteColumnMenuItem == null)
    {
      deleteColumnMenuItem = new MenuItem("Delete Column");
      deleteColumnMenuItem.setIcon(Resources.TABLE_DELETE_COLUMN);
      deleteColumnMenuItem.addSelectionListener(new DeleteColumnListener());
    }
    return deleteColumnMenuItem;
  }

  public MenuItem getDeleteRowMenuItem()
  {
    if (deleteRowMenuItem == null)
    {
      deleteRowMenuItem = new MenuItem("Delete Row");
      deleteRowMenuItem.setIcon(Resources.TABLE_DELETE_ROW);
      deleteRowMenuItem.addSelectionListener(new DeleteRowListener());
    }
    return deleteRowMenuItem;
  }

  public MenuItem getEditCellFormulaMenuItem()
  {
    if (editCellFormulaMenuItem == null)
    {
      editCellFormulaMenuItem = new MenuItem("Edit Cell Formula");
      editCellFormulaMenuItem.setEnabled(false);
      editCellFormulaMenuItem.setIcon(Resources.TABLE_EDIT_FORMULA_CELL);
      editCellFormulaMenuItem.addSelectionListener(new EditCellFormulaListener());
    }
    return editCellFormulaMenuItem;
  }

  public MenuItem getEditColumn()
  {
    if (editColumn == null)
    {
      editColumn = new MenuItem("Edit Column Definition");
      editColumn.setIcon(Resources.TABLE_EDIT_COLUMN);
      editColumn.addSelectionListener(new EditColumnListener());
    }
    return editColumn;
  }

  public MenuItem getEditColumnFormulaMenuItem()
  {
    if (editColumnFormulaMenuItem == null)
    {
      editColumnFormulaMenuItem = new MenuItem("Edit Column Formula");
      editColumnFormulaMenuItem.setEnabled(false);
      editColumnFormulaMenuItem.setIcon(Resources.TABLE_EDIT_FORMULA_COLUMN);
      editColumnFormulaMenuItem.addSelectionListener(new EditColumnFormulaListener());
    }
    return editColumnFormulaMenuItem;
  }

  public MenuItem getEditRow()
  {
    if (editRow == null)
    {
      editRow = new MenuItem("Edit Row Values");
      editRow.setIcon(Resources.TABLE_EDIT_ROW);
      editRow.addSelectionListener(new EditRowListener());
    }
    return editRow;
  }

  public MenuItem getEditRowFormulaMenuItem()
  {
    if (editRowFormulaMenuItem == null)
    {
      editRowFormulaMenuItem = new MenuItem("Edit Row Formula");
      editRowFormulaMenuItem.setIcon(Resources.TABLE_EDIT_FORMULA_ROW);
      editRowFormulaMenuItem.addSelectionListener(new EditRowFormulaListener());
    }
    return editRowFormulaMenuItem;
  }

  public MenuItem getInsertAboveMenuItem()
  {
    if (insertAboveMenuItem == null)
    {
      insertAboveMenuItem = new MenuItem("Insert Above");
      insertAboveMenuItem.setIcon(Resources.TABLE_INSERT_ABOVE);
      insertAboveMenuItem.addSelectionListener(new InsertAboveListener());
    }
    return insertAboveMenuItem;
  }

  public MenuItem getInsertBelowMenuItem()
  {
    if (insertBelowMenuItem == null)
    {
      insertBelowMenuItem = new MenuItem("Insert Below");
      insertBelowMenuItem.setIcon(Resources.TABLE_INSERT_BELOW);
      insertBelowMenuItem.addSelectionListener(new InsertBelowListener());
    }
    return insertBelowMenuItem;
  }

  public MenuItem getInsertLeftMenuItem()
  {
    if (insertLeftMenuItem == null)
    {
      insertLeftMenuItem = new MenuItem("Insert Left");
      insertLeftMenuItem.setIcon(Resources.TABLE_INSERT_LEFT);
      insertLeftMenuItem.addSelectionListener(new InsertLeftListener());
    }
    return insertLeftMenuItem;
  }

  public MenuItem getInsertRightMenuItem()
  {
    if (insertRightMenuItem == null)
    {
      insertRightMenuItem = new MenuItem("Insert Right");
      insertRightMenuItem.setIcon(Resources.TABLE_INSERT_RIGHT);
      insertRightMenuItem.addSelectionListener(new InsertRightListener());
    }
    return insertRightMenuItem;
  }

  private final class DeleteColumnListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.deleteColumn();
    }
  }

  private final class DeleteRowListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.deleteRow();
    }
  }

  private final class EditCellFormulaListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.editCellFormula();
    }
  }

  private final class EditColumnFormulaListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.editColumnFormula();
    }
  }

  private final class EditColumnListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.editColumn(false, false);
    }
  }

  private final class EditRowFormulaListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.editRowFormula();
    }
  }

  private final class EditRowListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.editRow();
    }
  }

  private final class InsertAboveListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.insertRow(false);
    }
  }

  private final class InsertBelowListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.insertRow(true);
    }
  }

  private final class InsertLeftListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.editColumn(false, true);
    }
  }

  private final class InsertRightListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      tableFramelet.editColumn(true, true);
    }
  }

}
