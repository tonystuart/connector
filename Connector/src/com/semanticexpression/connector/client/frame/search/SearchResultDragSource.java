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

package com.semanticexpression.connector.client.frame.search;

import java.util.List;

import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.semanticexpression.connector.shared.SearchResult;

public class SearchResultDragSource extends GridDragSource
{
  public SearchResultDragSource(Grid<SearchResult> grid)
  {
    super(grid);
    setStatusText("Copying {0} item(s)<br/>Press CTRL+SHIFT for Link");
  }

  @Override
  protected void onDragDrop(DNDEvent event)
  {
    event.setOperation(Operation.COPY);
    super.onDragDrop(event);
  }

  @Override
  public List<SearchResult> getData()
  {
    @SuppressWarnings("unchecked")
    List<SearchResult> searchResults = (List<SearchResult>)super.getData();
    return searchResults;
  }
}
