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

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class Framelet extends ContentPanel
{
  private Text descriptionText;
  private Menu frameletMenu;
  private String title;

  public Framelet(String title, AbstractImagePrototype icon)
  {
    this.title = title;

    setHeading(title);
    setIcon(icon);
    setBorders(false);
    setBodyBorder(true);
    setLayout(new FitLayout());
    sinkEvents(Event.ONCLICK);
  }

  private Text getDescriptionText()
  {
    if (descriptionText == null)
    {
      descriptionText = new Text(null);
      descriptionText.addStyleName("connector-FrameletDescription");
      getHeader().insertTool(descriptionText, 0);
    }
    return descriptionText;
  }

  @Override
  public void onComponentEvent(ComponentEvent ce)
  {
    super.onComponentEvent(ce);
    if (ce.getEventTypeInt() == Event.ONCLICK)
    {
      if (head != null && ce.within(head.el().getChildElement(0)))
      {
        if (frameletMenu != null)
        {
          frameletMenu.show(head);
        }
      }
    }
  }

  @Override
  protected void onRender(Element parent, int pos)
  {
    super.onRender(parent, pos);
    El.fly(head.el().getChildElement(0)).setStyleAttribute("cursor", "pointer");
  }

  public void removeDescriptionText()
  {
    getHeader().removeTool(getDescriptionText()); // subsequent calls to setDescription will have no effect
  }

  public void setDescription(String description)
  {
    getDescriptionText().setText(description);
  }

  public void setFrameletMenu(Menu frameletMenu)
  {
    this.frameletMenu = frameletMenu;
  }

  public void setReadOnly(boolean isReadOnly)
  {
    String newTitle;
    if (isReadOnly)
    {
      newTitle = title + " -- Read Only";
    }
    else
    {
      newTitle = title;
    }
    if (!getHeading().equals(newTitle))
    {
      setHeading(newTitle);
    }
  }
}
