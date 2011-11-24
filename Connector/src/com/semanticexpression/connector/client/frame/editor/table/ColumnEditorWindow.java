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
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.semanticexpression.connector.client.frame.editor.table.NavigationToolBar.NavigationHandler;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ConnectorWindow;
import com.semanticexpression.connector.client.widget.DropDownComboBox;
import com.semanticexpression.connector.client.widget.IntegerSpinnerField;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.client.widget.YesNoField;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Sequence;

public final class ColumnEditorWindow extends ConnectorWindow implements OkayCancelHandler
{
  private static final RowData FIELD_ROW_DATA = new RowData(1.0, Style.DEFAULT);
  private static final String HEADING = "Column Editor";
  private static final Margins LABEL_MARGINS = new Margins(5, 0, 0, 0);
  private static final RowData LABEL_ROW_DATA = new RowData(Style.DEFAULT, Style.DEFAULT, LABEL_MARGINS);

  private ColumnEditorFields columnEditorFields;
  private NavigationToolBar navigationToolBar;
  private ToolBar okayCancelToolBar;
  private TableFramelet tableFramelet;

  public ColumnEditorWindow(TableFramelet tableFramelet)
  {
    this.tableFramelet = tableFramelet;

    setSize(300, 350);
    setHeading(HEADING);
    setIcon(Resources.TABLE_EDIT_COLUMN);
    setLayout(new FitLayout());
    setTopComponent(getNavigationToolBar());
    add(getColumnEditorFields(), new FitData(5)); // work around for GXT.isIE margin issue
    setBottomComponent(getOkayCancelToolBar());
  }

  @Override
  public void cancel()
  {
    hide();
  }

  public void edit(int rowOffset, int columnOffset, boolean isNew)
  {
    getColumnEditorFields().edit(rowOffset, columnOffset, isNew);
  }

  private ColumnEditorFields getColumnEditorFields()
  {
    if (columnEditorFields == null)
    {
      columnEditorFields = new ColumnEditorFields();
    }
    return columnEditorFields;
  }

