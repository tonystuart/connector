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

package com.semanticexpression.connector.client.frame.editor;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.semanticexpression.connector.client.widget.Framelet;

public abstract class BaseEditor extends Framelet
{
  public BaseEditor(String title, AbstractImagePrototype icon)
  {
    super(title, icon);
    addListener(Events.Detach, new DetachListener());
  }

  protected abstract void onMinimizeOrClose();

  private final class DetachListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      onMinimizeOrClose();
    }
  }

}