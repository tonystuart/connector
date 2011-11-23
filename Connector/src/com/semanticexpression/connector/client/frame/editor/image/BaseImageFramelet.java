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

import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.semanticexpression.connector.client.frame.editor.BaseEditor;
import com.semanticexpression.connector.client.frame.editor.ContentReference;

public abstract class BaseImageFramelet extends BaseEditor
{
  public static final boolean SCALE_TO_FIT = true;

  protected ContentReference contentReference;
  private Menu contextImageMenu;
  private String emptyText = "";
  private Menu frameletImageMenu;
  private Html imageHtml;
  protected boolean isScaleToFit = SCALE_TO_FIT;
  private BaseImagePropertyEditorWindow propertyEditorWindow;

  public BaseImageFramelet(String title, AbstractImagePrototype icon)
  {
    super(title, icon);
    setFrameletMenu(getFrameletImageMenu());
    setMonitorWindowResize(true);
    add(getImageHtml());
  }

  protected abstract Menu createImageMenu();

  protected abstract BaseImagePropertyEditorWindow createPropertyEditorWindow();

  public void display()
  {
    String html = "";
    if (contentReference != null) // e.g. viewing history item before the image was uploaded
    {
      String maximizedDimension = "";
      if (isScaleToFit)
      {
        maximizedDimension = getScaledDimension();
      }
      String imageUrlString = getImageUrlString();
      html = "<img src='" + imageUrlString + "' " + maximizedDimension + " alt='" + emptyText + "' style='vertical-align: top;' />"; // vertical-align to prevent scroll bars in standards mode
    }
    getImageHtml().setHtml(html);
  }

  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    setDescription(contentReference.getId().formatString());
    setReadOnly(contentReference.isReadOnly());
    display();
  }

  public void editProperties()
  {
    getPropertyEditorWindow().show();
    getPropertyEditorWindow().alignTo(getElement(), "c-c?", null);
    getPropertyEditorWindow().toFront();
    getPropertyEditorWindow().edit(contentReference);
  }

  private Menu getContextImageMenu()
  {
    if (contextImageMenu == null)
    {
      contextImageMenu = createImageMenu();
    }
    return contextImageMenu;
  }

  protected Menu getFrameletImageMenu()
  {
    if (frameletImageMenu == null)
    {
      frameletImageMenu = createImageMenu();
    }
    return frameletImageMenu;
  }

  protected abstract Integer getImageHeight();

  protected Html getImageHtml()
  {
    if (imageHtml == null)
    {
      imageHtml = new Html();
      imageHtml.setStyleAttribute("background-color", "white");
      imageHtml.setStyleAttribute("overflow", "auto");
      imageHtml.setContextMenu(getContextImageMenu());
    }
    return imageHtml;
  }

  protected abstract String getImageUrlString();

  protected abstract Integer getImageWidth();

  private BaseImagePropertyEditorWindow getPropertyEditorWindow()
  {
    if (propertyEditorWindow == null)
    {
      propertyEditorWindow = createPropertyEditorWindow();
    }
    return propertyEditorWindow;
  }

  protected String getScaledDimension()
  {
    Integer imageWidth = getImageWidth();
    Integer imageHeight = getImageHeight();

    int windowWidth = getImageHtml().getWidth();
    int windowHeight = getImageHtml().isRendered() ? getImageHtml().getHeight() : 0; // workaround Ext-GWT problem when image is first item in outline tree, we monitor resize and get called again after render

    String maximizedDimension = "";

    if (imageHeight != null && imageHeight != 0 && windowHeight != 0)
    {
      float imageAspectRatio = (float)imageWidth / imageHeight;
      float windowAspectRatio = (float)windowWidth / windowHeight;
      if (imageAspectRatio > windowAspectRatio)
      {
        maximizedDimension = "width='100%'";
      }
      else if (imageAspectRatio < windowAspectRatio)
      {
        maximizedDimension = "height='100%'";
      }
    }

    return maximizedDimension;
  }

  @Override
  protected void onMinimizeOrClose()
  {
    if (propertyEditorWindow != null)
    {
      propertyEditorWindow.hide();
    }
  }

  @Override
  protected void onResize(int width, int height)
  {
    super.onResize(width, height);
    Scheduler.get().scheduleDeferred(new ScheduledCommand()
    {
      @Override
      public void execute()
      {
        display();
      }
    });
  }

  protected void setEmptyText(String emptyText)
  {
    this.emptyText = emptyText;
  }

  public void setScaleToFit(boolean isScaleToFit)
  {
    this.isScaleToFit = isScaleToFit;
    display();
  }

}