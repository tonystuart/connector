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

package com.semanticexpression.connector.client.frame.monitor;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.events.StatusEvent;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Frame;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Status;

public class MonitorFrame extends Frame
{
  private static final int DEFAULT_MAXIMUM_ROWS = 100;
  private static final boolean DEFAULT_TRACK_LATEST = true;

  private ColumnModel columnModel;
  private MonitorMenu frameMenu;
  private Grid<Status> grid;
  private MonitorMenu gridMenu;
  private boolean isTrackLatest = DEFAULT_TRACK_LATEST;
  private ListStore<Status> listStore;
  private int maximumRows = DEFAULT_MAXIMUM_ROWS;
  private MonitorPropertiesWindow monitorPropertiesWindow;

  public MonitorFrame()
  {
    Directory.getEventBus().addListener(StatusEvent.class, new EventListener<StatusEvent>()
    {
      @Override
      public void onEventNotification(StatusEvent statusEvent)
      {
        addStatus(statusEvent);
      }
    });
  }

  private void addStatus(Status status)
  {
    getListStore().add(status);
    if (isInitialized() && isTrackLatest)
    {
      getGrid().getSelectionModel().select(status, false);
      getGrid().getView().ensureVisible(getListStore().indexOf(status), 0, false);
    }
  }

  private void addStatus(StatusEvent statusEvent)
  {
    List<Status> statusList = statusEvent.getStatusList();
    for (Status status : statusList)
    {
      addStatus(status);
    }

    prune();
  }

  private ColumnModel getColumnModel()
  {
    if (columnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      columnConfigs.add(Utility.createDateTimeColumnConfig(Keys.CREATED_AT, "Time", 100));
      columnConfigs.add(new ColumnConfig(Keys.CREATED_BY, "User", 100));
      columnConfigs.add(new ColumnConfig(Keys.ACTION, "Event", 100));
      columnConfigs.add(new ColumnConfig(Keys.TITLE, "Title", 100));
      columnConfigs.add(new ColumnConfig(Keys.CONTENT_ID, "Id", 100));
      columnModel = new ColumnModel(columnConfigs);
    }
    return columnModel;
  }

  private MonitorMenu getFrameMenu()
  {
    if (frameMenu == null)
    {
      frameMenu = new MonitorMenu();
    }
    return frameMenu;
  }

  public Grid<Status> getGrid()
  {
    if (grid == null)
    {
      grid = new Grid<Status>(getListStore(), getColumnModel());
      grid.setContextMenu(getGridMenu());
      grid.getView().setForceFit(true);
      grid.getView().setAutoFill(true);
      grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    return grid;
  }

  private MonitorMenu getGridMenu()
  {
    if (gridMenu == null)
    {
      gridMenu = new MonitorMenu();
    }
    return gridMenu;
  }

  private ListStore<Status> getListStore()
  {
    if (listStore == null)
    {
      listStore = new ListStore<Status>();
    }
    return listStore;
  }

  private MonitorPropertiesWindow getMonitorPropertiesWindow()
  {
    if (monitorPropertiesWindow == null)
    {
      monitorPropertiesWindow = new MonitorPropertiesWindow(this);
    }
    return monitorPropertiesWindow;
  }

  private boolean isInitialized()
  {
    return grid != null;
  }

  protected void onMonitorProperties()
  {
    getMonitorPropertiesWindow().show();
    getMonitorPropertiesWindow().toFront();
    getMonitorPropertiesWindow().alignTo(getElement(), "c-c?", null);
    getMonitorPropertiesWindow().display(maximumRows, isTrackLatest);
  }

  protected void onTrackLatest()
  {
  }

  private void prune()
  {
    if (getListStore().getCount() > maximumRows)
    {
      List<Status> statusList = getListStore().getModels();
      Collections.sort(statusList, new Comparator<Status>()
      {
        @Override
        public int compare(Status o1, Status o2)
        {
          Date d1 = o1.get(Keys.CREATED_AT);
          Date d2 = o2.get(Keys.CREATED_AT);
          return d1.compareTo(d2);
        }
      });

      int rowsToRemove = statusList.size() - maximumRows;
      Iterator<Status> iterator = statusList.iterator();
      while (iterator.hasNext() && rowsToRemove-- > 0)
      {
        Status next = iterator.next();
        getListStore().remove(next);
      }
    }
  }

  public void setMaximumRows(int maximumRows)
  {
    this.maximumRows = maximumRows;
    prune();
  }

  public void setTrackLatest(Boolean isTrackLatest)
  {
    this.isTrackLatest = isTrackLatest;
  }

  @Override
  public void show()
  {
    if (!isInitialized())
    {
      setHeading("Monitor");
      setSize(400, 400);
      setIcon(Resources.MONITOR);
      setFrameMenu(getFrameMenu());
      setLayout(new FitLayout());
      add(getGrid());
    }

    super.show();
  }

  public class MonitorMenu extends Menu
  {
    private MenuItem monitorPropertiesMenuItem;

    public MonitorMenu()
    {
      add(getMonitorPropertiesMenuItem());
    }

    private MenuItem getMonitorPropertiesMenuItem()
    {
      if (monitorPropertiesMenuItem == null)
      {
        monitorPropertiesMenuItem = new MenuItem("Monitor Properties");
        monitorPropertiesMenuItem.setIcon(Resources.MONITOR_PROPERTIES);
        monitorPropertiesMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            onMonitorProperties();
          }
        });
      }
      return monitorPropertiesMenuItem;
    }

  }
}
