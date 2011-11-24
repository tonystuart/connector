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

package com.semanticexpression.connector.client.frame.editor.image;

import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.widget.ConnectorWindow;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;

public class BaseImagePropertyEditorWindow extends ConnectorWindow implements OkayCancelHandler
{
  protected ContentReference contentReference;
  private OkayCancelToolBar okayCancelToolBar;

  public BaseImagePropertyEditorWindow()
  {
    super();
    setBottomComponent(getOkayCancelToolBar());
  }

  @Override
  public void cancel()
  {
    hide();
  }

  public void edit(ContentReference contentReference)
  {
    this.contentReference = contentReference;
  }

  private OkayCancelToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(this);
    }
    return okayCancelToolBar;
  }

  @Override
  public void okay()
  {
    hide();
  }

}