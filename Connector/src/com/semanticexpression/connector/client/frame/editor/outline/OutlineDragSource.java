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

package com.semanticexpression.connector.client.frame.editor.outline;

import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.event.DNDEvent;

public final class OutlineDragSource extends TreePanelDragSource
{
  public OutlineDragSource(OutlineTreePanel outlineTreePanel)
  {
    super(outlineTreePanel);
    setStatusText("Moving {0} item(s)<br/>Press CTRL for Copy<br/>Press CTRL+SHIFT for Link");
  }

  @Override
  protected void onDragDrop(DNDEvent event)
  {
    if (event.isControlKey()) // copy or link
    {
      event.setOperation(Operation.COPY);
    }
    super.onDragDrop(event);
  }
}