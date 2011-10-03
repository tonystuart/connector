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

package com.semanticexpression.connector.client.frame.editor.text;

import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.semanticexpression.connector.client.events.TextEditorUpdateEvent;
import com.semanticexpression.connector.client.frame.editor.DetailsPanelComponent;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.frame.editor.HasCompareHistory;
import com.semanticexpression.connector.client.frame.editor.comment.CommentEditor;
import com.semanticexpression.connector.client.frame.editor.style.StyleFramelet;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Framelet;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.TextCompare;
import com.semanticexpression.connector.shared.enums.ContentType;

public final class TextEditor extends LayoutContainer implements DetailsPanelComponent, HasCompareHistory
{
  private BorderLayout borderLayout;
  private MenuItem cleanupAfterPasteMenuItem;
  private CommandProcessor commandProcessor;
  private CommentEditor commentEditor;
  private ContentReference contentReference;
  private EditorFrame editorFrame;
  private Menu frameletMenu;
  private boolean isCompareResult;
  private boolean isInitialized;
  private ModificationContext modificationContext;
  private RichTextAreaFramelet richTextAreaFramelet;
  private SemanticTextArea semanticTextArea;
  private SourceWindow sourceWindow;
  private ContentPanel styleCommentContentPanel;
  private StyleFramelet styleFramelet;
  private ToolButton styleHideButton;

  public TextEditor(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    this.editorFrame = editorFrame;
    this.modificationContext = modificationContext;
    this.commandProcessor = new CommandProcessor(this);

    setLayout(getBorderLayout());
    add(getRichTextAreaFramelet(), getRichTextAreaLayoutData());
    add(getStyleCommentContentPanel(), getStyleCommentLayoutData());
  }

  public String allocateNextElementId()
  {
    int styleCounter = contentReference.get(Keys.STYLE_COUNTER, 0);
    contentReference.set(Keys.STYLE_COUNTER, styleCounter + 1);
    String styleElementId = HtmlConstants.SE_ID_STYLE + styleCounter;
    return styleElementId;
  }

  @Override
  public void compareHistory(Date leftDate, Date rightDate)
  {
    Content leftContent = editorFrame.getContent(leftDate);
    Content rightContent = editorFrame.getContent(rightDate);

    String leftText = leftContent.get(Keys.TEXT, "");
    String rightText = rightContent.get(Keys.TEXT, "");

    int styleCounter = rightContent.get(Keys.STYLE_COUNTER, 0);
    TextCompare textCompare = new TextCompare(styleCounter);
    String result = textCompare.compare(leftText, rightText);

    displayCompareResult(result);
  }

  @Override
  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    getRichTextAreaFramelet().setDescription(contentReference.getId().formatString());

