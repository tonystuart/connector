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

package com.semanticexpression.connector.client.frame.editor;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.shared.Keys;

public abstract class CaptionBaseEditor extends LayoutContainer implements DetailsPanelComponent
{
  private LayoutContainer captionLayoutContainer;
  private SafeTextArea captionSafeTextArea;
  protected ContentReference contentReference;

  public CaptionBaseEditor()
  {
    setLayout(new BorderLayout());
    add(getCaptionLayoutContainer(), getCaptionBorderLayoutData());
  }

  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;

    String caption = contentReference.get(Keys.CAPTION);
    getCaptionSafeTextArea().setValue(caption);
  }

  protected BorderLayoutData getCaptionBorderLayoutData()
  {
    BorderLayoutData captionBorderLayoutData = new BorderLayoutData(LayoutRegion.SOUTH, 0.2f, 0, 10000);
    captionBorderLayoutData.setMargins(new Margins(5, 0, 0, 0));
    captionBorderLayoutData.setSplit(true);
    captionBorderLayoutData.setFloatable(false);
    return captionBorderLayoutData;
  }

  public LayoutContainer getCaptionLayoutContainer()
  {
    if (captionLayoutContainer == null)
    {
      captionLayoutContainer = new LayoutContainer();
      captionLayoutContainer.setLayout(new FitLayout());
      captionLayoutContainer.add(getCaptionSafeTextArea());
    }
    return captionLayoutContainer;
  }

  public SafeTextArea getCaptionSafeTextArea()
  {
    if (captionSafeTextArea == null)
    {
      captionSafeTextArea = new SafeTextArea();
      captionSafeTextArea.setEmptyText("Enter caption here");
      captionSafeTextArea.addListener(Events.Change, new PropertyValueChangeListener());
      captionSafeTextArea.addKeyListener(new PropertyValueKeyListener());
    }
    return captionSafeTextArea;
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  public void onCaptionTextChange()
  {
    String caption = getCaptionSafeTextArea().getValue();
    contentReference.set(Keys.CAPTION, caption);
  }

  @Override
  public void saveChanges()
  {
    // No action required, changes are made directly to contentReference 
  }

  private final class PropertyValueChangeListener implements Listener<FieldEvent>
  {
    @Override
    public void handleEvent(FieldEvent be)
    {
      onCaptionTextChange();
    }
  }

  private final class PropertyValueKeyListener extends KeyListener
  {
    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      onCaptionTextChange();
    }
  }
}
