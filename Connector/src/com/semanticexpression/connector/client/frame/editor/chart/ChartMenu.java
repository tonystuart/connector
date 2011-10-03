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

package com.semanticexpression.connector.client.frame.editor.chart;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.semanticexpression.connector.client.frame.editor.image.BaseImageMenu;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.enums.ChartType;

public final class ChartMenu extends BaseImageMenu
{
  public ChartMenu(ChartFramelet chartFramelet)
  {
    super(chartFramelet);
    add(new SeparatorMenuItem());
    add(new MenuItem("Bar", Resources.CHART_BAR, new ChartTypeListener(ChartType.BAR)));
    add(new MenuItem("Line", Resources.CHART_LINE, new ChartTypeListener(ChartType.LINE)));
    add(new MenuItem("Pie", Resources.CHART_PIE, new ChartTypeListener(ChartType.PIE)));
  }

  protected ChartFramelet getChartImageFramelet()
  {
    return (ChartFramelet)baseImageFramelet;
  }

  private final class ChartTypeListener extends SelectionListener<MenuEvent>
  {
    private ChartType chartType;

    public ChartTypeListener(ChartType chartType)
    {
      this.chartType = chartType;
    }

    @Override
    public void componentSelected(MenuEvent ce)
    {
      getChartImageFramelet().changeType(chartType);
    }
  }

}