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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.semanticexpression.connector.client.frame.editor.table.NavigationToolBar.NavigationHandler;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ConnectorWindow;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Sequence;

public class RowEditorWindow extends ConnectorWindow implements OkayCancelHandler
{
  private static final RowData FIELD_ROW_DATA = new RowData(1.0, Style.DEFAULT);
  private static final String HEADING = "Row Editor";
  private static final Margins LABEL_MARGINS = new Margins(5, 0, 0, 0);
  private static final RowData LABEL_ROW_DATA = new RowData(Style.DEFAULT, Style.DEFAULT, LABEL_MARGINS);

  private NavigationToolBar navigationToolBar;
  private ToolBar okayCancelToolBar;
  private RowEditorFields rowEditorFields;
  private TableFramelet tableFramelet;

  public RowEditorWindow(TableFramelet tableFramelet)
  {
    this.tableFramelet = tableFramelet;

    setSize(300, 350);
    setHeading(HEADING);
    setIcon(Resources.TABLE_EDIT_ROW);
    setLayout(new FitLayout());
    setTopComponent(getNavigationToolBar());
    add(getRowEditorFields(), new FitData(5)); // work around for GXT.isIE margin issue
    setBottomComponent(getOkayCancelToolBar());
  }

  @Override
  public void cancel()
  {
    hide();
  }

  public void edit(int rowOffset, int columnOffset, boolean isNew)
  {
    getRowEditorFields().edit(rowOffset, columnOffset, isNew);
  }

  public NavigationToolBar getNavigationToolBar()
  {
    if (navigationToolBar == null)
    {
      navigationToolBar = new NavigationToolBar(getRowEditorFields());
    }
    return navigationToolBar;
  }

  public ToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(this);
    }
    return okayCancelToolBar;
  }

  private RowEditorFields getRowEditorFields()
  {
    if (rowEditorFields == null)
    {
      rowEditorFields = new RowEditorFields();
    }
    return rowEditorFields;
  }

  @Override
  public void okay()
  {
    getRowEditorFields().okay();
  }

  public class RowEditorFields extends LayoutContainer implements NavigationHandler
  {
    private int columnOffset;
    private boolean isNew;
    private Association row;
    private int rowOffset;

    public RowEditorFields()
    {
      setLayout(new RowLayout(Orientation.VERTICAL));

      int variableHeightFieldCount = 0;
      RowData variableHeightRowData = new RowData(1.0, 1.0);

      Sequence<Association> columns = tableFramelet.getColumns();
      for (Association column : columns)
      {
        ConstrainedType constrainedType = ConstrainedTypeFactory.createConstrainedType(column);

        String name = column.get(Keys.NAME);
        add(new Text(name), LABEL_ROW_DATA);

        Field<?> field = ConstrainedFieldFactory.createConstrainedField(constrainedType);
        
        field.setMessageTarget("tooltip");
        
        if (field instanceof SafeTextArea)
        {
          variableHeightFieldCount++;
          float variableHeightFieldPercent = 1.0F / variableHeightFieldCount;
          variableHeightRowData.setHeight(variableHeightFieldPercent);
          add(field, variableHeightRowData);
        }
        else
        {
          add(field, FIELD_ROW_DATA);
        }
      }

    }

    private void apply()
    {
      int itemOffset = 1;
      Sequence<Association> columns = tableFramelet.getColumns();
      for (Association column : columns)
      {
        String name = column.get(Keys.NAME);
        ConstrainedField field = (ConstrainedField)getItem(itemOffset);
        Object value = field.getObjectValue();
        row.set(name, value);
        itemOffset += 2;
      }
      tableFramelet.onRowEditComplete(rowOffset, row, isNew);
      isNew = false; // only new once
    }

    public void edit(int rowOffset, int columnOffset, boolean isNew)
    {
      this.rowOffset = rowOffset;
      this.columnOffset = columnOffset;
      this.isNew = isNew;

      tableFramelet.getCellSelectionModel().selectCell(rowOffset, columnOffset);

      if (isNew)
      {
        row = new Association(IdManager.createIdentifier(), true);
      }
      else
      {
        row = tableFramelet.getRow(rowOffset);
      }

      int itemOffset = 1;
      Sequence<Association> columns = tableFramelet.getColumns();
      for (Association column : columns)
      {
        String name = column.get(Keys.NAME);
        Object value = row.get(name);
        ConstrainedField field = (ConstrainedField)getItem(itemOffset);
        field.setObjectValue(value);
        if (((itemOffset - 1) / 2) == columnOffset)
        {
          setFocusWidget((Field<?>)field);
        }
        itemOffset += 2;
      }

      updateFormState();
    }

    public void first()
    {
      apply();
      edit(0, columnOffset, false);
    }

    public void last()
    {
      apply();
      edit(tableFramelet.getRowCount() - 1, columnOffset, false);
    }

    public void next()
    {
      apply();
      edit(rowOffset + 1, columnOffset, false);
    }

    public void okay()
    {
      apply();
      RowEditorWindow.this.hide();
    }

    public void previous()
    {
      apply();
      edit(rowOffset - 1, columnOffset, false);
    }

    public void updateFormState()
    {
      int rowCount = tableFramelet.getRowCount();
      if (isNew)
      {
        rowCount++;
      }
      getNavigationToolBar().updateFormState(rowOffset, rowCount);
      setHeading(HEADING + " (Row " + (rowOffset + 1) + " of " + (rowCount) + ")");
    }

  }
}
