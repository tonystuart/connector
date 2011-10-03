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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.semanticexpression.connector.client.icons.Resources;

public abstract class Wizard extends Window
{
  private Button actionButton;
  private AbstractImagePrototype actionIcon;
  private String actionTitle;
  private int activeItemOffset;
  private CardLayout cardLayout;
  private Button nextButton;
  private Button previousButton;
  private ToolBar toolBar;

  public Wizard(String actionTitle, AbstractImagePrototype actionIcon)
  {
    this.actionTitle = actionTitle;
    this.actionIcon = actionIcon;
    setLayout(getCardLayout());
    setBottomComponent(getToolBar());
  }

  public <T> T addEnterKeyListener(T widget)
  {
    if (widget instanceof TextField)
    {
      ((TextField<?>)widget).addKeyListener(getEnterKeyListener());
    }
    return widget;
  }

  public Button getActionButton()
  {
    if (actionButton == null)
    {
      actionButton = new Button(actionTitle);
      actionButton.setIcon(actionIcon);
      actionButton.addSelectionListener(new ActionButtonSelectionListener());
    }
    return actionButton;
  }

  private CardLayout getCardLayout()
  {
    if (cardLayout == null)
    {
      cardLayout = new CardLayout();
    }
    return cardLayout;
  }

  private KeyListener getEnterKeyListener()
  {
    return new EnterKeyListener();
  }

  private int getItemOffset(Component component)
  {
    int itemCount = getItemCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      if (getItem(itemOffset) == component)
      {
        return itemOffset;
      }
    }
    return -1;
  }

  private int getLastItemOffset()
  {
    return getItemCount() - 1;
  }

  private Button getNextButton()
  {
    if (nextButton == null)
    {
      nextButton = new Button("Next");
      nextButton.setIcon(Resources.NEXT);
      nextButton.addSelectionListener(new NextButtonListener());
    }
    return nextButton;
  }

  private Widget getNextTextField(Component component)
  {
    boolean isReturnNextTextField = false;
    Component activeItem = getCardLayout().getActiveItem();
    if (activeItem instanceof Container<?>)
    {
      Container<?> container = (Container<?>)activeItem;
      int itemCount = container.getItemCount();
      for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
      {
        Object item = container.getItem(itemOffset);
        if (component == item)
        {
          isReturnNextTextField = true;
        }
        else if ((component == null || isReturnNextTextField) && item instanceof TextField)
        {
          return (Widget)item;
        }
      }
    }
    return null;
  }

  private Button getPreviousButton()
  {
    if (previousButton == null)
    {
      previousButton = new Button("Previous");
      previousButton.setIcon(Resources.PREVIOUS);
      previousButton.addSelectionListener(new PreviousButtonListener());
    }
    return previousButton;
  }

  private ToolBar getToolBar()
  {
    if (toolBar == null)
    {
      toolBar = new ToolBar();
      toolBar.setSpacing(5);
      toolBar.add(getPreviousButton());
      toolBar.add(new FillToolItem());
      toolBar.add(getNextButton());
      toolBar.add(getActionButton());
    }
    return toolBar;
  }

  private void next()
  {
    int lastItemOffset = getLastItemOffset();
    if (activeItemOffset < lastItemOffset)
    {
      getCardLayout().setActiveItem(getItem(++activeItemOffset));
      Widget firstTextField = getNextTextField(null);
      setFocusWidget(firstTextField);
    }
    updateButtonState();
  }

  protected abstract void onAction();

  private void onEnter(ComponentEvent event)
  {
    Component component = event.getComponent();
    Widget nextTextField = getNextTextField(component);
    if (nextTextField == null)
    {
      if (activeItemOffset == getLastItemOffset())
      {
        onAction();
      }
      else
      {
        next();
      }
    }
    else
    {
      setFocusWidget(nextTextField);
    }
  }

  private void onKeyDown(ComponentEvent event)
  {
    if (event.getKeyCode() == KeyCodes.KEY_ENTER)
    {
      onEnter(event);
    }

  }

  private void previous()
  {
    if (activeItemOffset > 0)
    {
      getCardLayout().setActiveItem(getItem(--activeItemOffset));
      Widget firstTextField = getNextTextField(null);
      setFocusWidget(firstTextField);
    }
    updateButtonState();
  }

  public void setActiveItem(Component component)
  {
    getCardLayout().setActiveItem(component);
    activeItemOffset = getItemOffset(component);
    updateButtonState();
  }

  @Override
  public void setFocusWidget(Widget focusWidget)
  {
    // Could ascend parent list to find wizard's child if necessary
    Widget parent = focusWidget.getParent();
    if (parent instanceof LayoutContainer && parent != getCardLayout().getActiveItem())
    {
      setActiveItem((LayoutContainer)parent);
    }
    super.setFocusWidget(focusWidget);
    doFocus();
  }

  public void updateButtonState()
  {
    getPreviousButton().setEnabled(activeItemOffset > 0);
    getNextButton().setEnabled(activeItemOffset < getLastItemOffset());
  }

  private class ActionButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      onAction();
    }
  }

  private final class EnterKeyListener extends KeyListener
  {
    @Override
    public void componentKeyDown(ComponentEvent event)
    {
      onKeyDown(event);
    }

  }

  private final class NextButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      next();
    }
  }

  private final class PreviousButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      previous();
    }
  }

}
