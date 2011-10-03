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

package com.semanticexpression.connector.client.frame.editor.table;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.semanticexpression.connector.client.icons.Resources;

public class NavigationToolBar extends ToolBar
{
  private Button firstRowButton;
  private Button lastRowButton;
  private NavigationHandler navigationHandler;
  private Button nextRowButton;
  private Button previousRowButton;

  public NavigationToolBar(NavigationHandler navigationHandler)
  {
    this.navigationHandler = navigationHandler;

    add(getFirstRowButton());
    add(getPreviousRowButton());
    add(new FillToolItem());
    add(getNextRowButton());
    add(getLastRowButton());
  }

  private Button getFirstRowButton()
  {
    if (firstRowButton == null)
    {
      firstRowButton = new Button("First");
      firstRowButton.setIcon(Resources.TABLE_ROW_FIRST);
      firstRowButton.addSelectionListener(new SelectionListener<ButtonEvent>()
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          navigationHandler.first();
        }
      });
    }
    return firstRowButton;
  }

  private Button getLastRowButton()
  {
    if (lastRowButton == null)
    {
      lastRowButton = new Button("Last");
      lastRowButton.setIcon(Resources.TABLE_ROW_LAST);
      lastRowButton.addSelectionListener(new SelectionListener<ButtonEvent>()
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          navigationHandler.last();
        }
      });
    }
    return lastRowButton;
  }

  private Button getNextRowButton()
  {
    if (nextRowButton == null)
    {
      nextRowButton = new Button("Next");
      nextRowButton.setIcon(Resources.TABLE_ROW_NEXT);
      nextRowButton.addSelectionListener(new SelectionListener<ButtonEvent>()
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          navigationHandler.next();
        }
      });
    }
    return nextRowButton;
  }

  private Button getPreviousRowButton()
  {
    if (previousRowButton == null)
    {
      previousRowButton = new Button("Previous");
      previousRowButton.setIcon(Resources.TABLE_ROW_PREVIOUS);
      previousRowButton.addSelectionListener(new SelectionListener<ButtonEvent>()
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          navigationHandler.previous();
        }
      });
    }
    return previousRowButton;
  }

  public void updateFormState(int rowOffset, int rowCount)
  {
    getFirstRowButton().setEnabled(rowCount > 0);
    getPreviousRowButton().setEnabled(rowOffset > 0);
    getNextRowButton().setEnabled((rowOffset + 1) < rowCount);
    getLastRowButton().setEnabled(rowCount > 0);
  }

  public interface NavigationHandler
  {

    void first();

    void last();

    void next();

    void previous();

  }
}
