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

package com.semanticexpression.connector.client.wiring;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.store.TreeStore;

public class VisitorTreeStore<M extends ModelData> extends TreeStore<M>
{
  public VisitorTreeStore()
  {
  }

  public VisitorTreeStore(TreeLoader<M> treeLoader)
  {
    super(treeLoader);
  }

  public void visit(StoreVisitor<M> storeVisitor)
  {
    for (M modelData : all)
    {
      if (!storeVisitor.visit(modelData))
      {
        return;
      }
    }
  }
}
