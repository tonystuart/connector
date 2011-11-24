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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.button.Button;

public class DefaultButtonWindow extends ConnectorWindow
{
  private Button defaultButton;

  public DefaultButtonWindow()
  {
    new KeyNav<ComponentEvent>(this)
    {
      @Override
      public void onEnter(ComponentEvent ce)
      {
        doDefault();
      }
    };

  }

  protected void doDefault()
  {
    if (defaultButton != null)
    {
      defaultButton.fireEvent(Events.Select);
    }
  }

  public Button getDefaultButton()
  {
    return defaultButton;
  }

  public void setDefaultButton(Button defaultButton)
  {
    this.defaultButton = defaultButton;
  }

}
