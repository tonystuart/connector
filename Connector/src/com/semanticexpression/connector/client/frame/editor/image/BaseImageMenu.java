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
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.icons.Resources;

public class BaseImageMenu extends Menu
{
  protected BaseImageFramelet baseImageFramelet;
  private MenuItem editPropertiesMenuItem;
  private CheckMenuItem scaleToFitCheckMenuItem;

  public BaseImageMenu(BaseImageFramelet baseImageFramelet)
  {
    this.baseImageFramelet = baseImageFramelet;

    add(getEditPropertiesMenuItem());
    add(getScaleToFitCheckMenuItem());
  }

  public MenuItem getEditPropertiesMenuItem()
  {
    if (editPropertiesMenuItem == null)
    {
      editPropertiesMenuItem = new MenuItem("Edit Properties");
      editPropertiesMenuItem.setIcon(Resources.IMAGE_EDIT);
      editPropertiesMenuItem.addSelectionListener(new EditPropertiesListener());
    }
    return editPropertiesMenuItem;
  }

  public CheckMenuItem getScaleToFitCheckMenuItem()
  {
    if (scaleToFitCheckMenuItem == null)
    {
      scaleToFitCheckMenuItem = new CheckMenuItem("Scale to Fit");
      scaleToFitCheckMenuItem.setChecked(BaseImageFramelet.SCALE_TO_FIT);
      scaleToFitCheckMenuItem.addSelectionListener(new ScaleToFitListener());
    }
    return scaleToFitCheckMenuItem;
  }

  private final class EditPropertiesListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      baseImageFramelet.editProperties();
    }
  }

  private final class ScaleToFitListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      baseImageFramelet.setScaleToFit(scaleToFitCheckMenuItem.isChecked());
    }
  }

}
