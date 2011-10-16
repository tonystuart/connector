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

package com.semanticexpression.connector.client.frame.admin;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.Element;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.widget.Frame;
import com.semanticexpression.connector.client.widget.PositionRetainingTextArea;
import com.semanticexpression.connector.client.widget.ResizableTabPanel;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.AdminRequest;
import com.semanticexpression.connector.shared.AdminResult;

public class AdminFrame extends Frame
{
  private GroupsTabItem groupsTabItem;
  private SqlTabItem sqlTabItem;
  private ResizableTabPanel tabPanel;
  private UsersTabItem usersTabItem;

  public AdminFrame()
  {
    setHeading("Admin");
    setSize(400, 400);
    setIcon(Resources.ADMIN);
    setBodyBorder(false);
    setBorders(false);
    setLayout(new FitLayout());
    add(getTabPanel());
  }

  private GroupsTabItem getGroupsTabItem()
  {
    if (groupsTabItem == null)
    {
      groupsTabItem = new GroupsTabItem();
      groupsTabItem.setIcon(Resources.GROUPS);
      groupsTabItem.setText("Groups");
    }
    return groupsTabItem;
  }

  private SqlTabItem getSqlTabItem()
  {
    if (sqlTabItem == null)
    {
      sqlTabItem = new SqlTabItem();
      sqlTabItem.setIcon(Resources.SQL);
      sqlTabItem.setText("SQL");
    }
    return sqlTabItem;
  }

  private ResizableTabPanel getTabPanel()
  {
    if (tabPanel == null)
    {
      tabPanel = new ResizableTabPanel();
      tabPanel.setTabPosition(TabPosition.BOTTOM);
      tabPanel.add(getUsersTabItem());
      tabPanel.add(getGroupsTabItem());
      tabPanel.add(getSqlTabItem());
    }
    return tabPanel;
  }

  private UsersTabItem getUsersTabItem()
  {
    if (usersTabItem == null)
    {
      usersTabItem = new UsersTabItem();
      usersTabItem.setIcon(Resources.USERS);
      usersTabItem.setText("Users");
    }
    return usersTabItem;
  }

  public class GroupsTabItem extends TabItem
  {
  }

  public class SqlTabItem extends TabItem
  {
    private ResultPanel resultPanel;
    private StatementPanel statementPanel;

    public SqlTabItem()
    {
      setLayout(new BorderLayout());
      add(getStatementPanel(), getStatementLayoutData());
      add(getResultPanel(), getResultLayoutData());
    }

    private void displayResult(AdminResult result)
    {
      getResultPanel().displayResult(result);
      ((BorderLayout)getLayout()).show(LayoutRegion.SOUTH);
    }

    protected BorderLayoutData getResultLayoutData()
    {
      BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.SOUTH, 0.5f, 0, 10000);
      layoutData.setMargins(new Margins(0, 5, 5, 5));
      layoutData.setSplit(true);
      layoutData.setFloatable(false);
      layoutData.setHidden(true);
      return layoutData;
    }

    private ResultPanel getResultPanel()
    {
      if (resultPanel == null)
      {
        resultPanel = new ResultPanel();
      }
      return resultPanel;
    }

    private BorderLayoutData getStatementLayoutData()
    {
      BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
      layoutData.setMargins(new Margins(5, 5, 5, 5));
      return layoutData;
    }

    private StatementPanel getStatementPanel()
    {
      if (statementPanel == null)
      {
        statementPanel = new StatementPanel();
      }
      return statementPanel;
    }

    public class ResultPanel extends ContentPanel
    {
      private static final int DEFAULT_PANEL_WIDTH = 1000;
      private static final int DEFAULT_COLUMN_WIDTH = 100;

      public ResultPanel()
      {
        setHeaderVisible(false);
        setLayout(new FitLayout());
      }

      public void displayResult(AdminResult result)
      {
        removeAll();
        final Grid<ModelData> grid = new Grid<ModelData>(getStore(result), getColumnModel(result));
        int width = AdminFrame.this.getWidth();
        if (width == 0)
        {
          width = DEFAULT_PANEL_WIDTH;
        }
        if ((result.getColumnNames().size() * DEFAULT_COLUMN_WIDTH) < width)
        {
          grid.setBorders(false);
          grid.getView().setAutoFill(true);
          grid.getView().setForceFit(true);
        }
        grid.addListener(Events.CellDoubleClick, new Listener<GridEvent<?>>()
        {
          @Override
          public void handleEvent(GridEvent<?> gridEvent)
          {
            int columnIndex = gridEvent.getColIndex();
            int rowIndex = gridEvent.getRowIndex();
            Element cell = grid.getView().getCell(rowIndex, columnIndex);
            if (cell != null)
            {
              String text = cell.getInnerText();
              getStatementPanel().getStatementTextArea().insertAtCursor(text, true, 0);
            }
          }
        });
        add(grid);
        doLayout();
      }

      private ColumnModel getColumnModel(AdminResult result)
      {
        List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
        List<String> columns = result.getColumnNames();
        for (String column : columns)
        {
          columnConfigs.add(new ColumnConfig(column, column, DEFAULT_COLUMN_WIDTH));
        }
        ColumnModel columnModel = new ColumnModel(columnConfigs);
        return columnModel;
      }

      private ListStore<ModelData> getStore(AdminResult result)
      {
        ListStore<ModelData> listStore = new ListStore<ModelData>();
        listStore.add(result.getRows());
        return listStore;
      }
    }

    public class StatementPanel extends ContentPanel
    {
      private Button runSqlButton;
      private PositionRetainingTextArea statementTextArea;
      private ToolBar toolBar;

      public StatementPanel()
      {
        setHeaderVisible(false);
        setLayout(new FitLayout());
        add(getStatementTextArea());
        setBottomComponent(getToolBar());
      }

      private Button getRunSqlButton()
      {
        if (runSqlButton == null)
        {
          runSqlButton = new Button("Run");
          runSqlButton.setIcon(Resources.SQL_RUN);
          runSqlButton.addSelectionListener(new RunButtonListener());
        }
        return runSqlButton;
      }

      private PositionRetainingTextArea getStatementTextArea()
      {
        if (statementTextArea == null)
        {
          statementTextArea = new PositionRetainingTextArea();
          statementTextArea.setBorders(false);
        }
        return statementTextArea;
      }

      private ToolBar getToolBar()
      {
        if (toolBar == null)
        {
          toolBar = new ToolBar();
          toolBar.add(getRunSqlButton());
        }
        return toolBar;
      }

      private void runSql()
      {
        String command = getStatementTextArea().getValue();
        if (command == null)
        {
          MessageBox.alert("Missing Statement", "Please enter an SQL statement and try again", null);
          return;
        }
        AdminRequest adminRequest = new AdminRequest(command);
        Directory.getConnectorService().executeAdminRequest(Utility.getAuthenticationToken(), adminRequest, new AdminCallback());
      }

      private final class AdminCallback extends FailureReportingAsyncCallback<AdminResult>
      {
        @Override
        public void onSuccess(AdminResult result)
        {
          displayResult(result);
        }
      }

      private final class RunButtonListener extends SelectionListener<ButtonEvent>
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          runSql();
        }
      }
    }
  }

  public class UsersTabItem extends TabItem
  {
  }

}
