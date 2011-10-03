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

package com.semanticexpression.connector.client.frame.editor.style;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.semanticexpression.connector.client.frame.editor.text.CommandProcessor;
import com.semanticexpression.connector.client.icons.Resources;

public class StyleMenu extends Menu
{
  private MenuItem applyMenuItem;
  private MenuItem clearAllMenuItem;
  private MenuItem clearMenuItem;
  private CommandProcessor commandProcessor;
  private MenuItem nextMenuItem;
  private MenuItem previousMenuItem;
  private MenuItem selectAllMenuItem;
  private MenuItem selectMenuItem;
  private MenuItem updateHyperlinkMenuItem;

  public StyleMenu(CommandProcessor commandProcessor)
  {
    this.commandProcessor = commandProcessor;

    add(getApplyMenuItem());
    add(getClearMenuItem());
    add(getClearAllMenuItem());
    add(getSelectMenuItem());
    add(getSelectAllMenuItem());
    add(new SeparatorMenuItem());
    add(getNextMenuItem());
    add(getPreviousMenuItem());
    add(new SeparatorMenuItem());
    add(getUpdateHyperlinkMenuItem());
  }

  public MenuItem getApplyMenuItem()
  {
    if (applyMenuItem == null)
    {
      applyMenuItem = new MenuItem("Apply");
      applyMenuItem.setIcon(Resources.STYLE_APPLY);
      applyMenuItem.addSelectionListener(new ApplyButtonSelectionListener());
    }
    return applyMenuItem;
  }

  public MenuItem getClearAllMenuItem()
  {
    if (clearAllMenuItem == null)
    {
      clearAllMenuItem = new MenuItem("Clear All");
      clearAllMenuItem.setIcon(Resources.STYLE_CLEAR_ALL);
      clearAllMenuItem.addSelectionListener(new ClearAllButtonSelectionListener());
    }
    return clearAllMenuItem;
  }

  public MenuItem getClearMenuItem()
  {
    if (clearMenuItem == null)
    {
      clearMenuItem = new MenuItem("Clear");
      clearMenuItem.setIcon(Resources.STYLE_CLEAR);
      clearMenuItem.addSelectionListener(new ClearButtonSelectionListener());
    }
    return clearMenuItem;
  }

  public MenuItem getNextMenuItem()
  {
    if (nextMenuItem == null)
    {
      nextMenuItem = new MenuItem("Next");
      nextMenuItem.setIcon(Resources.STYLE_NEXT);
      nextMenuItem.addSelectionListener(new NextButtonSelectionListener());
    }
    return nextMenuItem;
  }

  public MenuItem getPreviousMenuItem()
  {
    if (previousMenuItem == null)
    {
      previousMenuItem = new MenuItem("Previous");
      previousMenuItem.setIcon(Resources.STYLE_PREVIOUS);
      previousMenuItem.addSelectionListener(new PreviousButtonSelectionListener());
    }
    return previousMenuItem;
  }

  public MenuItem getSelectAllMenuItem()
  {
    if (selectAllMenuItem == null)
    {
      selectAllMenuItem = new MenuItem("Select All");
      selectAllMenuItem.setIcon(Resources.STYLE_SELECT_ALL);
      selectAllMenuItem.addSelectionListener(new SelectAllButtonSelectionListener());
    }
    return selectAllMenuItem;
  }

  public MenuItem getSelectMenuItem()
  {
    if (selectMenuItem == null)
    {
      selectMenuItem = new MenuItem("Select");
      selectMenuItem.setIcon(Resources.STYLE_SELECT);
      selectMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        @Override
        public void componentSelected(MenuEvent ce)
        {
          commandProcessor.selectTextStylesFromGrid();
        }
      });
    }
    return selectMenuItem;
  }

  public MenuItem getUpdateHyperlinkMenuItem()
  {
    if (updateHyperlinkMenuItem == null)
    {
      updateHyperlinkMenuItem = new MenuItem("Update Hyperlink");
      updateHyperlinkMenuItem.setIcon(Resources.STYLE_EDIT_LINK);
      updateHyperlinkMenuItem.addSelectionListener(new UpdateHyperlinkButtonSelectionListener());
    }
    return updateHyperlinkMenuItem;
  }

  private final class ApplyButtonSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      commandProcessor.apply();
    }
  }

  private final class ClearAllButtonSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      commandProcessor.clearAll();
    }
  }

  private final class ClearButtonSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      commandProcessor.clear();
    }
  }

  private final class NextButtonSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      commandProcessor.find(true);
    }
  }

  private final class PreviousButtonSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      commandProcessor.find(false);
    }
  }

  private final class SelectAllButtonSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      commandProcessor.selectAll();
    }
  }

  private final class UpdateHyperlinkButtonSelectionListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      commandProcessor.updateHyperlink();
    }
  }

}
