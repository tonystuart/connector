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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.DND.Feedback;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.i18n.client.NumberFormat;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.BaseEditor;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.frame.editor.EditorStore;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ClearSortEditorGrid;
import com.semanticexpression.connector.client.widget.ClearSortGridDropTarget;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Sequence;

public final class TableFramelet extends BaseEditor
{
  private static final String NUMBER_FORMAT = "#.##";

  private ColumnModel columnModel;
  private ColumnEditorWindow columnWindow;
  private ContentReference contentReference;
  private TableMenu contextTableMenu;
  private EditorStore editorStore;
  private FormulaEditorWindow formulaEditorWindow;
  private TableMenu frameletMenu;
  private ClearSortEditorGrid<Association> grid;
  private boolean isInRecalculateFormulas;
  private ModificationContext modificationContext;
  private MultiLineGridCellRenderer multiLineGridCellRenderer;
  private RowEditorWindow rowEditorWindow;
  private TableConfigurationWindow tableConfigurationWindow;

  public TableFramelet(ModificationContext modificationContext)
  {
    super("Table", Resources.TABLE);
    this.modificationContext = modificationContext;
    setFrameletMenu(getFrameletMenu());
  }

  private void configureColumns(Sequence<Association> columns)
  {
    List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
    for (Association column : columns)
    {
      Boolean isDisplayColumn = column.get(Keys.TABLE_COLUMN_DISPLAY, true);
      if (isDisplayColumn)
      {
        String columnName = column.get(Keys.NAME);
        String id = HtmlConstants.SE_MC_CONTENT + column.getId().formatString();
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(id);
        columnConfig.setDataIndex(columnName);
        columnConfig.setHeader(columnName);
        columnConfig.setWidth(100);
        columnConfig.setRenderer(getMultiLineGridCellRenderer());
        ConstrainedType constrainedType = ConstrainedTypeFactory.createConstrainedType(column);
        ColumnType columnType = constrainedType.getColumnType();
        if (columnType == ColumnType.DECIMAL || columnType == ColumnType.INTEGER)
        {
          columnConfig.setAlignment(HorizontalAlignment.RIGHT);
        }
        Field<?> field = ConstrainedFieldFactory.createConstrainedField(constrainedType);
        ResizableCellEditor cellEditor = new ResizableCellEditor(field);
        columnConfig.setEditor(cellEditor);
        // TODO: to auto-commit use this: cellEditor.addListener(Events.Complete, new CellEditorCompleteListener());
        columnConfigs.add(columnConfig);
      }
    }

    if (columnConfigs.size() == 0)
    {
      columnConfigs.add(new ColumnConfig("A", "A", 100)); // workaround GXT problem on reconfigure if header is empty
    }

    columnModel = new ColumnModel(columnConfigs);

    if (rowEditorWindow != null)
    {
      rowEditorWindow.hide();
      rowEditorWindow = null;
    }
  }

  public void configureGrid()
  {
    getGrid().reconfigure(getEditorStore(), columnModel);
  }

  protected Association createAssociation()
  {
    Association association = new Association(IdManager.createIdentifier());
    association.setTrackChanges(true);
    return association;
  }

  protected String createColumnName(int columnOffset)
  {
    String columnName;
    int cycle = columnOffset / 26;
    int position = columnOffset % 26;
    char letter = (char)('A' + position);
    columnName = Character.toString(letter);
    if (cycle > 0)
    {
      columnName += cycle;
    }
    return columnName;
  }

  public void deleteColumn()
  {
    CellSelectionModel<Association>.CellSelection cellSelection = getCellSelectionModel().getSelectCell();
    if (cellSelection == null)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    int deleteColumnOffset = cellSelection.cell;
    Sequence<Association> columns = contentReference.get(Keys.TABLE_COLUMNS);
    if (deleteColumnOffset >= columns.size())
    {
      throw new IllegalArgumentException("Column does not exist");
    }

    Association column = null;
    int currentColumnOffset = 0;
    Iterator<Association> iterator = columns.iterator();

    while (currentColumnOffset <= deleteColumnOffset)
    {
      column = iterator.next();
      currentColumnOffset++;
    }

    iterator.remove();

    String columnName = column.get(Keys.NAME);

    int rowCount = getEditorStore().getCount();

    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      Association row = getEditorStore().getAt(rowOffset);
      row.set(columnName, null);
    }

