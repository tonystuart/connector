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

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;

public final class ClearSortEditorGrid<M extends ModelData> extends EditorGrid<M>
{
  public ClearSortEditorGrid(ListStore<M> store, ColumnModel cm)
  {
    super(store, cm);
  }

  public void clearColumnHeaderSort()
  {
    getStore().setStoreSorter(null);
    getStore().setSortField(null);
    getStore().setSortDir(SortDir.NONE);
    getView().getHeader().refresh();
  }
}