  public NavigationToolBar getNavigationToolBar()
  {
    if (navigationToolBar == null)
    {
      navigationToolBar = new NavigationToolBar(getColumnEditorFields());
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

  @Override
  public void okay()
  {
    getColumnEditorFields().okay();
  }

  public final class ColumnEditorFields extends LayoutContainer implements NavigationHandler
  {
    private int baseSize;
    private Text columnNameText;
    private SafeTextField<String> columnNameTextField;
    private int columnOffset;
    private DropDownComboBox<ModelData> columnTypeComboBox;
    private Text columnTypeText;
    private Text displayInTableText;
    private YesNoField displayInTableYesNoField;
    private YesNoField displayMultipleLinesField;
    private Text displayMultipleLinesText;
    private boolean isNew;
    private DateField maximumDateField;
    private Text maximumDateText;
    private SpinnerField maximumDecimalValueField;
    private IntegerSpinnerField maximumIntegerValueField;
    private IntegerSpinnerField maximumLengthSpinnerField;
    private Text maximumLengthText;
    private Text maximumValueText;
    private DateField minimumDateField;
    private Text minimumDateText;
    private SpinnerField minimumDecimalValueField;
    private IntegerSpinnerField minimumIntegerValueField;
    private IntegerSpinnerField minimumLengthSpinnerField;
    private Text minimumLengthText;
    private Text minimumValueText;
    private YesNoField mustBePresentField;
    private Text mustBePresentText;
    private ColumnType previousColumnType;
    private int rowOffset;

    public ColumnEditorFields()
    {
      setLayout(new RowLayout(Orientation.VERTICAL));
      add(getColumnNameText(), LABEL_ROW_DATA);
      add(getColumnNameTextField(), FIELD_ROW_DATA);
      add(getDisplayInTableText(), LABEL_ROW_DATA);
      add(getDisplayInTableYesNoField(), FIELD_ROW_DATA);
      add(getColumnTypeText(), LABEL_ROW_DATA);
      add(getColumnTypeComboBox(), FIELD_ROW_DATA);
      setLayoutOnChange(true);
      baseSize = getItemCount();
    }

    private boolean apply()
    {
      String columnName = getColumnNameTextField().getValue();
      if (columnName == null)
      {
        MessageBox.alert("Missing Name", "Please enter a column name and try again.", null);
        return false;
      }
      if (isDuplicateColumnName(columnName))
      {
        MessageBox.alert("Duplicate Name", "A column with that name already exists. Please enter a name that does not exist and try again.", null);
        return false;
      }
      Boolean isDisplayInTable = getDisplayInTableYesNoField().getValue();
      String constrainedTypeSpecification = getConstrainedTypeSpecification();
      tableFramelet.onColumnDefinitionComplete(columnName, isDisplayInTable, constrainedTypeSpecification, rowOffset, columnOffset, isNew);
      isNew = false; // only new once
      return true;
    }

    public void clearConstraints(ColumnType columnType)
    {
      switch (columnType)
      {
        case DATE:
          getMinimumDateField().setValue(null);
          getMaximumDateField().setValue(null);
          getMustBePresentField().setValue(null);
          break;
        case DECIMAL:
          getMinimumDecimalValueField().setValue(null);
          getMaximumDecimalValueField().setValue(null);
          getMustBePresentField().setValue(null);
          break;
        case INTEGER:
          getMinimumIntegerValueField().setValue(null);
          getMaximumIntegerValueField().setValue(null);
          getMustBePresentField().setValue(null);
          break;
        case TEXT:
          getMinimumLengthSpinnerField().setValue(null); // includes mustBePresent semantics
          getMaximumLengthSpinnerField().setValue(null);
          getDisplayMultipleLinesField().setValue(null);
          break;
        case YESNO:
          getMustBePresentField().setValue(null);
          break;
      }
    }

    private void configureConstraints(ColumnType columnType)
    {
      if (columnType != previousColumnType)
      {
        int itemCount;
        while ((itemCount = getItemCount()) > baseSize)
        {
          remove(getItem(itemCount - 1));
        }
        switch (columnType)
        {
          case DATE:
            add(getMinimumDateText(), LABEL_ROW_DATA);
            add(getMinimumDateField(), FIELD_ROW_DATA);
            add(getMaximumDateText(), LABEL_ROW_DATA);
            add(getMaximumDateField(), FIELD_ROW_DATA);
            add(getMustBePresentText(), LABEL_ROW_DATA);
            add(getMustBePresentField(), FIELD_ROW_DATA);
            break;
          case DECIMAL:
            add(getMinimumValueText(), LABEL_ROW_DATA);
            add(getMinimumDecimalValueField(), FIELD_ROW_DATA);
            add(getMaximumValueText(), LABEL_ROW_DATA);
            add(getMaximumDecimalValueField(), FIELD_ROW_DATA);
            add(getMustBePresentText(), LABEL_ROW_DATA);
            add(getMustBePresentField(), FIELD_ROW_DATA);
            break;
          case INTEGER:
            add(getMinimumValueText(), LABEL_ROW_DATA);
            add(getMinimumIntegerValueField(), FIELD_ROW_DATA);
            add(getMaximumValueText(), LABEL_ROW_DATA);
            add(getMaximumIntegerValueField(), FIELD_ROW_DATA);
            add(getMustBePresentText(), LABEL_ROW_DATA);
            add(getMustBePresentField(), FIELD_ROW_DATA);
            break;
          case TEXT:
            add(getMinimumLengthText(), LABEL_ROW_DATA);
            add(getMinimumLengthSpinnerField(), FIELD_ROW_DATA); // includes mustBePresent semantics
            add(getMaximumLengthText(), LABEL_ROW_DATA);
            add(getMaximumLengthSpinnerField(), FIELD_ROW_DATA);
            add(getDisplayMultipleLinesText(), LABEL_ROW_DATA);
            add(getDisplayMultipleLinesField(), FIELD_ROW_DATA);
            break;
          case YESNO:
            add(getMustBePresentText(), LABEL_ROW_DATA);
            add(getMustBePresentField(), FIELD_ROW_DATA);
            break;
        }
        previousColumnType = columnType;
      }
    }

    private void configureFields(String columnName, ColumnType columnType, boolean isDisplayInTable)
    {
      getColumnNameTextField().setValue(columnName);
      getDisplayInTableYesNoField().setValue(isDisplayInTable);
      getColumnTypeComboBox().setSimpleValue(columnType);
      configureConstraints(columnType);
    }

    public void edit(int rowOffset, int columnOffset, boolean isNew)
    {
      this.rowOffset = rowOffset;
      this.columnOffset = columnOffset;
      this.isNew = isNew;
      this.previousColumnType = null;

      tableFramelet.getCellSelectionModel().selectCell(rowOffset, columnOffset);

      if (isNew)
      {
        configureFields(null, ColumnType.TEXT, true);
        clearConstraints(previousColumnType);
      }
      else
      {
        Association column = tableFramelet.getColumn(columnOffset);
        String columnName = column.get(Keys.NAME);
        ConstrainedType constrainedType = ConstrainedTypeFactory.createConstrainedType(column);
        ColumnType columnType = constrainedType.getColumnType();
        boolean isDisplayInTable = column.get(Keys.TABLE_COLUMN_DISPLAY, true);
        configureFields(columnName, columnType, isDisplayInTable);
        initializeConstraints(constrainedType);
      }

      ColumnEditorWindow.this.setFocusWidget(getColumnNameTextField());

      updateFormState();
    }

    public void first()
    {
      if (apply())
      {
        edit(rowOffset, 0, false);
      }
    }

    public Text getColumnNameText()
    {
      if (columnNameText == null)
      {
        columnNameText = new Text("Column Name:");
      }
      return columnNameText;
    }

    public SafeTextField<String> getColumnNameTextField()
    {
      if (columnNameTextField == null)
      {
        columnNameTextField = new SafeTextField<String>();
      }
      return columnNameTextField;
    }

    public DropDownComboBox<ModelData> getColumnTypeComboBox()
    {
      if (columnTypeComboBox == null)
      {
        columnTypeComboBox = new DropDownComboBox<ModelData>();
        columnTypeComboBox.add(ColumnTypeModelData.getColumnTypeList());
        columnTypeComboBox.addListener(Events.Select, new Listener<BaseEvent>()
        {
          @Override
          public void handleEvent(BaseEvent be)
          {
            onColumnTypeSelect();
          }
        });
      }
      return columnTypeComboBox;
    }

    public Text getColumnTypeText()
    {
      if (columnTypeText == null)
      {
        columnTypeText = new Text("Column Type:");
      }
      return columnTypeText;
    }

    private String getConstrainedTypeSpecification()
    {
      String specification = null;
      ColumnType columnType = getColumnTypeComboBox().getSimpleValue();
      switch (columnType)
      {
        case DATE:
          specification = ConstrainedTypeFactory.createSpecification(columnType, getMinimumDateField(), getMaximumDateField(), getMustBePresentField());
          break;
        case DECIMAL:
          specification = ConstrainedTypeFactory.createSpecification(columnType, getMinimumDecimalValueField(), getMaximumDecimalValueField(), getMustBePresentField());
          break;
        case INTEGER:
          specification = ConstrainedTypeFactory.createSpecification(columnType, getMinimumIntegerValueField(), getMaximumIntegerValueField(), getMustBePresentField());
          break;
        case TEXT:
          specification = ConstrainedTypeFactory.createSpecification(columnType, getMinimumLengthSpinnerField(), getMaximumLengthSpinnerField(), getDisplayMultipleLinesField());
          break;
        case YESNO:
          specification = ConstrainedTypeFactory.createSpecification(columnType, getMustBePresentField());
          break;
      }
      return specification;
    }

    private Text getDisplayInTableText()
    {
      if (displayInTableText == null)
      {
        displayInTableText = new Text("Display in Table:");
      }
      return displayInTableText;
    }

    private YesNoField getDisplayInTableYesNoField()
    {
      if (displayInTableYesNoField == null)
      {
        displayInTableYesNoField = new YesNoField();
      }
      return displayInTableYesNoField;
    }

    private YesNoField getDisplayMultipleLinesField()
    {
      if (displayMultipleLinesField == null)
      {
        displayMultipleLinesField = new YesNoField();
        displayMultipleLinesField.setEmptyText("Defaults to No");
      }
      return displayMultipleLinesField;
    }

    private Text getDisplayMultipleLinesText()
    {
      if (displayMultipleLinesText == null)
      {
        displayMultipleLinesText = new Text("Display Multiple Lines:");
      }
      return displayMultipleLinesText;
    }

    public DateField getMaximumDateField()
    {
      if (maximumDateField == null)
      {
        maximumDateField = new DateField();
        maximumDateField.setEmptyText("Defaults to unlimited");
      }
      return maximumDateField;
    }

    public Text getMaximumDateText()
    {
      if (maximumDateText == null)
      {
        maximumDateText = new Text("Latest Allowed Date:");
      }
      return maximumDateText;
    }

    public SpinnerField getMaximumDecimalValueField()
    {
      if (maximumDecimalValueField == null)
      {
        maximumDecimalValueField = new SpinnerField();
        maximumDecimalValueField.setEmptyText("Defaults to unlimited");
      }
      return maximumDecimalValueField;
    }

    public IntegerSpinnerField getMaximumIntegerValueField()
    {
      if (maximumIntegerValueField == null)
      {
        maximumIntegerValueField = new IntegerSpinnerField();
        maximumIntegerValueField.setEmptyText("Defaults to unlimited");
      }
      return maximumIntegerValueField;
    }

    public IntegerSpinnerField getMaximumLengthSpinnerField()
    {
      if (maximumLengthSpinnerField == null)
      {
        maximumLengthSpinnerField = new IntegerSpinnerField();
        maximumLengthSpinnerField.setEmptyText("Defaults to Unlimited");
        maximumLengthSpinnerField.setMaxValue(50000);
        maximumLengthSpinnerField.setMinValue(1);
        maximumLengthSpinnerField.setAllowNegative(false);
      }
      return maximumLengthSpinnerField;
    }

    public Text getMaximumLengthText()
    {
      if (maximumLengthText == null)
      {
        maximumLengthText = new Text("Maximum Length for Text:");
      }
      return maximumLengthText;
    }

    public Text getMaximumValueText()
    {
      if (maximumValueText == null)
      {
        maximumValueText = new Text("Maximum Value:");
      }
      return maximumValueText;
    }

    public DateField getMinimumDateField()
    {
      if (minimumDateField == null)
      {
        minimumDateField = new DateField();
        minimumDateField.setEmptyText("Defaults to unlimited");
      }
      return minimumDateField;
    }

    public Text getMinimumDateText()
    {
      if (minimumDateText == null)
      {
        minimumDateText = new Text("Earliest Allowed Date:");
      }
      return minimumDateText;
    }

    public SpinnerField getMinimumDecimalValueField()
    {
      if (minimumDecimalValueField == null)
      {
        minimumDecimalValueField = new SpinnerField();
        minimumDecimalValueField.setEmptyText("Defaults to unlimited");
      }
      return minimumDecimalValueField;
    }

    public IntegerSpinnerField getMinimumIntegerValueField()
    {
      if (minimumIntegerValueField == null)
      {
        minimumIntegerValueField = new IntegerSpinnerField();
        minimumIntegerValueField.setEmptyText("Defaults to unlimited");
      }
      return minimumIntegerValueField;
    }

    public IntegerSpinnerField getMinimumLengthSpinnerField()
    {
      if (minimumLengthSpinnerField == null)
      {
        minimumLengthSpinnerField = new IntegerSpinnerField();
        minimumLengthSpinnerField.setEmptyText("Defaults to Unlimited");
        minimumLengthSpinnerField.setMaxValue(50000);
        minimumLengthSpinnerField.setMinValue(1);
        minimumLengthSpinnerField.setAllowNegative(false);
      }
      return minimumLengthSpinnerField;
    }

    public Text getMinimumLengthText()
    {
      if (minimumLengthText == null)
      {
        minimumLengthText = new Text("Minimum Length for Text:");
      }
      return minimumLengthText;
    }

    public Text getMinimumValueText()
    {
      if (minimumValueText == null)
      {
        minimumValueText = new Text("Minimum Value:");
      }
      return minimumValueText;
    }

    private YesNoField getMustBePresentField()
    {
      if (mustBePresentField == null)
      {
        mustBePresentField = new YesNoField();
        mustBePresentField.setEmptyText("Defaults to No");
      }
      return mustBePresentField;
    }

    public Text getMustBePresentText()
    {
      if (mustBePresentText == null)
      {
        mustBePresentText = new Text("Must be Present:");
      }
      return mustBePresentText;
    }

    private void initializeConstraints(ConstrainedType constrainedType)
    {
      ColumnType columnType = constrainedType.getColumnType();
      switch (columnType)
      {
        case DATE:
          getMinimumDateField().setValue(constrainedType.getDate(0));
          getMaximumDateField().setValue(constrainedType.getDate(1));
          getMustBePresentField().setValue(constrainedType.getBoolean(2));
          break;
        case DECIMAL:
          getMinimumDecimalValueField().setValue(constrainedType.getInteger(0));
          getMaximumDecimalValueField().setValue(constrainedType.getInteger(1));
          getMustBePresentField().setValue(constrainedType.getBoolean(2));
          break;
        case INTEGER:
          getMinimumIntegerValueField().setValue(constrainedType.getInteger(0));
          getMaximumIntegerValueField().setValue(constrainedType.getInteger(0));
          getMustBePresentField().setValue(constrainedType.getBoolean(2));
          break;
        case TEXT:
          getMinimumLengthSpinnerField().setValue(constrainedType.getInteger(0)); // includes mustBePresent semantics
          getMaximumLengthSpinnerField().setValue(constrainedType.getInteger(1));
          getDisplayMultipleLinesField().setValue(constrainedType.getBoolean(2));
          break;
        case YESNO:
          getMustBePresentField().setValue(constrainedType.getBoolean(0));
          break;
      }
    }

    public boolean isDuplicateColumnName(String needleColumnName)
    {
      Sequence<Association> columns = tableFramelet.getColumns();
      int columnOffset = 0;
      for (Association column : columns)
      {
        String haystackColumnName = column.get(Keys.NAME);
        if ((isNew || (columnOffset != this.columnOffset)) && haystackColumnName.equals(needleColumnName))
        {
          return true;
        }
        columnOffset++;
      }
      return false;
    }

    public void last()
    {
      if (apply())
      {
        edit(rowOffset, tableFramelet.getColumnCount() - 1, false);
      }
    }

    public void next()
    {
      if (apply())
      {
        edit(rowOffset, columnOffset + 1, false);
      }
    }

    public void okay()
    {
      if (apply())
      {
        ColumnEditorWindow.this.hide();
      }
    }

    public void onColumnTypeSelect()
    {
      ColumnType columnType = getColumnTypeComboBox().getSimpleValue();
      configureConstraints(columnType);
      clearConstraints(previousColumnType);
    }

    public void previous()
    {
      if (apply())
      {
        edit(rowOffset, columnOffset - 1, false);
      }
    }

    public void updateFormState()
    {
      int columnCount = tableFramelet.getColumnCount();
      if (isNew)
      {
        columnCount++;
      }
      getNavigationToolBar().updateFormState(columnOffset, columnCount);
      setHeading(HEADING + " (Column " + (columnOffset + 1) + " of " + (columnCount) + ")");
    }

  }
}
