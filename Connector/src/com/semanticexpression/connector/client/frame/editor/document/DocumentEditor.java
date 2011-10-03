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

package com.semanticexpression.connector.client.frame.editor.document;

import java.util.Date;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.semanticexpression.connector.client.ClientUrlBuilder;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.DetailsPanelComponent;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.HasCompareHistory;
import com.semanticexpression.connector.client.frame.editor.HasOnCommit;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Framelet;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.UrlBuilder;
import com.semanticexpression.connector.shared.UrlConstants;

public final class DocumentEditor extends Framelet implements DetailsPanelComponent, HasOnCommit, HasCompareHistory
{
  private ContentReference contentReference;
  private ContentViewer contentViewer;
  private DocumentMenu documentContextMenu;
  private Menu documentFrameletMenu;
  private EditorFrame editorFrame;

  public DocumentEditor(EditorFrame editorFrame)
  {
    super("Document Viewer", Resources.DOCUMENT);
    this.editorFrame = editorFrame;
    setFrameletMenu(getDocumentFrameletMenu());
    add(getContentViewer());
  }

  @Override
  public void compareHistory(Date leftDate, Date rightDate)
  {
    Id contentId = contentReference.getId();
    String url = formatUrl(contentId, leftDate, rightDate);
    getContentViewer().setUrl(url);
  }

  @Override
  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    setDescription(contentReference.getId().formatString());

    if (editorFrame.isModified())
    {
      getContentViewer().setEmptyText("Your document contains unsaved changes. To view formatted content please save your changes or select an item from your history.");
      return;
    }

    Id contentId = contentReference.getId();
    Date historyDate = contentReference.getHistoryDate();

    String url = formatUrl(contentId, historyDate, null);
    getContentViewer().setUrl(url);
  }

  protected String formatUrl(Id contentId, Date historyDate, Date compareDate)
  {
    UrlBuilder urlBuilder = new ClientUrlBuilder(UrlConstants.URL_CONTENT);
    urlBuilder.addParameter(UrlConstants.PARAMETER_ID, contentId.formatString());

    if (historyDate != null)
    {
      urlBuilder.addParameter(UrlConstants.PARAMETER_PRESENT_AT, historyDate);
    }

    if (compareDate != null)
    {
      urlBuilder.addParameter(UrlConstants.PARAMETER_COMPARE_AT, compareDate);
    }

    return urlBuilder.toString();
  }

  private ContentViewer getContentViewer()
  {
    if (contentViewer == null)
    {
      contentViewer = new ContentViewer();
      contentViewer.setContextMenu(getDocumentContextMenu()); // doesn't work
    }
    return contentViewer;
  }

  private Menu getDocumentContextMenu()
  {
    if (documentContextMenu == null)
    {
      documentContextMenu = new DocumentMenu();
    }
    return documentContextMenu;
  }

  private Menu getDocumentFrameletMenu()
  {
    if (documentFrameletMenu == null)
    {
      documentFrameletMenu = new DocumentMenu();
    }
    return documentFrameletMenu;
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  @Override
  public void onCommit()
  {
    display(contentReference);
  }

  @Override
  public void saveChanges()
  {
  }

  protected void viewFormatted()
  {
    display(contentReference);
  }

  protected void viewSource()
  {
    Frame myFrame = getContentViewer().frame;
    Element element = myFrame.getElement();
    IFrameElement iFrameElement = IFrameElement.as(element);
    Document contentDocument = iFrameElement.getContentDocument();
    String innerHtml = DOM.getInnerHTML((Element)contentDocument.getDocumentElement());
    getContentViewer().setText(innerHtml);
  }

  public class ContentViewer extends LayoutContainer
  {
    private Frame frame;

    public ContentViewer()
    {
      setLayout(new FitLayout());
    }

    public String getUrl()
    {
      return frame.getUrl();
    }

    public void setEmptyText(String text)
    {
      removeAll();
      Html html = new Html("<div class='x-grid-empty'>" + text + "</div>");
      add(html);
      layout();
    }

    public void setText(String value)
    {
      removeAll();
      TextArea textArea = new TextArea(); // SafeTextArea check - display only, we never read the value
      textArea.setValue(value);
      add(textArea);
      layout();
    }

    public void setUrl(String url)
    {
      removeAll();
      frame = new Frame(url);
      frame.getElement().setPropertyInt("frameBorder", 0);
      frame.getElement().getStyle().setBackgroundColor("white");
      frame.setSize("100%", "100%");
      add(frame);
      layout();
    }
  }

  private class DocumentMenu extends Menu
  {
    private MenuItem documentFormattedMenuItem;
    private MenuItem documentSourceMenuItem;
    private MenuItem openMenuItem;

    public DocumentMenu()
    {
      add(getDocumentSourceMenuItem());
      add(getDocumentFormattedMenuItem());
      add(getOpenMenuItem());
    }

    private MenuItem getDocumentFormattedMenuItem()
    {
      if (documentFormattedMenuItem == null)
      {
        documentFormattedMenuItem = new MenuItem("Formatted");
        documentFormattedMenuItem.setIcon(Resources.DOCUMENT_FORMATTED);
        documentFormattedMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            viewFormatted();
          }
        });
      }
      return documentFormattedMenuItem;
    }

    private MenuItem getDocumentSourceMenuItem()
    {
      if (documentSourceMenuItem == null)
      {
        documentSourceMenuItem = new MenuItem("Source");
        documentSourceMenuItem.setIcon(Resources.DOCUMENT_SOURCE);
        documentSourceMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            viewSource();
          }
        });
      }
      return documentSourceMenuItem;
    }

    public MenuItem getOpenMenuItem()
    {
      if (openMenuItem == null)
      {
        openMenuItem = new MenuItem("Open in Browser");
        openMenuItem.setIcon(Resources.OPEN_BROWSER_DOCUMENT);
        openMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            // See https://developer.mozilla.org/en/DOM/window.open
            Window.open(getContentViewer().getUrl(), null, null);
          }
        });
      }
      return openMenuItem;
    }

  }
}
