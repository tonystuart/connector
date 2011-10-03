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

package com.semanticexpression.connector.client.frame.editor.access;

import com.extjs.gxt.ui.client.widget.Component;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.DetailsPanelComponent;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Framelet;

public final class AccessDetails extends Framelet implements DetailsPanelComponent
{
  public AccessDetails(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    super("Access", Resources.ACCESS);
  }

  @Override
  public void display(ContentReference contentReference)
  {
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  @Override
  public void saveChanges()
  {
  }
}