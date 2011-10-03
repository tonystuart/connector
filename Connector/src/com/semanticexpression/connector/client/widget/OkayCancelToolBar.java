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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.semanticexpression.connector.client.icons.Resources;

public final class OkayCancelToolBar extends ToolBar
{
  protected Button cancelButton;
  protected Button okayButton;
  private OkayCancelHandler okayCancelHandler;

  public OkayCancelToolBar(OkayCancelHandler okayCancelHandler)
  {
    this.okayCancelHandler = okayCancelHandler;

    add(new FillToolItem());
    add(getCancelButton());
    add(getOkayButton());
  }

  public Button getCancelButton()
  {
    if (cancelButton == null)
    {
      cancelButton = new Button("Cancel");
      cancelButton.setIcon(Resources.CANCEL);
      cancelButton.addSelectionListener(new CancelButtonSelectionListener());
    }
    return cancelButton;
  }

  public Button getOkayButton()
  {
    if (okayButton == null)
    {
      okayButton = new Button("Okay");
      okayButton.setIcon(Resources.OKAY);
      okayButton.addSelectionListener(new OkayButtonSelectionListener());
    }
    return okayButton;
  }

  private class CancelButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      okayCancelHandler.cancel();
    }
  }

  private class OkayButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      okayCancelHandler.okay();
    }
  }

  public interface OkayCancelHandler
  {

    public void cancel();

    public void okay();

  }

}
