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

package com.semanticexpression.connector.client.widget;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.semanticexpression.connector.shared.Keys;

public class ListRenderer implements GridCellRenderer<ModelData>
{
  private List<ModelData> modelMap;

  public ListRenderer(List<ModelData> modelMap)
  {
    this.modelMap = modelMap;
  }

  @Override
  public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
  {
    Object value = model.get(property);
    for (ModelData mapItem : modelMap)
    {
      if (mapItem.get(Keys.VALUE).equals(value))
      {
        return mapItem.get(Keys.NAME);
      }
    }
    return value;
  }

}