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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.semanticexpression.connector.client.Connector;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Frame.ActivateEvent;
import com.semanticexpression.connector.client.widget.Frame.CloseEvent;

public class FrameManager extends ContentPanel
{
  public static final EventType Activate = new EventType();
  public static final EventType Close = new EventType();

  public static final int MARGIN_BOTTOM = 10;
  public static final int MARGIN_LEFT = 10;
  public static final int MARGIN_RIGHT = 10;
  public static final int MARGIN_TOP = 10;

  public static final int SPACING = 10;

  private LayoutContainer clientArea;
  private Button closeAllButton;
  private FillToolItem fillToolItem;
  private boolean isMaximized;
  private boolean isTiled;
  private Map<Frame, ToggleButton> map = new LinkedHashMap<Frame, ToggleButton>();
  private ToggleButton tileWindowsToggleButton;
  private ToolBar toolBar;

  public FrameManager()
  {
    setHeaderVisible(false);
    setLayout(new FitLayout());
    setMonitorWindowResize(true);
    setBottomComponent(getToolBar());
    add(getClientArea());
    addCloseHandler();

    isTiled = true;
    getTileWindowsToggleButton().toggle(true);
  }

  protected void activate(Frame frame)
  {
    if (frame.isVisible())
    {
      frame.toFront();
    }
    else
    {
      frame.show();
      if (isMaximized)
      {
        frame.maximize();
      }
      tileCheck();
    }
  }

  private void addCloseHandler()
  {
    com.google.gwt.user.client.Window.addWindowClosingHandler(new ClosingHandler()
    {
      @Override
      public void onWindowClosing(ClosingEvent event)
      {
        event.setMessage(getOnClosingMessage());
      }
    });
  }

  public void addFrame(Frame frame)
  {
    addToggleButton(frame);

    frame.setDragLimit(clientArea);

    frame.addListener(Frame.MinimizeButton, new MinimizeButtonListener());
    frame.addListener(Frame.MaximizeButton, new MaximizeButtonListener());
    frame.addListener(Frame.RestoreButton, new RestoreButtonListener());
    frame.addListener(Frame.CloseButton, new CloseButtonListener());
    frame.addListener(Frame.HeadingChange, new HeadingChangeListener());

    frame.addListener(Events.Activate, new ActivateListener());
    frame.addListener(Events.Deactivate, new DeactivateListener());

    frame.show();

    if (isTiled)
    {
      frame.setTiled(true);
    }

    tileCheck();

    if (isMaximized)
    {
      frame.maximize();
    }
  }

  private void addToggleButton(Frame frame)
  {
    String heading = frame.getHeading();
    String abbreviatedHeading = Utility.abbreviate(heading);

    ToggleButton toggleButton = new ToggleButton(abbreviatedHeading);
    toggleButton.setIcon(frame.getIcon());
    toggleButton.setToolTip(heading);
    toggleButton.addSelectionListener(new ToggleButtonListener());

    map.put(frame, toggleButton);
    int index = map.size() - 1;

    getToolBar().insert(toggleButton, index);
  }

  public void close(Frame frame)
  {
    frame.hide();
    onClose(frame);
  }

  private int getAvailableHeight()
  {
    return clientArea.getHeight() - (MARGIN_TOP + MARGIN_BOTTOM);
  }

  private int getAvailableWidth()
  {
    return clientArea.getWidth() - (MARGIN_LEFT + MARGIN_RIGHT);
  }

  private LayoutContainer getClientArea()
  {
    if (clientArea == null)
    {
      clientArea = new LayoutContainer();
    }
    return clientArea;
  }

  public Button getCloseAllButton()
  {
    if (closeAllButton == null)
    {
      closeAllButton = new Button("Close All");
      // ToolTips on bottom ToolBar obscure button and make it hard to click
      closeAllButton.setIcon(Resources.CLOSE_ALL);
      closeAllButton.addSelectionListener(new CloseAllListener());
    }
    return closeAllButton;
  }

  public FillToolItem getFillToolItem()
  {
    if (fillToolItem == null)
    {
      fillToolItem = new FillToolItem();
    }
    return fillToolItem;
  }

  private Frame getFrame(ToggleButton toggleButton)
  {
    for (Entry<Frame, ToggleButton> entry : map.entrySet())
    {
      if (entry.getValue() == toggleButton)
      {
        return entry.getKey();
      }
    }
    return null;
  }

