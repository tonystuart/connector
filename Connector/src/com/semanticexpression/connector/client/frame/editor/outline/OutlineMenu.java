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
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.OpenBrowserType;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.enums.ContentType;

public class OutlineMenu extends Menu
{
  private MenuItem chartMenu;
  private final EditorFrame editorFrame;
  private MenuItem imageMenu;
  private MenuSelectionListener menuSelectionListener = new MenuSelectionListener();
  private MenuItem openBrowserDocumentMenuItem;
  private MenuItem openBrowserWebPageMenuItem;
  private MenuItem publishMenuItem;
  private MenuItem removeMenuItem;
  private MenuItem sectionMenu;
  private MenuItem sendMenuItem;
  private MenuItem styleMenu;
  private MenuItem tableMenu;
  private MenuItem textMenu;
  private MenuItem workflowMenu;

  public OutlineMenu(EditorFrame editorFrame)
  {
    this.editorFrame = editorFrame;
    add(getChartMenu());
    add(getImageMenu());
    add(getSectionMenu());
    add(getStyleMenu());
    add(getTableMenu());
    add(getTextMenu());
    add(getWorkflowMenu());
    add(new SeparatorMenuItem());
    add(getRemoveMenuItem());
    add(new SeparatorMenuItem());
    add(getOpenBrowserDocumentMenuItem());
    add(getOpenBrowserWebPageMenuItem());
    add(new SeparatorMenuItem());
    add(getSendMenuItem());
    add(getPublishMenuItem());
  }

  public MenuItem getChartMenu()
  {
    if (chartMenu == null)
    {
      chartMenu = new MenuItem("Add Chart");
      chartMenu.setIcon(Resources.CHART);
      chartMenu.addSelectionListener(menuSelectionListener);
      chartMenu.setSubMenu(new InsertLocationMenu(editorFrame, ContentType.CHART));
    }
    return chartMenu;
  }

  public MenuItem getImageMenu()
  {
    if (imageMenu == null)
    {
      imageMenu = new MenuItem("Add Image");
      imageMenu.setIcon(Resources.IMAGE);
      imageMenu.addSelectionListener(menuSelectionListener);
      imageMenu.setSubMenu(new InsertLocationMenu(editorFrame, ContentType.IMAGE));
    }
    return imageMenu;
  }

  public MenuItem getOpenBrowserDocumentMenuItem()
  {
    if (openBrowserDocumentMenuItem == null)
    {
      openBrowserDocumentMenuItem = new MenuItem("Browse as Document");
      openBrowserDocumentMenuItem.setIcon(Resources.OPEN_BROWSER_DOCUMENT);
      openBrowserDocumentMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        @Override
        public void componentSelected(MenuEvent ce)
        {
          editorFrame.openSelectedContent(OpenBrowserType.DOCUMENT);
        }
      });
    }
    return openBrowserDocumentMenuItem;
  }

  public MenuItem getOpenBrowserWebPageMenuItem()
  {
    if (openBrowserWebPageMenuItem == null)
    {
      openBrowserWebPageMenuItem = new MenuItem("Browse as Pages");
      openBrowserWebPageMenuItem.setIcon(Resources.OPEN_BROWSER_WEB_PAGE);
      openBrowserWebPageMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        @Override
        public void componentSelected(MenuEvent ce)
        {
          editorFrame.openSelectedContent(OpenBrowserType.WEB_PAGE);
        }
      });
    }
    return openBrowserWebPageMenuItem;
  }

  public MenuItem getPublishMenuItem()
  {
    if (publishMenuItem == null)
    {
      publishMenuItem = new MenuItem("Publish");
      publishMenuItem.setIcon(Resources.PUBLISH);
      publishMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        @Override
        public void componentSelected(MenuEvent ce)
        {
          editorFrame.publishSelectedContent();
        }
      });
    }
    return publishMenuItem;
  }

  public MenuItem getRemoveMenuItem()
  {
    if (removeMenuItem == null)
    {
      removeMenuItem = new MenuItem("Remove");
      removeMenuItem.setIcon(Resources.CONTENT_REMOVE);
      removeMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        @Override
        public void componentSelected(MenuEvent ce)
        {
          editorFrame.removeSelectedContent();
        }
      });
    }
    return removeMenuItem;
  }

  public MenuItem getSectionMenu()
  {
    if (sectionMenu == null)
    {
      sectionMenu = new MenuItem("Add Section");
      sectionMenu.setIcon(Resources.SECTION);
      sectionMenu.addSelectionListener(menuSelectionListener);
      sectionMenu.setSubMenu(new InsertLocationMenu(editorFrame, ContentType.DOCUMENT));
    }
    return sectionMenu;
  }

  public MenuItem getSendMenuItem()
  {
    if (sendMenuItem == null)
    {
      sendMenuItem = new MenuItem("Send as E-Mail");
      sendMenuItem.setIcon(Resources.SEND);
      sendMenuItem.setEnabled(false);
      sendMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        @Override
        public void componentSelected(MenuEvent ce)
        {
          editorFrame.sendSelectedContent();
        }
      });
    }
    return sendMenuItem;
  }

  public MenuItem getStyleMenu()
  {
    if (styleMenu == null)
    {
      styleMenu = new MenuItem("Add Style");
      styleMenu.setIcon(Resources.STYLE);
      styleMenu.addSelectionListener(menuSelectionListener);
      styleMenu.setSubMenu(new InsertLocationMenu(editorFrame, ContentType.STYLE));
    }
    return styleMenu;
  }

  public MenuItem getTableMenu()
  {
    if (tableMenu == null)
    {
      tableMenu = new MenuItem("Add Table");
      tableMenu.setIcon(Resources.TABLE);
      tableMenu.addSelectionListener(menuSelectionListener);
      tableMenu.setSubMenu(new InsertLocationMenu(editorFrame, ContentType.TABLE));
    }
    return tableMenu;
  }

  public MenuItem getTextMenu()
  {
    if (textMenu == null)
    {
      textMenu = new MenuItem("Add Text");
      textMenu.setIcon(Resources.TEXT);
      textMenu.addSelectionListener(menuSelectionListener);
      textMenu.setSubMenu(new InsertLocationMenu(editorFrame, ContentType.TEXT));
    }
    return textMenu;
  }

  public MenuItem getWorkflowMenu()
  {
    if (workflowMenu == null)
    {
      workflowMenu = new MenuItem("Add Workflow");
      workflowMenu.setIcon(Resources.WORKFLOW);
      workflowMenu.addSelectionListener(menuSelectionListener);
      workflowMenu.setSubMenu(new InsertLocationMenu(editorFrame, ContentType.WORKFLOW));
    }
    return workflowMenu;
  }

  private final class MenuSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent menuEvent)
    {
      Component item = menuEvent.getItem();
      if (item instanceof MenuItem)
      {
        MenuItem menuItem = (MenuItem)item;
        Menu subMenu = menuItem.getSubMenu();
        if (subMenu instanceof InsertLocationMenu)
        {
          InsertLocationMenu insertLocationMenu = (InsertLocationMenu)subMenu;
          MenuItem firstEnabledItem = insertLocationMenu.getFirstEnabledItem();
          menuEvent.setItem(firstEnabledItem);
          firstEnabledItem.fireEvent(Events.Select, menuEvent);
        }
      }
    }
  }

}