    isCompareResult = false;
    String initialValue = (String)contentReference.get(Keys.TEXT);
    setValue(initialValue);
  }

  public void displayCompareResult(String result)
  {
    isCompareResult = true;
    setValue(result);
  }

  private void fireTextUpdateEvent()
  {
    if (Directory.getContentManager().getReferenceCount(contentReference.getId()) > 1)
    {
      Directory.getEventBus().post(new TextEditorUpdateEvent(TextEditor.this));
    }
  }

  private BorderLayout getBorderLayout()
  {
    if (borderLayout == null)
    {
      borderLayout = new BorderLayout();
    }
    return borderLayout;
  }

  private MenuItem getCleanupAfterPasteMenuItem()
  {
    if (cleanupAfterPasteMenuItem == null)
    {
      cleanupAfterPasteMenuItem = new MenuItem("Cleanup after Paste");
      cleanupAfterPasteMenuItem.setIcon(Resources.CLEANUP);
      cleanupAfterPasteMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        @Override
        public void componentSelected(MenuEvent ce)
        {
          normalize();
          getCommentEditor().synchronizeCommentsAndText();
        }
      });
    }
    return cleanupAfterPasteMenuItem;
  }

  public CommentEditor getCommentEditor()
  {
    if (commentEditor == null)
    {
      commentEditor = new CommentEditor(commandProcessor, modificationContext);
    }
    return commentEditor;
  }

  private BorderLayoutData getCommentLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.SOUTH, 0.5f, 0, 10000);
    layoutData.setMargins(new Margins(5, 0, 0, 0));
    layoutData.setSplit(true);
    layoutData.setCollapsible(true);
    layoutData.setFloatable(false); // buggy
    return layoutData;
  }

  private Menu getFrameletMenu()
  {
    if (frameletMenu == null)
    {
      frameletMenu = new Menu();
      frameletMenu.add(getCleanupAfterPasteMenuItem());
    }
    return frameletMenu;
  }

  public RichTextAreaFramelet getRichTextAreaFramelet()
  {
    if (richTextAreaFramelet == null)
    {
      richTextAreaFramelet = new RichTextAreaFramelet();
    }
    return richTextAreaFramelet;
  }

  private BorderLayoutData getRichTextAreaLayoutData()
  {
    BorderLayoutData richTextAreaLayoutData = new BorderLayoutData(LayoutRegion.CENTER);
    return richTextAreaLayoutData;
  }

  public SemanticTextArea getSemanticTextArea()
  {
    if (semanticTextArea == null)
    {
      semanticTextArea = new SemanticTextArea(this);
      semanticTextArea.addKeyDownHandler(new KeyDownHandler()
      {
        @Override
        public void onKeyDown(KeyDownEvent event)
        {
          if (isReadOnly())
          {
            // Workaround problem with setEnabled()
            event.preventDefault();
            event.stopPropagation();
          }
        }
      });
      semanticTextArea.addKeyUpHandler(new KeyUpHandler()
      {
        public void onKeyUp(KeyUpEvent event)
        {
          if (!isReadOnly())
          {
            onModify();
          }
        }
      });
      semanticTextArea.addClickHandler(new ClickHandler()
      {
        @Override
        public void onClick(ClickEvent event)
        {
          commandProcessor.selectGridStylesFromText();
          if (event.isControlKeyDown() && event.isShiftKeyDown())
          {
            String rawHtml = semanticTextArea.getHTML();
            String value = rawHtml;
            //            normalize();
            //            String normalizedHtml = semanticTextArea.getHTML();
            //            String value = "Raw Html:\n" + rawHtml + "\n\nNormalized Html:\n" + normalizedHtml;
            getSourceWindow().getTextArea().setValue(value);
            getSourceWindow().show();
            getSourceWindow().toFront();
          }
        }
      });
    }
    return semanticTextArea;
  }

  public SourceWindow getSourceWindow()
  {
    if (sourceWindow == null)
    {
      sourceWindow = new SourceWindow();
    }
    return sourceWindow;
  }

  private ContentPanel getStyleCommentContentPanel()
  {
    if (styleCommentContentPanel == null)
    {
      styleCommentContentPanel = new ContentPanel(); // provides expand support
      styleCommentContentPanel.setHeaderVisible(false);
      styleCommentContentPanel.setBodyBorder(false);
      styleCommentContentPanel.setLayout(new BorderLayout());
      styleCommentContentPanel.add(getStyleFramelet(), getStyleLayoutData());
      styleCommentContentPanel.add(getCommentEditor(), getCommentLayoutData());
    }
    return styleCommentContentPanel;
  }

  private BorderLayoutData getStyleCommentLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.EAST, 0.33f, 0, 10000);
    layoutData.setMargins(new Margins(0, 0, 0, 5));
    layoutData.setSplit(true);
    layoutData.setFloatable(false); // buggy
    return layoutData;
  }

  public StyleFramelet getStyleFramelet()
  {
    if (styleFramelet == null)
    {
      styleFramelet = new StyleFramelet(commandProcessor);
      styleFramelet.getHeader().addTool(getStyleHideButton());
    }
    return styleFramelet;
  }

  public Grid<Association> getStyleGrid()
  {
    return getStyleFramelet().getStyleGrid();
  }

  public ToolButton getStyleHideButton()
  {
    if (styleHideButton == null)
    {
      styleHideButton = new ToolButton("x-tool-right");
      styleHideButton.addListener(Events.Select, new Listener<BaseEvent>()
      {
        @Override
        public void handleEvent(BaseEvent be)
        {
          hideStyle();
        }
      });
    }
    return styleHideButton;
  }

  private BorderLayoutData getStyleLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
    layoutData.setSplit(true);
    layoutData.setCollapsible(true);
    layoutData.setFloatable(false); // buggy
    return layoutData;
  }

  public ListStore<Association> getStyleListStore()
  {
    return getStyleFramelet().getStyleListStore();
  }

  public String getValueFastRawUnsanitized()
  {
    return semanticTextArea.getHTML();
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  public void hideStyle()
  {
    if (getStyleCommentContentPanel().isVisible())
    {
      getBorderLayout().collapse(LayoutRegion.EAST);
      // Nothing more to do... the StyleCommentContentPanel does the expand.
    }
  }

  public boolean isReadOnly()
  {
    return contentReference.isReadOnly() || isCompareResult;
  }

  protected void normalize()
  {
    Document document = getSemanticTextArea().getIFrameDocument();
    DomNormalizer domNormalizer = new DomNormalizer(this);
    domNormalizer.normalize(document);
    String html = semanticTextArea.getHTML(); // let browser combine adjacent text nodes
    if (html != null)
    {
      html = html.replaceAll("(\\s*" + DomNormalizer.GS + "\\s*)+", "<br />");
      html = html.replaceAll("\\s+", " ");
    }
    setValue(html); // display value and update style grid
  }

  public void onModify()
  {
    modificationContext.onModify();
    fireTextUpdateEvent();
  }

  protected void onSemanticTextEditorNotReady()
  {
    isInitialized = false;
  }

  /**
   * Note that onInitialize is invoked every time the RichTextArea is attached
   * to the browser window, not just the first time. For efficiency reasons all
   * content editors are only detached when the type of a newly selected item is
   * different from the type of the currently selected item. This makes it a bit
   * difficult to predict when an onInitialize event will occur. We work around
   * this by using a local field to keep track of when we need to initialize.
   */
  void onSemanticTextEditorReady()
  {
    if (!isInitialized)
    {
      getSemanticTextArea().createStyleElement();
      performActionsRequiringInitializedTextArea();
      isInitialized = true;
    }
  }

  public void onTextEditorUpdate(TextEditorUpdateEvent textEditorUpdateEvent)
  {
    TextEditor textEditor = textEditorUpdateEvent.getTextEditor();
    if (textEditor != this)
    {
      if (contentReference.getId().equals(textEditor.contentReference.getId()))
      {
        shuntValueFrom(textEditor);
      }
    }
  }

  private void performActionsRequiringInitializedTextArea()
  {
    List<ContentReference> styles = editorFrame.getContext(contentReference, ContentType.STYLE);

    commandProcessor.applyStyles(styles);

    getCommentEditor().display(contentReference);

    boolean isReadOnly = isReadOnly();
    getSemanticTextArea().setEnabled(!isReadOnly); // Doesn't work, but leave in for future fix, see http://code.google.com/p/google-web-toolkit/issues/detail?id=1488&q=Richtextarea
    getRichTextAreaFramelet().setReadOnly(isReadOnly);

    semanticTextArea.setFocus(true);
  }

  @Override
  public void saveChanges()
  {
    if (!isReadOnly())
    {
      normalize();

      String text = getSemanticTextArea().getHTML();
      contentReference.set(Keys.TEXT, text);

      List<String> semanticTags = getSemanticTextArea().getSemanticTags();
      if (semanticTags.size() == 0)
      {
        semanticTags = null; // avoid spurious modification
      }
      contentReference.set(Keys.SEMANTIC_TAGS, semanticTags);
    }
  }

  public void setValue(String value)
  {
    semanticTextArea.setHTML(value);

    if (isInitialized)
    {
      performActionsRequiringInitializedTextArea();
    }
  }

  public void setValueFastRawUnsanitized(String value)
  {
    semanticTextArea.setHTML(value);
  }

  public void shuntValueFrom(TextEditor textEditor)
  {
    String value = textEditor.getValueFastRawUnsanitized();
    setValueFastRawUnsanitized(value);
  }

  public class RichTextAreaFramelet extends Framelet
  {
    public RichTextAreaFramelet()
    {
      super("Text", Resources.TEXT);
      setFrameletMenu(getFrameletMenu());
      add(getSemanticTextArea());
    }
  }

  public class SourceWindow extends Window
  {
    private TextArea textArea;

    public SourceWindow()
    {
      setHeading("Source Window");
      setIcon(Resources.DOCUMENT_SOURCE);
      setLayout(new FitLayout());
      setBodyBorder(false);
      add(getTextArea());
      setSize(400, 400);
    }

    private TextArea getTextArea() // SafeTextArea check - display only, we never read the value
    {
      if (textArea == null)
      {
        textArea = new TextArea();
      }
      return textArea;
    }
  }

}