  public Set<Frame> getFrames()
  {
    return map.keySet();
  }

  private String getModifiedContentList()
  {
    StringBuilder s = new StringBuilder();
    for (Frame frame : map.keySet())
    {
      if (frame.isModified())
      {
        if (s.length() > 0)
        {
          s.append("\n");
        }
        s.append("          ");
        s.append(frame.getHeadingTitle());
      }
    }
    return s.length() > 0 ? s.toString() : null;
  }

  public String getOnClosingMessage()
  {
    String closingMessage = null;
    String modifiedContentList = getModifiedContentList();
    if (modifiedContentList != null)
    {
      StringBuilder s = new StringBuilder();
      s.append("The following window(s) have unsaved changes:\n\n");
      s.append(modifiedContentList);
      s.append("\n\n");
      s.append("These changes will be lost if you press refresh or navigate away from this page.");
      closingMessage = s.toString();
    }
    return closingMessage;
  }

  public ToggleButton getTileWindowsToggleButton()
  {
    if (tileWindowsToggleButton == null)
    {
      tileWindowsToggleButton = new ToggleButton("Tile Windows");
      // ToolTips on bottom ToolBar obscure button and make it hard to click
      tileWindowsToggleButton.setIcon(Resources.TILE);
      tileWindowsToggleButton.addSelectionListener(new TileWindowsToggleButtonSelectionListener());
    }
    return tileWindowsToggleButton;
  }

  public ToolBar getToolBar()
  {
    if (toolBar == null)
    {
      toolBar = new ToolBar();
      toolBar.setSpacing(5);
      toolBar.add(getFillToolItem());
      toolBar.add(new SeparatorToolItem());
      toolBar.add(getCloseAllButton());
      toolBar.add(new SeparatorToolItem());
      toolBar.add(getTileWindowsToggleButton());
    }
    return toolBar;
  }

  private void onActivate(Frame frame)
  {
    ToggleButton toggleButton = map.get(frame);
    toggleButton.toggle(true);

    String heading = frame.getHeading();
    setBrowserTitle(heading);

    frame.fireEvent(Activate, new ActivateEvent(frame, true));
  }

  private void onClose(Frame frame)
  {
    CloseEvent closeEvent = new CloseEvent(this);
    frame.fireEvent(Close, closeEvent);
    if (!closeEvent.isDeferredPendingCallback())
    {
      onCloseContinue(frame);
    }
  }

  public void onCloseAll()
  {
    for (Frame frame : new LinkedList<Frame>(map.keySet()))
    {
      frame.hide();
      onCloseButton(frame);
    }
  }

  private void onCloseButton(Frame frame)
  {
    onClose(frame);
  }

  public void onCloseContinue(Frame frame)
  {
    Button toggleButton = map.get(frame);
    getToolBar().remove(toggleButton);
    map.remove(frame);
    if (map.size() == 0)
    {
      setBrowserTitle(null);
    }
    tileCheck();
  }

  private void onDeactivate(Frame frame)
  {
    ToggleButton toggleButton = map.get(frame);
    toggleButton.toggle(false);
    frame.fireEvent(Activate, new ActivateEvent(frame, false));
  }

  public void onHeadingChange(Frame frame)
  {
    ToggleButton toggleButton = map.get(frame);
    String heading = frame.getHeading();

    if (WindowManager.get().getActive() == frame)
    {
      setBrowserTitle(heading);
    }

    heading = Utility.abbreviate(heading);
    toggleButton.setText(heading);
  }

  private void onMaximizeButton(Frame frame)
  {
    for (Window window : WindowManager.get().getWindows()) // NB: getWindows() does not return hidden Windows
    {
      if (window != frame && window instanceof Frame)
      {
        window.maximize();
      }
    }
    isMaximized = true;
  }

  private void onMinimizeButton(Frame frame)
  {
    frame.setActive(false);
    frame.hide();
    tileCheck();
  }

  @Override
  protected void onResize(int width, int height)
  {
    super.onResize(width, height);
    tileLaterCheck();
  }

  private void onRestoreButton(Frame frame)
  {
    for (Window window : WindowManager.get().getWindows()) // NB: getWindows() does not return hidden Windows
    {
      if (window != frame && window instanceof Frame)
      {
        window.restore();
      }
    }
    isMaximized = false;
    tileCheck();
  }

