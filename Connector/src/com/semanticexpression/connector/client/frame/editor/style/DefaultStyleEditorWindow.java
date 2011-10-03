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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.RichTextArea;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Keys;

public final class DefaultStyleEditorWindow extends Window implements OkayCancelHandler
{
  private static final String PREVIEW_HTML = //
  "<h1>Heading 1</h1>\n" + //
      "<h2>Heading 2</h2>\n" + //
      "<h3>Heading 3</h3>\n" + //
      "<h4>Heading 4</h4>\n" + //
      "<h5>Heading 5</h5>\n" + //
      "<h6>Heading 5</h6>\n" + //
      "An ordered list:\n" + //
      "<ol>\n" + //
      "<li>First List Item</li>\n" + //
      "<li>Second List Item</li>\n" + //
      "</ol>\n" + //
      "An unordered list:\n" + //
      "<ul>\n" + //
      "<li>First List Item</li>\n" + //
      "<li>Second List Item</li>\n" + //
      "</ul>\n" + //
      "Text above / before a table\n" + //
      "<div class='" + HtmlConstants.SE_MC_TABLE + "'>\n" + //
      "<table>\n" + //
      "<tr><th>Column Heading</th><th>Column Heading</th></tr>\n" + //
      "<tr class='" + HtmlConstants.SE_MC_ROW_EVEN + "'><td>Column Data</td><td>Column Data</td></tr>\n" + //
      "<tr class='" + HtmlConstants.SE_MC_ROW_ODD + "'><td>Column Data</td><td>Column Data</td></tr>\n" + //
      "<tr class='" + HtmlConstants.SE_MC_ROW_EVEN + "'><td>Column Data</td><td>Column Data</td></tr>\n" + //
      "<tr class='" + HtmlConstants.SE_MC_ROW_ODD + "'><td>Column Data</td><td>Column Data</td></tr>\n" + //
      "</table>\n" + //
      "<div class='" + HtmlConstants.SE_MC_CAPTION + "'>Optional table caption</div>\n" + //
      "</div>\n" + //
      "Text below / after a table\n<br/>\n" + //
      "Text above / before an image\n" + //
      "<div class='" + HtmlConstants.SE_MC_IMAGE + "'>\n" + //
      "<img src='http://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Tampa_Florida_Lawyers.jpg/800px-Tampa_Florida_Lawyers.jpg' width='200px' alt='View of Tampa, Florida'>\n" + //
      "<div class='" + HtmlConstants.SE_MC_CAPTION + "'>Optional image caption</div>\n" + //
      "</div>\n" + //
      "Text below / after an image\n<br/>\n" + //
      "Normal text with a <div>div</div> in the middle\n<br/>\n" + //
      "Normal text with a <span>span</span> in the middle\n<br/>\n";

  private ContentReference contentReference;
  private LayoutContainer leftLayoutContainer;
  protected OkayCancelToolBar okayCancelToolBar;
  private AdapterField previewAdapterField;
  private LayoutContainer previewLayoutContainer;
  private RichTextArea previewRichTextArea;

  private Text previewText;

  private LayoutContainer rightLayoutContainer;

  private SafeTextArea safeTextArea;

  protected SafeTextArea valueSafeTextArea;
  protected Text valueText;

  public DefaultStyleEditorWindow(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    setHeading("Default Style Editor");
    setIcon(Resources.STYLE_OMEGA);
    setLayout(new RowLayout(Orientation.HORIZONTAL));
    setClosable(false);
    add(getLeftLayoutContainer(), new RowData(0.5, 1.0, new Margins(5, 0, 5, 5)));
    add(getRightLayoutContainer(), new RowData(0.5, 1.0, new Margins(5, 5, 5, 5)));
    setBottomComponent(getOkayCancelToolBar());
    setSize("450", "350");
  }

  @Override
  public void cancel()
  {
    hide();
  }

