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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;

public class NullSelectionEditor extends LayoutContainer implements DetailsPanelComponent
{
  public NullSelectionEditor()
  {
    setLayout(new CenterLayout());
    add(new Html("Please select an item in the Outline view to see details here."));
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
