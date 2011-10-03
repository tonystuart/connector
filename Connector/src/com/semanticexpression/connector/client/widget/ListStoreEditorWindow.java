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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.shared.Association;

public abstract class ListStoreEditorWindow extends Window implements OkayCancelHandler
{
  protected Association association;
  protected boolean isNew;
  protected ListStore<Association> listStore;
  protected OkayCancelToolBar okayCancelToolBar;

  public ListStoreEditorWindow(ListStore<Association> listStore)
  {
    this.listStore = listStore;

    setClosable(false); // tricky to reliably intercept, hide() is invoked during initial render
    setBottomComponent(getOkayCancelToolBar());
  }

  public void cancel()
  {
    cleanup();
    hide();
  }

  protected void cleanup()
  {
    if (isNew)
    {
      if (listStore != null)
      {
        listStore.remove(association);
      }
      isNew = false;
    }
  }

  public void edit(Association association, boolean isNew)
  {
    cleanup();

    this.association = association;
    this.isNew = isNew;
  }

  public OkayCancelToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(this);
    }
    return okayCancelToolBar;
  }

}