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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteData;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.IntegerSpinnerField;

public class TableConfigurationWindow extends Window
{
  private TableConfigurationSpinnerField columnCountSpinnerField;
  private Text columnsText;
  private Button okayButton;
  private TableConfigurationSpinnerField rowCountSpinnerField;
  private Text rowsText;
  private TableFramelet tableFramelet;
  private ToolBar toolBar;

  public TableConfigurationWindow(TableFramelet tableFramelet)
  {
    this.tableFramelet = tableFramelet;

    setHeight(150);
    setResizable(false);
    setClosable(false);
    setHeading("Initial Table Configuration");
    setLayout(new AbsoluteLayout());
    add(getRowsText(), new AbsoluteData(25, 24));
    add(getColumnsText(), new AbsoluteData(25, 59));
    add(getRowCountSpinnerField(), new AbsoluteData(116, 16));
    add(getColumnCountSpinnerField(), new AbsoluteData(116, 51));
    setBottomComponent(getToolBar());
    setFocusWidget(getRowCountSpinnerField());
  }

  public void display()
  {
    getRowCountSpinnerField().setValue(1);
    getColumnCountSpinnerField().setValue(1);
  }

  public TableConfigurationSpinnerField getColumnCountSpinnerField()
  {
    if (columnCountSpinnerField == null)
    {
      columnCountSpinnerField = new TableConfigurationSpinnerField();
      columnCountSpinnerField.setValue(1);
      columnCountSpinnerField.setMaxValue(100);
      columnCountSpinnerField.setMinValue(1);
      columnCountSpinnerField.setAllowNegative(false);
      columnCountSpinnerField.setAllowBlank(false);
      columnCountSpinnerField.setAllowDecimals(false);
      columnCountSpinnerField.setPropertyEditorType(Integer.class);
    }
    return columnCountSpinnerField;
  }

  public Text getColumnsText()
  {
    if (columnsText == null)
    {
      columnsText = new Text("Columns:");
    }
    return columnsText;
  }

  public Button getOkayButton()
  {
    if (okayButton == null)
    {
      okayButton = new Button("Okay");
      okayButton.setIcon(Resources.OKAY);
      okayButton.addSelectionListener(new OkayButtonSelectionListener());
    }
    return okayButton;
  }

  public TableConfigurationSpinnerField getRowCountSpinnerField()
  {
    if (rowCountSpinnerField == null)
    {
      rowCountSpinnerField = new TableConfigurationSpinnerField();
      rowCountSpinnerField.setValue(1);
      rowCountSpinnerField.setMaxValue(100);
      rowCountSpinnerField.setMinValue(1);
      rowCountSpinnerField.setAllowNegative(false);
      rowCountSpinnerField.setAllowBlank(false);
      rowCountSpinnerField.setAllowDecimals(false);
      rowCountSpinnerField.setPropertyEditorType(Integer.class);
    }
    return rowCountSpinnerField;
  }

  public Text getRowsText()
  {
    if (rowsText == null)
    {
      rowsText = new Text("Rows:");
    }
    return rowsText;
  }

  public ToolBar getToolBar()
  {
    if (toolBar == null)
    {
      toolBar = new ToolBar();
      toolBar.add(new FillToolItem());
      toolBar.add(getOkayButton());
    }
    return toolBar;
  }

  private void propagateToTableEditor()
  {
    if (tableFramelet != null)
    {
      if (getRowCountSpinnerField().isValid() && getColumnCountSpinnerField().isValid())
      {
        getRowCountSpinnerField().clearInvalid();
        getColumnCountSpinnerField().clearInvalid();
        Number rowValue = getRowCountSpinnerField().getValue();
        Number columnValue = getColumnCountSpinnerField().getValue();
        int rowCount = (Integer)rowValue;
        int columnCount = (Integer)columnValue;
        tableFramelet.onTableDefinitionUpdate(rowCount, columnCount);
      }
    }
  }

  private class OkayButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      hide();
      tableFramelet.onTableDefinitionComplete();
    }
  }

  public final class TableConfigurationSpinnerField extends IntegerSpinnerField
  {
    @Override
    protected void onKeyUp(FieldEvent fe)
    {
      super.onKeyUp(fe);
      propagateToTableEditor();
    }

    @Override
    public void setValue(Number value)
    {
      super.setValue(value);
      propagateToTableEditor();
    }
  }
}
