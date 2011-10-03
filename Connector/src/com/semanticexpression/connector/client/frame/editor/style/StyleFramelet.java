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

package com.semanticexpression.connector.client.frame.editor.style;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.text.CommandProcessor;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Framelet;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public class StyleFramelet extends Framelet
{
  private CommandProcessor commandProcessor;
  private StyleMenu contextStyleMenu;
  private StyleMenu frameletMenu;
  private ColumnModel styleColumnModel;
  private Grid<Association> styleGrid;
  private ListStore<Association> styleListStore;

  public StyleFramelet(CommandProcessor commandProcessor)
  {
    super("Style", Resources.STYLE);
    this.commandProcessor = commandProcessor;
    add(getStyleGrid());
    setFrameletMenu(getFrameletMenu());
  }

  private Menu getContextStyleMenu()
  {
    if (contextStyleMenu == null)
    {
      contextStyleMenu = new StyleMenu(commandProcessor);
    }
    return contextStyleMenu;
  }

  private Menu getFrameletMenu()
  {
    if (frameletMenu == null)
    {
      frameletMenu = new StyleMenu(commandProcessor);
    }
    return frameletMenu;
  }

  private ColumnModel getStyleColumnModel()
  {
    if (styleColumnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      ColumnConfig columnConfig = new ColumnConfig(Keys.NAME, "Name", 100);
      columnConfig.setRenderer(new GridCellRenderer<ModelData>()
      {
        @Override
        public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
        {
          String html = model.get(property);
          if (Utility.coalesce(model.<Boolean> get(Keys.STYLE_IS_UNDEFINED), false))
          {
            html = "<i>" + html + "</i>";
          }
          return html;
        }
      });
      columnConfigs.add(columnConfig);
      columnConfigs.add(Utility.createColumnConfigYesNo(Keys.STYLE_IS_COMMENT_ENABLED, "Comment Style", 100));
      columnConfigs.add(Utility.createColumnConfigStyleExample(Keys.VALUE, "Example", 100));
      styleColumnModel = new ColumnModel(columnConfigs);
    }
    return styleColumnModel;
  }

  public Grid<Association> getStyleGrid()
  {
    if (styleGrid == null)
    {
      styleGrid = new Grid<Association>(getStyleListStore(), getStyleColumnModel());
      styleGrid.getView().setForceFit(true);
      styleGrid.getView().setAutoFill(true);
      styleGrid.setBorders(false);
      styleGrid.setContextMenu(getContextStyleMenu());
      styleGrid.addListener(Events.RowDoubleClick, new StyleGridDoubleClickListener());
    }
    return styleGrid;
  }

  public ListStore<Association> getStyleListStore()
  {
    if (styleListStore == null)
    {
      styleListStore = new ListStore<Association>();
    }
    return styleListStore;
  }

  private final class StyleGridDoubleClickListener implements Listener<GridEvent<ModelData>>
  {
    @Override
    public void handleEvent(GridEvent<ModelData> e)
    {
      // Automatic toggling is undesirable for styles that nest (e.g. lists)
      if (e.isControlKey())
      {
        commandProcessor.clear();
      }
      else
      {
        commandProcessor.apply();
      }
    }
  }

}
