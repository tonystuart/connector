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

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;

public class OptionTreePanel extends TreePanel<ModelData>
{
  protected static final String NAME = "name";

  public OptionTreePanel()
  {
    super(new TreeStore<ModelData>());
    store.add(getRoot().getChildren(), true);
    setDisplayProperty(NAME);
    setCheckable(true);
    setCheckStyle(CheckCascade.CHILDREN);
    setStyleAttribute("background-color", "white");
  }

  protected BaseTreeModel createTreeModel(BaseTreeModel parent, String name)
  {
    BaseTreeModel baseTreeModel = new BaseTreeModel();
    baseTreeModel.set(NAME, name);
    if (parent != null)
    {
      parent.add(baseTreeModel);
    }
    return baseTreeModel;
  }

  protected BaseTreeModel getRoot()
  {
    return null;
  }

}