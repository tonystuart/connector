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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.icons.Resources;

public final class ImageMenu extends BaseImageMenu
{
  private MenuItem replaceImageMenuItem;

  public ImageMenu(BaseImageFramelet baseImageFramelet)
  {
    super(baseImageFramelet);
    add(getReplaceImageMenuItem());
  }

  protected ImageFramelet getImageFramelet()
  {
    return (ImageFramelet)baseImageFramelet;
  }

  private MenuItem getReplaceImageMenuItem()
  {
    if (replaceImageMenuItem == null)
    {
      replaceImageMenuItem = new MenuItem("Replace Image");
      replaceImageMenuItem.setIcon(Resources.IMAGE_REPLACE);
      replaceImageMenuItem.addSelectionListener(new ReplaceImageListener());
    }
    return replaceImageMenuItem;
  }

  private final class ReplaceImageListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      getImageFramelet().replaceImage();
    }
  }

}
