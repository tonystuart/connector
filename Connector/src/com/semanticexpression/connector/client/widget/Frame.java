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

import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

public class Frame extends Window
{
  public static final EventType CloseButton = new EventType();
  public static final EventType HeadingChange = new EventType();

  private static final int MARGIN_HORIZONTAL = FrameManager.MARGIN_LEFT + FrameManager.MARGIN_RIGHT;
  private static final int MARGIN_VERTICAL = FrameManager.MARGIN_TOP + FrameManager.MARGIN_BOTTOM;

  public static final EventType MaximizeButton = new EventType();
  public static final EventType MinimizeButton = new EventType();
  public static final EventType RestoreButton = new EventType();

  private Component dragLimit;
  private Menu frameMenu;
  private boolean isTiled;

  public Frame()
  {
    setMinimizable(true);
    setMaximizable(true);
    setOnEsc(false);
  }

  private void applyDragLimit()
  {
    setContainer(dragLimit.getElement());
    getDraggable().setContainer(dragLimit);
  }

  @Override
  protected void fitContainer()
  {
    Rectangle bounds = fly(getContainer()).getBounds();
    alignTo(getContainer(), "tl-tl", new int[] {
        FrameManager.MARGIN_LEFT,
        FrameManager.MARGIN_TOP
    });
    setSize(bounds.width - MARGIN_HORIZONTAL, bounds.height - MARGIN_VERTICAL);
  }

  public String getHeadingTitle()
  {
    return getHeading();
  }

  public IconButton hookHeaderButton(String name)
  {
    Header header = getHeader();
    List<Component> tools = header.getTools();
    for (Component component : tools)
    {
      if (component instanceof IconButton)
      {
        IconButton iconButton = (IconButton)component;
        String styleName = iconButton.getStyleName();
        String[] styles = styleName.split(" ");
        for (String style : styles)
        {
          if (style.equals(name))
          {
            return iconButton;
          }
        }
      }
    }
    return null;
  }

  public boolean isModified()
  {
    return false;
  }

  public boolean isTiled()
  {
    return isTiled;
  }

  @Override
  public void maximize()
  {
    setActiveDecoration(false, false); // Prevent shadow on restore, may no longer be active
    super.maximize();
    addStyleName("connector-MaximizedFrame");
  }

  @Override
  public void onComponentEvent(ComponentEvent ce)
  {
    super.onComponentEvent(ce);
    if (ce.getEventTypeInt() == Event.ONCLICK)
    {
      if (frameMenu != null && head != null && ce.within(head.el().getChildElement(0)))
      {
        frameMenu.show(head);
      }
    }
  }

  @Override
  protected void onRender(Element parent, int pos)
  {
    super.onRender(parent, pos);

    Component minimizeButton = hookHeaderButton("x-tool-minimize");
    minimizeButton.setToolTip("Minimize");
    minimizeButton.addListener(Events.Select, new EventPropagator(MinimizeButton));

    Component maximizeButton = hookHeaderButton("x-tool-maximize");
    maximizeButton.setToolTip("Maximize");
    maximizeButton.addListener(Events.Select, new EventPropagator(MaximizeButton));

    Component restoreButton = hookHeaderButton("x-tool-restore");
    restoreButton.setToolTip("Restore");
    restoreButton.addListener(Events.Select, new EventPropagator(RestoreButton));

    Component closeButton = hookHeaderButton("x-tool-close");
    closeButton.setToolTip("Close");
    closeButton.addListener(Events.Select, new EventPropagator(CloseButton));
  }

  @Override
  public void restore()
  {
    super.restore();
    if (WindowManager.get().getActive() == this)
    {
      setActiveDecoration(true, true);
    }
    removeStyleName("connector-MaximizedFrame");
  }

  @Override
  public void setActive(boolean isActive)
  {
    super.setActive(isActive);
    if (!isMaximized())
    {
      setActiveDecoration(isActive, true);
    }
  }

  /**
   * Sets the window decoration that indicates the window is active (typically a
   * shadow).
   * <p/>
   * The decoration gets out of sync in a few cases:
   * <ol>
   * <li>Maximize - Activate Another Window - Restore</li>
   * <li>Tile with Draggable and Resizable styles off</li>
   * </ol>
   * This method puts them back in sync.
   * 
   * @param doSync
   */

  public void setActiveDecoration(boolean isActive, boolean doSync)
  {
    setShadow(isActive);
    if (doSync)
    {
      if (isActive)
      {
        layer.enableShadow();
      }
      else
      {
        layer.disableShadow();
      }
      layer.sync(isActive);
    }
  }

  public void setDragLimit(Component dragLimit)
  {
    this.dragLimit = dragLimit;
    applyDragLimit();
  }

  public void setFrameMenu(Menu frameMenu)
  {
    this.frameMenu = frameMenu;
    sinkEvents(Event.ONCLICK);
  }

  @Override
  public void setHeading(String heading)
  {
    super.setHeading(heading);
    fireEvent(HeadingChange, new WindowEvent(this));
  }

  public void setTiled(boolean isTiled)
  {
    this.isTiled = isTiled;
    boolean isDraggable = !isTiled;
    setDraggable(isDraggable);
    setResizable(isDraggable);
    if (isDraggable)
    {
      applyDragLimit();
    }
  }

  public static class ActivateEvent extends BaseEvent
  {
    private boolean isActive;

    public ActivateEvent(Object source, boolean isActive)
    {
      super(source);
      this.isActive = isActive;
    }

    public boolean isActive()
    {
      return isActive;
    }
  }

  public static class CloseEvent extends BaseEvent
  {
    private boolean isDeferredPendingCallback;

    public CloseEvent(FrameManager frameManager)
    {
      super(frameManager);
    }

    public FrameManager getFrameManager()
    {
      return (FrameManager)getSource();
    }

    public boolean isDeferredPendingCallback()
    {
      return this.isDeferredPendingCallback;
    }

    public void setDeferredPendingCallback(boolean isDeferredPendingCallback)
    {
      this.isDeferredPendingCallback = isDeferredPendingCallback;
    }
  }

  public final class EventPropagator implements Listener<BaseEvent>
  {
    private EventType eventType;

    public EventPropagator(EventType eventType)
    {
      this.eventType = eventType;
    }

    @Override
    public void handleEvent(BaseEvent be)
    {
      fireEvent(eventType, new WindowEvent(Frame.this));
    }
  }

}