  public void onTileButton()
  {
    isTiled = getTileWindowsToggleButton().isPressed();

    if (isTiled && isMaximized)
    {
      for (Frame frame : map.keySet())
      {
        frame.restore();
      }
      isMaximized = false;
    }

    for (Frame frame : map.keySet())
    {
      frame.setTiled(isTiled);
    }

    tileCheck();
  }

  public void onToggleButton(ToggleButton toggleButton)
  {
    Frame frame = getFrame(toggleButton);
    boolean isPressed = toggleButton.isPressed();
    if (isPressed)
    {
      activate(frame);
    }
    else
    {
      frame.setActive(false);
      frame.hide();
      tileCheck();
    }
  }

  private void setBrowserTitle(String heading)
  {
    String title;
    if (heading == null)
    {
      title = Connector.TITLE;
    }
    else
    {
      title = Connector.TITLE + " - " + heading;
    }
    com.google.gwt.user.client.Window.setTitle(title);
  }

  private void tile()
  {
    int layoutCount = 0;

    for (Frame frame : map.keySet())
    {
      if (frame.isVisible())
      {
        layoutCount++;
      }
    }

    int availableWidth = getAvailableWidth();
    int availableHeight = getAvailableHeight();

    int rowCount = 1;
    int columnCount = 1;
    int bestFit = Integer.MAX_VALUE;

    for (int testRowCount = 1; testRowCount <= layoutCount; testRowCount++)
    {
      int testColumnCount = (layoutCount + (testRowCount - 1)) / testRowCount;
      int tileWidth = availableWidth / testColumnCount;
      int tileHeight = availableHeight / testRowCount;
      int delta = tileWidth - tileHeight;
      if (delta < 0)
      {
        delta = -delta;
      }
      if (delta < bestFit)
      {
        bestFit = delta;
        rowCount = testRowCount;
        columnCount = testColumnCount;
      }
    }

    int horizontalSpacing = (columnCount - 1) * SPACING;
    int verticalSpacing = (rowCount - 1) * SPACING;

    int tileWidth = (availableWidth - horizontalSpacing) / columnCount;
    int tileHeight = (availableHeight - verticalSpacing) / rowCount;

    int layoutIndex = 0;
    Element clientAreaElement = getClientArea().getElement();

    for (Frame frame : map.keySet())
    {
      if (frame.isVisible())
      {
        int row = layoutIndex / columnCount;
        int column = layoutIndex % columnCount;
        int top = MARGIN_TOP + (row * tileHeight + row * SPACING);
        int left = MARGIN_LEFT + (column * tileWidth + column * SPACING);
        frame.alignTo(clientAreaElement, "tl-tl", new int[] {
            left,
            top
        });
        frame.setSize(tileWidth, tileHeight);
        layoutIndex++;
      }
    }
  }

  public void tileCheck()
  {
    if (isTiled && !isMaximized)
    {
      tile();
    }
  }

  private void tileLater()
  {
    Scheduler.get().scheduleDeferred(new ScheduledCommand()
    {
      @Override
      public void execute()
      {
        tile();
      }
    });
  }

  public void tileLaterCheck()
  {
    if (isTiled && !isMaximized)
    {
      tileLater();
    }
  }

  private final class ActivateListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent windowEvent)
    {
      onActivate((Frame)windowEvent.getWindow());
    }
  }

  public class CloseAllListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      onCloseAll();
    }
  }

  private final class CloseButtonListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent windowEvent)
    {
      onCloseButton((Frame)windowEvent.getWindow());
    }
  }

  private final class DeactivateListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent windowEvent)
    {
      onDeactivate((Frame)windowEvent.getWindow());
    }
  }

  private final class HeadingChangeListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent windowEvent)
    {
      onHeadingChange((Frame)windowEvent.getWindow());
    }
  }

  private final class MaximizeButtonListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent windowEvent)
    {
      onMaximizeButton((Frame)windowEvent.getWindow());
    }
  }

  private final class MinimizeButtonListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent windowEvent)
    {
      onMinimizeButton((Frame)windowEvent.getWindow());
    }
  }

  private final class RestoreButtonListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent windowEvent)
    {
      onRestoreButton((Frame)windowEvent.getWindow());
    }
  }

  private class TileWindowsToggleButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      onTileButton();
    }
  }

  public class ToggleButtonListener extends SelectionListener<ButtonEvent>
  {

    @Override
    public void componentSelected(ButtonEvent buttonEvent)
    {
      onToggleButton((ToggleButton)buttonEvent.getButton());
    }

  }
}
