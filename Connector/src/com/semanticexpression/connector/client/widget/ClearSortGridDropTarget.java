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

import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.grid.Grid;

public final class ClearSortGridDropTarget extends GridDropTarget
{
  public ClearSortGridDropTarget(Grid<?> grid)
  {
    super(grid);
  }

  @Override
  protected void onDragDrop(DNDEvent e)
  {
    if (getGrid() instanceof ClearSortGrid<?>)
    {
      ((ClearSortGrid<?>)getGrid()).clearColumnHeaderSort();
    }
    super.onDragDrop(e);
  }
}