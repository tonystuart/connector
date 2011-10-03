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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;

/**
 * A ContentPanel with no bottom border for use within TabPanel or for use
 * when containing a Grid. It should be used with setBodyBorder(true) which is
 * the default.
 */
public class NoBottomBorderContentPanel extends ContentPanel
{
  @Override
  protected void onRender(com.google.gwt.user.client.Element parent, int pos)
  {
    Component component = getTopComponent();

    // Removing the component border can be done before render to eliminate redrawing
    if (component != null)
    {
      component.setStyleAttribute("borderTop", "none");
    }

    super.onRender(parent, pos);

    // Removing the body border must be done after render (see ContentPanel.onRender: if (!bodyBorder))
    if (component == null)
    {
      getBody().setStyleAttribute("borderBottom", "none");
    }
  }
}