  public void display(ContentReference contentReference, SafeTextArea safeTextArea)
  {
    this.contentReference = contentReference;
    this.safeTextArea = safeTextArea;
    String defaultStyle = contentReference.get(Keys.STYLE_DEFAULT);
    getValueSafeTextArea().setValue(defaultStyle);
    displayPreview();
  }

  private void displayPreview()
  {
    String defaultStyle = getValueSafeTextArea().getValue();
    if (defaultStyle == null)
    {
      defaultStyle = "";
    }
    getPreviewRichTextArea().setHTML(getPreviewHtml(defaultStyle));
  }

  private LayoutContainer getLeftLayoutContainer()
  {
    if (leftLayoutContainer == null)
    {
      leftLayoutContainer = new LayoutContainer();
      leftLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      leftLayoutContainer.add(getValueText(), new RowData(1.0, Style.DEFAULT, new Margins(0, 0, 0, 0)));
      leftLayoutContainer.add(getValueSafeTextArea(), new RowData(1.0, 1.0));
    }
    return leftLayoutContainer;
  }

  public OkayCancelToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(this);
    }
    return okayCancelToolBar;
  }

  private AdapterField getPreviewAdapterField()
  {
    if (previewAdapterField == null)
    {
      previewAdapterField = new AdapterField(getPreviewRichTextArea());
      previewAdapterField.setResizeWidget(true);
    }
    return previewAdapterField;
  }

  public String getPreviewHtml(String defaultStyle)
  {
    String previewHtml = "<html><head><style type='text/css'>\n" + defaultStyle + "\n</style></head><body>\n" + PREVIEW_HTML + "\n</body></html";
    return previewHtml;
  }

  private LayoutContainer getPreviewLayoutContainer()
  {
    if (previewLayoutContainer == null)
    {
      previewLayoutContainer = new LayoutContainer(new FitLayout());
      previewLayoutContainer.setBorders(true);
      previewLayoutContainer.add(getPreviewAdapterField());
    }
    return previewLayoutContainer;
  }

  public RichTextArea getPreviewRichTextArea()
  {
    if (previewRichTextArea == null)
    {
      previewRichTextArea = new RichTextArea();
    }
    return previewRichTextArea;
  }

  public Text getPreviewText()
  {
    if (previewText == null)
    {
      previewText = new Text("Style Preview:");
    }
    return previewText;
  }

  private LayoutContainer getRightLayoutContainer()
  {
    if (rightLayoutContainer == null)
    {
      rightLayoutContainer = new LayoutContainer();
      rightLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      rightLayoutContainer.add(getPreviewText(), new RowData(1.0, Style.DEFAULT));
      rightLayoutContainer.add(getPreviewLayoutContainer(), new RowData(1.0, 1.0));
    }
    return rightLayoutContainer;
  }

  public SafeTextArea getValueSafeTextArea()
  {
    if (valueSafeTextArea == null)
    {
      valueSafeTextArea = new SafeTextArea();
      valueSafeTextArea.addListener(Events.Change, new PropertyValueChangeListener());
      valueSafeTextArea.addKeyListener(new PropertyValueKeyListener());
      valueSafeTextArea.setEmptyText("Enter CSS style declaration(s) here");
    }
    return valueSafeTextArea;
  }

  public Text getValueText()
  {
    if (valueText == null)
    {
      valueText = new Text("Default Style Declaration(s):");
    }
    return valueText;
  }

  @Override
  public void okay()
  {
    String defaultStyle = getValueSafeTextArea().getValue();
    contentReference.set(Keys.STYLE_DEFAULT, defaultStyle);
    safeTextArea.setValue(defaultStyle);
    hide();
  }

  private final class PropertyValueChangeListener implements Listener<FieldEvent>
  {
    @Override
    public void handleEvent(FieldEvent be)
    {
      displayPreview();
    }
  }

  private final class PropertyValueKeyListener extends KeyListener
  {
    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      displayPreview();
    }
  }
}
