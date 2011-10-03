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

package com.semanticexpression.connector.client.frame.editor.relationship;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.DetailsPanelComponent;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.services.OpenExistingContentServiceRequest;
import com.semanticexpression.connector.client.widget.EnumIconRenderer;
import com.semanticexpression.connector.client.widget.Framelet;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Relationship;

public final class RelationshipDetails extends Framelet implements DetailsPanelComponent
{
  private ColumnModel columnModel;
  private ContentReference contentReference;
  private Menu frameletMenu;
  private Grid<Relationship> grid;
  private Menu gridContextMenu;
  private ListStore<Relationship> listStore;
  private ColumnConfig relationshipTypeColumnConfig;

  public RelationshipDetails(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    super("Relationships", Resources.RELATIONSHIP);
    setFrameletMenu(getFrameletMenu());
    add(getGrid());
  }

  @Override
  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    Id contentId = contentReference.getId();
    display(contentId);
  }

  private void display(Id contentId)
  {
    setDescription(contentId.formatString());
    Directory.getConnectorService().getRelationships(Utility.getAuthenticationToken(), contentId, new FailureReportingAsyncCallback<List<Relationship>>()
    {
      @Override
      public void onSuccess(List<Relationship> relationships)
      {
        getListStore().removeAll();
        if (relationships != null) // null indicates unsaved content
        {
          getListStore().add(relationships);
        }
      }
    });
  }

  public void drill()
  {
    Relationship relationship = getGrid().getSelectionModel().getSelectedItem();
    if (relationship == null)
    {
      Utility.displaySelectionMessageBox();
    }
    else
    {
      Id contentId = relationship.get(Keys.CONTENT_ID);
      display(contentId);
    }
  }

  public ColumnModel getColumnModel()
  {
    if (columnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      columnConfigs.add(getRelationshipTypeColumnConfig());
      columnConfigs.add(Utility.createDateTimeColumnConfig(Keys.CREATED_AT, "Date", 100));
      columnConfigs.add(new ColumnConfig(Keys.CREATED_BY, "User", 100));
      columnConfigs.add(new ColumnConfig(Keys.TITLE, "Title", 100));
      columnConfigs.add(new ColumnConfig(Keys.CONTENT_ID, "Id", 100));
      columnModel = new ColumnModel(columnConfigs);
    }
    return columnModel;
  }

  public Menu getFrameletMenu()
  {
    if (frameletMenu == null)
    {
      frameletMenu = new RelationshipMenu();
    }
    return frameletMenu;
  }

  public Grid<Relationship> getGrid()
  {
    if (grid == null)
    {
      grid = new Grid<Relationship>(getListStore(), getColumnModel());
      grid.setContextMenu(getGridContextMenu());
      grid.setBorders(false);
      grid.getView().setAutoFill(true);
      grid.getView().setForceFit(true);
      grid.getView().setEmptyText("This content is original and currently has no derivations.");
      grid.addListener(Events.RowDoubleClick, new RowDoubleClickListener());
    }
    return grid;
  }

  public Menu getGridContextMenu()
  {
    if (gridContextMenu == null)
    {
      gridContextMenu = new RelationshipMenu();
    }
    return gridContextMenu;
  }

  public ListStore<Relationship> getListStore()
  {
    if (listStore == null)
    {
      listStore = new ListStore<Relationship>();
    }
    return listStore;
  }

  public ColumnConfig getRelationshipTypeColumnConfig()
  {
    if (relationshipTypeColumnConfig == null)
    {
      relationshipTypeColumnConfig = new ColumnConfig(Keys.RELATIONSHIP_TYPE, "Type", 40);
      relationshipTypeColumnConfig.setAlignment(HorizontalAlignment.CENTER);
      relationshipTypeColumnConfig.setRenderer(new EnumIconRenderer());
    }
    return relationshipTypeColumnConfig;
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  public void open()
  {
    Relationship relationship = getGrid().getSelectionModel().getSelectedItem();
    if (relationship == null)
    {
      Utility.displaySelectionMessageBox();
    }
    else
    {
      Id contentId = relationship.get(Keys.CONTENT_ID);
      String title = relationship.get(Keys.TITLE);
      Directory.getServiceBus().invoke(new OpenExistingContentServiceRequest(contentId, title));
    }
  }

  public void reset()
  {
    display(contentReference);
  }

  @Override
  public void saveChanges()
  {
  }

  private final class RelationshipMenu extends Menu
  {
    private MenuItem drillMenuItem;
    protected MenuItem openMenuItem;
    private MenuItem resetMenuItem;

    public RelationshipMenu()
    {
      add(getDrillMenuItem());
      add(getResetMenuItem());
      add(getOpenMenuItem());
    }

    public MenuItem getDrillMenuItem()
    {
      if (drillMenuItem == null)
      {
        drillMenuItem = new MenuItem("Drill");
        drillMenuItem.setIcon(Resources.RELATIONSHIP_DRILL);
        drillMenuItem.addSelectionListener(new DrillListener());
      }
      return drillMenuItem;
    }

    public MenuItem getOpenMenuItem()
    {
      if (openMenuItem == null)
      {
        openMenuItem = new MenuItem("Open");
        openMenuItem.setIcon(Resources.OPEN_IN_EDITOR);
        openMenuItem.addSelectionListener(new OpenListener());
      }
      return openMenuItem;
    }

    public MenuItem getResetMenuItem()
    {
      if (resetMenuItem == null)
      {
        resetMenuItem = new MenuItem("Reset");
        resetMenuItem.setIcon(Resources.RELATIONSHIP_RESET);
        resetMenuItem.addSelectionListener(new ResetListener());
      }
      return resetMenuItem;
    }

    private final class DrillListener extends SelectionListener<MenuEvent>
    {
      public void componentSelected(MenuEvent ce)
      {
        drill();
      }
    }

    private final class OpenListener extends SelectionListener<MenuEvent>
    {
      public void componentSelected(MenuEvent ce)
      {
        open();
      }
    }

    private final class ResetListener extends SelectionListener<MenuEvent>
    {
      public void componentSelected(MenuEvent ce)
      {
        reset();
      }
    }

  }

  private final class RowDoubleClickListener implements Listener<GridEvent<Relationship>>
  {
    @Override
    public void handleEvent(GridEvent<Relationship> be)
    {
      drill();
    }
  }
}