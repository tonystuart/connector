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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.enums.ContentType;

public class InsertLocationMenu extends Menu
{
  private MenuItem afterMenuItem;
  private MenuItem beforeMenuItem;
  private MenuItem childMenuItem;
  private EditorFrame editorFrame;
  private ContentType menuContentType;
  private MenuItemListener menuItemListener;
  private MenuItem parentMenuItem;

  public InsertLocationMenu(EditorFrame editorFrame, ContentType menuContentType)
  {
    this.editorFrame = editorFrame;
    this.menuContentType = menuContentType;

    add(getAfterMenuItem());
    add(getBeforeMenuItem());
    add(getChildMenuItem());
    add(getParentMenuItem());
    addListener(Events.BeforeShow, new BeforeShowListener());
  }

  public MenuItem getAfterMenuItem()
  {
    if (afterMenuItem == null)
    {
      afterMenuItem = new MenuItem("After");
      afterMenuItem.setIcon(Resources.AFTER);
      afterMenuItem.setData(Keys.LOCATION_TYPE, InsertLocationType.AFTER);
      afterMenuItem.addSelectionListener(getLocationMenuItemSelectionListener());
    }
    return afterMenuItem;
  }

  public MenuItem getBeforeMenuItem()
  {
    if (beforeMenuItem == null)
    {
      beforeMenuItem = new MenuItem("Before");
      beforeMenuItem.setIcon(Resources.BEFORE);
      beforeMenuItem.setData(Keys.LOCATION_TYPE, InsertLocationType.BEFORE);
      beforeMenuItem.addSelectionListener(getLocationMenuItemSelectionListener());
    }
    return beforeMenuItem;
  }

  public MenuItem getChildMenuItem()
  {
    if (childMenuItem == null)
    {
      childMenuItem = new MenuItem("Child");
      childMenuItem.setIcon(Resources.CHILD);
      childMenuItem.setData(Keys.LOCATION_TYPE, InsertLocationType.CHILD);
      childMenuItem.addSelectionListener(getLocationMenuItemSelectionListener());
    }
    return childMenuItem;
  }

  public MenuItem getFirstEnabledItem()
  {
    MenuItem firstEnabledItem = null;
    if (afterMenuItem.isEnabled())
    {
      firstEnabledItem = afterMenuItem;
    }
    else if (beforeMenuItem.isEnabled())
    {
      firstEnabledItem = beforeMenuItem;
    }
    else if (childMenuItem.isEnabled())
    {
      firstEnabledItem = childMenuItem;
    }
    else if (parentMenuItem.isEnabled())
    {
      firstEnabledItem = parentMenuItem;
    }
    return firstEnabledItem;
  }

  public MenuItemListener getLocationMenuItemSelectionListener()
  {
    if (menuItemListener == null)
    {
      menuItemListener = new MenuItemListener();
    }
    return menuItemListener;
  }

  public MenuItem getParentMenuItem()
  {
    if (parentMenuItem == null)
    {
      parentMenuItem = new MenuItem("Parent");
      parentMenuItem.setIcon(Resources.PARENT);
      parentMenuItem.setData(Keys.LOCATION_TYPE, InsertLocationType.PARENT);
      parentMenuItem.addSelectionListener(getLocationMenuItemSelectionListener());
    }
    return parentMenuItem;
  }

  public void updateMenuState()
  {
    boolean isAfterEnabled = false;
    boolean isBeforeEnabled = false;
    boolean isChildEnabled = false;
    boolean isParentEnabled = false;

    ContentReference contentReference = editorFrame.getOutlineTreePanel().getSelectionModel().getSelectedItem();
    if (contentReference == null)
    {
      // Empty editor, holds anything, after yields correct insert index
      isAfterEnabled = true;
    }
    else if (contentReference != null)
    {
      boolean hasDocumentRoot = editorFrame.getOutlineTreeStore().hasDocumentRoot();
      boolean isRoot = editorFrame.getOutlineTreeStore().getParent(contentReference) == null;
      
      if (!hasDocumentRoot || !isRoot)
      {
        isAfterEnabled = true;
        isBeforeEnabled = true;
      }

      ContentType modelDataContentType = contentReference.get(Keys.CONTENT_TYPE);

      if (modelDataContentType == ContentType.DOCUMENT)
      {
        isChildEnabled = true;
      }
      if (menuContentType == ContentType.DOCUMENT)
      {
        isParentEnabled = true;
      }
    }

    getAfterMenuItem().setEnabled(isAfterEnabled);
    getBeforeMenuItem().setEnabled(isBeforeEnabled);
    getChildMenuItem().setEnabled(isChildEnabled);
    getParentMenuItem().setEnabled(isParentEnabled);
  }

  private final class BeforeShowListener implements Listener<MenuEvent>
  {
    @Override
    public void handleEvent(MenuEvent be)
    {
      // Note that documentContentPanel is null in WindowBuilder
      if (editorFrame != null)
      {
        updateMenuState();
      }
    }

  }

  private class MenuItemListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent menuEvent)
    {
      MenuItem menuItem = (MenuItem)menuEvent.getItem();
      InsertLocationType insertLocationType = menuItem.getData(Keys.LOCATION_TYPE);
      editorFrame.addContent(menuContentType, insertLocationType);
    }
  }

}