    configureColumns(columns);
    configureGrid();

  }

  public void deleteRow()
  {
    Association selectedItem = getGrid().getSelectionModel().getSelectedItem();
    if (selectedItem == null)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    getEditorStore().remove(selectedItem);
  }

  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    setDescription(contentReference.getId().formatString());
    getEditorStore().initialize(contentReference, Keys.TABLE_ROWS);

    Sequence<Association> columns = contentReference.get(Keys.TABLE_COLUMNS);
    if (columns == null)
    {
      columns = new Sequence<Association>(true);
      contentReference.set(Keys.TABLE_COLUMNS, columns);
    }

    configureColumns(columns);

    if (grid == null)
    {
      add(getGrid());
      doLayout();
    }
    else
    {
      configureGrid();
    }

    if (columns.size() == 0)
    {
      showTableConfigurationWindow();
    }

    setReadOnly(contentReference.isReadOnly());
  }

  public void editCellFormula()
  {
  }

  public void editColumn(boolean isInsertOnRight, boolean isNew)
  {
    CellSelectionModel<Association>.CellSelection cellSelection = getCellSelectionModel().getSelectCell();
    if (cellSelection == null)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    int rowOffset = cellSelection.row;
    int columnOffset = cellSelection.cell;

    if (isInsertOnRight)
    {
      columnOffset++;
    }

    showColumnDefinitionWindow(rowOffset, columnOffset, isNew);
  }

  public void editColumnFormula()
  {
  }

  public void editRow()
  {
    CellSelectionModel<Association>.CellSelection cellSelection = getCellSelectionModel().getSelectCell();
    if (cellSelection == null)
    {
      Utility.displaySelectionMessageBox();
      return;
    }
    showRowEditorWindow(cellSelection.row, cellSelection.cell, false);
  }

  public void editRowFormula()
  {
    Association selectedItem = getGrid().getSelectionModel().getSelectedItem();
    if (selectedItem == null)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    getFormulaEditor().display(this, selectedItem);
  }

  CellSelectionModel<Association> getCellSelectionModel()
  {
    return (CellSelectionModel<Association>)getGrid().getSelectionModel();
  }

  public Association getColumn(int columnOffset)
  {
    Association column = null;
    Sequence<Association> columns = contentReference.get(Keys.TABLE_COLUMNS);
    if (columnOffset < columns.size())
    {
      column = columns.get(columnOffset);
    }
    return column;
  }

  public Association getColumn(String needleColumnName)
  {
    Sequence<Association> columns = contentReference.get(Keys.TABLE_COLUMNS);
    for (Association column : columns)
    {
      String haystackColumnName = column.get(Keys.NAME);
      if (haystackColumnName.equals(needleColumnName))
      {
        return column;
      }
    }
    return null;
  }

  public int getColumnCount()
  {
    return getColumns().size();
  }

  public ColumnEditorWindow getColumnDefinitionEditor()
  {
    if (columnWindow == null)
    {
      columnWindow = new ColumnEditorWindow(this);
    }
    return columnWindow;
  }

  private String getColumnName(Sequence<Association> columns, int columnOffset)
  {
    String columnName = "";
    if (columns != null && (0 <= columnOffset && columnOffset < columns.size()))
    {
      columnName = columns.get(columnOffset).get(Keys.NAME);
    }
    return columnName;
  }

  public Sequence<Association> getColumns()
  {
    return contentReference.get(Keys.TABLE_COLUMNS);
  }

  public Menu getContextTableMenu()
  {
    if (contextTableMenu == null)
    {
      contextTableMenu = new TableMenu(this);
    }
    return contextTableMenu;
  }

  private EditorStore getEditorStore()
  {
    if (editorStore == null)
    {
      editorStore = new EditorStore(modificationContext);
      editorStore.setMonitorChanges(true);
      editorStore.addStoreListener(new StoreListener<Association>()
      {

        @Override
        public void handleEvent(StoreEvent<Association> e)
        {
          super.handleEvent(e);
          recalculateFormulas();
        }
      });
    }
    return editorStore;
  }

  private FormulaEditorWindow getFormulaEditor()
  {
    if (formulaEditorWindow == null)
    {
      formulaEditorWindow = new FormulaEditorWindow();
    }
    return formulaEditorWindow;
  }

  private Menu getFrameletMenu()
  {
    if (frameletMenu == null)
    {
      frameletMenu = new TableMenu(this);
    }
    return frameletMenu;
  }

  public ClearSortEditorGrid<Association> getGrid()
  {
    if (grid == null)
    {
      grid = new ClearSortEditorGrid<Association>(getEditorStore(), columnModel);
      grid.getView().setEmptyText("Please add a row and column.");
      grid.getView().setAutoFill(true);
      grid.getView().setForceFit(true);
      grid.setClicksToEdit(ClicksToEdit.TWO);
      grid.setBorders(false);
      grid.setColumnLines(true);
      grid.setContextMenu(getContextTableMenu());
      grid.setSelectionModel(getCellSelectionModel());
      grid.addListener(Events.ContextMenu, new ContextMenuListener());

      new GridDragSource(grid);
      GridDropTarget target = new ClearSortGridDropTarget(grid);
      target.setAllowSelfAsSource(true);
      target.setFeedback(Feedback.INSERT);
    }
    return grid;
  }

  private GridCellRenderer<ModelData> getMultiLineGridCellRenderer()
  {
    if (multiLineGridCellRenderer == null)
    {
      multiLineGridCellRenderer = new MultiLineGridCellRenderer();
    }
    return multiLineGridCellRenderer;
  }

  public Association getRow(int rowOffset)
  {
    return getEditorStore().getAt(rowOffset);
  }

  public int getRowCount()
  {
    return getEditorStore().getCount();
  }

  private RowEditorWindow getRowEditor()
  {
    if (rowEditorWindow == null)
    {
      rowEditorWindow = new RowEditorWindow(this);
    }
    return rowEditorWindow;
  }

  private TableConfigurationWindow getTableConfigurationWindow()
  {
    if (tableConfigurationWindow == null)
    {
      tableConfigurationWindow = new TableConfigurationWindow(this);
    }
    return tableConfigurationWindow;
  }

  public void insertRow(boolean isInsertBelow)
  {
    Association selectedItem = getGrid().getSelectionModel().getSelectedItem();
    if (selectedItem == null)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    int rowOffset = getEditorStore().indexOf(selectedItem);
    if (isInsertBelow)
    {
      rowOffset++;
    }

    getGrid().clearColumnHeaderSort();
    getEditorStore().insert(createAssociation(), rowOffset);
  }

  public void onColumnDefinitionComplete(String newColumnName, Boolean isDisplayInTable, String constrainedTypeSpecification, int rowOffset, int columnOffset, boolean isNew)
  {
    Sequence<Association> columns = contentReference.get(Keys.TABLE_COLUMNS);

    int listOffset = 0;
    ListIterator<Association> listIterator = columns.listIterator();

    while (listOffset < columnOffset && listIterator.hasNext())
    {
      listIterator.next();
      listOffset++;
    }

    Association column;

    if (isNew)
    {
      Id id = IdManager.createIdentifier();
      column = new Association(id, true);
      listIterator.add(column);
      column.set(Keys.NAME, newColumnName);
      column.set(Keys.TABLE_COLUMN_DISPLAY, isDisplayInTable);
      column.set(Keys.CONSTRAINED_TYPE, constrainedTypeSpecification);
    }
    else
    {
      column = listIterator.next();
      String oldColumnName = column.set(Keys.NAME, newColumnName);
      column.set(Keys.TABLE_COLUMN_DISPLAY, isDisplayInTable);
      column.set(Keys.CONSTRAINED_TYPE, constrainedTypeSpecification);

      if (!oldColumnName.equals(newColumnName))
      {
        List<Association> rows = getEditorStore().getModels();
        for (Association row : rows)
        {
          Object oldValue = row.get(oldColumnName);
          row.set(oldColumnName, null); // set to null to force server delete
          row.set(newColumnName, oldValue);
        }
      }
    }

    configureColumns(columns);
    configureGrid();

    getCellSelectionModel().selectCell(rowOffset, columnOffset);
  }

  @Override
  protected void onMinimizeOrClose()
  {
    if (tableConfigurationWindow != null)
    {
      tableConfigurationWindow.hide();
    }
    if (columnWindow != null)
    {
      columnWindow.hide();
    }
    if (formulaEditorWindow != null)
    {
      formulaEditorWindow.hide();
    }
    if (rowEditorWindow != null)
    {
      rowEditorWindow.hide();
    }
  }

  public void onRowEditComplete(int rowOffset, Association row, boolean isNew)
  {
    if (isNew)
    {
      getEditorStore().insert(row, rowOffset);
    }
    else
    {
      // row already updated
    }
  }

  public void onTableDefinitionComplete()
  {
    showColumnDefinitionWindow(0, 0, false);
  }

  public void onTableDefinitionUpdate(int newRowCount, int newColumnCount)
  {
    int columnChangeCount = 0;
    Sequence<Association> columns = contentReference.get(Keys.TABLE_COLUMNS);
    int columnCount;

    while ((columnCount = columns.size()) < newColumnCount)
    {
      Id id = IdManager.createIdentifier();
      Association column = new Association(id, true);
      String columnName = createColumnName(columnCount);
      column.set(Keys.NAME, columnName);
      columns.add(column);
      columnChangeCount++;
    }

    while (columns.size() > newColumnCount)
    {
      columns.removeLast();
      columnChangeCount++;
    }

    if (columnChangeCount > 0)
    {
      configureColumns(columns);
      configureGrid();
    }

    while (getEditorStore().getCount() < newRowCount)
    {
      Id id = IdManager.createIdentifier();
      Association row = new Association(id, true);
      getEditorStore().add(row);
    }

    int rowCount;
    while ((rowCount = getEditorStore().getCount()) > newRowCount)
    {
      getEditorStore().remove(rowCount - 1);
    }
  }

  public void recalculateFormula(String formula, Association row)
  {
    Sequence<Association> columns = contentReference.get(Keys.TABLE_COLUMNS);
    int columnCount = columns.size();
    int nameColumn = row.get(Keys.FORMULA_NAME_COLUMN, 1) - 1;
    int firstColumn = row.get(Keys.FORMULA_FIRST_COLUMN, 2) - 1;
    int lastColumn = row.get(Keys.FORMULA_LAST_COLUMN, columnCount) - 1;
    String numberFormat = row.get(Keys.FORMULA_RESULT_FORMAT, NUMBER_FORMAT);
    String rowKey = getColumnName(columns, nameColumn);

    int columnOffset = 0;
    for (Association column : columns)
    {
      if (firstColumn <= columnOffset && columnOffset <= lastColumn)
      {
        String columnKey = column.get(Keys.NAME);
        Evaluator evaluator = new Evaluator(formula, getEditorStore(), rowKey, columnKey);
        try
        {
          double value = evaluator.evaluateExpression();
          String x = NumberFormat.getFormat(numberFormat).format(value);
          row.set(columnKey, x);
        }
        catch (Exception e)
        {
          row.set(columnKey, "");
        }
      }
      columnOffset++;
    }

  }

  protected void recalculateFormulas()
  {
    if (!getEditorStore().isSuppressModifyEvent()) // don't recalculate during initial load
    {
      if (!isInRecalculateFormulas) // don't let our recalculation trigger a recalculation
      {
        isInRecalculateFormulas = true;
        try
        {
          int rowCount = getEditorStore().getCount();
          for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
          {
            Object object = getEditorStore().getAt(rowOffset);
            Association row = (Association)object;
            String formula = row.get(Keys.FORMULA);
            if (formula != null)
            {
              recalculateFormula(formula, row);
            }
          }
        }
        finally
        {
          isInRecalculateFormulas = false;
        }
      }
    }
  }

  private Object renderValue(Object value, boolean isFormula, boolean isMultipleLine)
  {
    if (value == null)
    {
      value = "<div class='x-form-empty-field' style='white-space: normal;'>Double click to edit.</div>";
    }
    else
    {
      if (isFormula)
      {
        value = "<span class='connector-TableFormula'>" + value + "</span>";
      }
      if (isMultipleLine) // not else
      {
        value = "<div style='white-space: normal;'>" + value + "</div>";
      }
      if (value instanceof Boolean) // not else
      {
        value = ((Boolean)value) ? "Yes" : "No";
      }
      else if (value instanceof Date)
      {
        value = Utility.formatDate((Date)value);
      }
    }
    return value;
  }

  public void saveChanges()
  {
    getEditorStore().commitChanges();
  }

  private void showColumnDefinitionWindow(int rowOffset, int columnOffset, boolean isNew)
  {
    getColumnDefinitionEditor().show();
    getColumnDefinitionEditor().toFront();
    getColumnDefinitionEditor().alignTo(getElement(), "c-c?", null);
    getColumnDefinitionEditor().edit(rowOffset, columnOffset, isNew);
  }

  private void showRowEditorWindow(int rowOffset, int columnOffset, boolean isNew)
  {
    getRowEditor().show();
    getRowEditor().toFront();
    getRowEditor().alignTo(getElement(), "c-c?", null);
    getRowEditor().edit(rowOffset, columnOffset, isNew);
  }

  private void showTableConfigurationWindow()
  {
    getTableConfigurationWindow().show();
    getTableConfigurationWindow().toFront();
    getTableConfigurationWindow().alignTo(getElement(), "c-c?", null);
    getTableConfigurationWindow().display();
  }

  private final class ContextMenuListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent baseEvent)
    {
      if (baseEvent instanceof GridEvent<?>)
      {
        @SuppressWarnings("unchecked")
        GridEvent<ModelData> gridEvent = (GridEvent<ModelData>)baseEvent;
        int rowIndex = gridEvent.getRowIndex();
        int columnIndex = gridEvent.getColIndex();
        if (rowIndex != -1 && columnIndex != -1) // workaround occasional GXT problem with inability to detect current cell
        {
          getCellSelectionModel().selectCell(rowIndex, columnIndex); // select cell prior to context menu display
        }
      }
    }
  }

  private final class MultiLineGridCellRenderer implements GridCellRenderer<ModelData>
  {
    @Override
    public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
    {
      Object value = model.get(property);
      boolean isFormula = model.get(Keys.FORMULA) != null;
      Field<?> field = grid.getColumnModel().getColumn(colIndex).getEditor().getField();
      boolean isMultipleLine = field instanceof SafeTextArea;
      value = renderValue(value, isFormula, isMultipleLine);
      return value;
    }
  }
}
