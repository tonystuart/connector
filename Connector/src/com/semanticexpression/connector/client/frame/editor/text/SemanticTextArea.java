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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.user.client.ui.RichTextArea;
import com.semanticexpression.connector.client.frame.editor.DomIterable;

// For cross-browser style issues, see
// http://code.google.com/p/doctype/wiki/ArticleInstallStyles

public final class SemanticTextArea extends RichTextArea
{
  public static final String FF_EMPTY_TEXT = "<br>";

  private static native void addRange(JavaScriptObject selection, JavaScriptObject range) /*-{
		selection.addRange(range);
  }-*/;

  private static native JavaScriptObject createRange() /*-{
		var range = $wnd.rangy.createRange();
		return range;
  }-*/;

  private static native StyleElement createStyleElement(Document doc) /*-{
		return doc.createStyleSheet();
  }-*/;

  // Not sure where (if) this must be invoked. It causes NS_ERROR_DOM_INVALID_STATE_ERR on subsequent select if invoked after select.
  private static native Node detachRange(JavaScriptObject range) /*-{
		return range.detach();
  }-*/;

  private static native boolean execCommand(Document document, String sCommand, boolean bUserInterface, Object vValue) /*-{
		var success = document.execCommand(sCommand, bUserInterface, vValue);
		return success;
  }-*/;

  private static native JsArray<JavaScriptObject> getAllRanges(JavaScriptObject selection) /*-{
		return selection.getAllRanges();
  }-*/;

  private static native Node getEndContainer(JavaScriptObject range) /*-{
		return range.endContainer;
  }-*/;

  private static native int getEndOffset(JavaScriptObject range) /*-{
		return range.endOffset;
  }-*/;

  private static native Document getIFrameDocument(Element iFrameElement) /*-{
		var document = $wnd.rangy.dom.getIframeDocument(iFrameElement);
		return document;
  }-*/;

  private static native JavaScriptObject getSelection(Element iFrameElement) /*-{
		var selection = $wnd.rangy.getIframeSelection(iFrameElement);
		return selection;
  }-*/;

  private static native Node getStartContainer(JavaScriptObject range) /*-{
		return range.startContainer;
  }-*/;

  private static native int getStartOffset(JavaScriptObject range) /*-{
		return range.startOffset;
  }-*/;

  private static native boolean isCollapsed(JavaScriptObject range) /*-{
		return range.collapsed;
  }-*/;

  private static native void removeAllRanges(JavaScriptObject selection) /*-{
		selection.removeAllRanges();
  }-*/;

  private static native void selectNodeContents(JavaScriptObject range, Node node) /*-{
		range.selectNodeContents(node);
  }-*/;

  private boolean isFocused; // GXT.isIE only
  private JsArray<JavaScriptObject> ranges; // GXT.isIE only
  private StyleElement styleElement;
  private final TextEditor textEditor;

  public SemanticTextArea(final TextEditor textEditor)
  {
    this.textEditor = textEditor;

    if (GXT.isIE)
    {
      addFocusHandler(new FocusHandler()
      {
        @Override
        public void onFocus(FocusEvent event)
        {
          isFocused = true;
        }
      });

      addBlurHandler(new BlurHandler()
      {
        @Override
        public void onBlur(BlurEvent event)
        {
          isFocused = false;
          JavaScriptObject selection = getSelection();
          ranges = getAllRanges(selection);
        }
      });
    }

    addHandler(new InitializeHandler()
    {
      @Override
      public void onInitialize(InitializeEvent event)
      {
        textEditor.onSemanticTextEditorReady();
      }
    }, InitializeEvent.getType());
  }

  public void applyStyles(String styleRules)
  {
    if (GXT.isIE)
    {
      styleElement.setCssText(styleRules);
    }
    else if (GXT.isSafari)
    {
      styleElement.setInnerText(styleRules);
    }
    else
    {
      styleElement.setInnerHTML(styleRules);
    }
  }

  private Range createRange(JavaScriptObject jsRange)
  {
    Range range;
    Node left = SemanticTextArea.getStartContainer(jsRange);
    Node right = SemanticTextArea.getEndContainer(jsRange);

    int leftOffset = SemanticTextArea.getStartOffset(jsRange);
    int rightOffset = SemanticTextArea.getEndOffset(jsRange);

    range = new Range(left, right, leftOffset, rightOffset);
    return range;
  }

  public void createStyleElement()
  {
    com.google.gwt.user.client.Element richTextAreaElement = getElement();
    IFrameElement iFrameElement = IFrameElement.as(richTextAreaElement);
    Document document = iFrameElement.getContentDocument();

    if (GXT.isIE)
    {
      styleElement = createStyleElement(document);
    }
    else
    {
      styleElement = document.createStyleElement();
      styleElement.setType("text/css");

      HeadElement head = HeadElement.as((HeadElement)document.getElementsByTagName("head").getItem(0));
      if (head == null) // early versions of Opera
      {
        BodyElement body = document.getBody();
        head = document.createHeadElement();
        body.getParentNode().insertBefore(head, body);
      }
      head.insertFirst(styleElement);
    }
  }

  public void deselectAll()
  {
    deselectAll(getSelection());
  }

  private void deselectAll(JavaScriptObject selection)
  {
    removeAllRanges(selection);
  }

  public DomIterable getDocumentDomIterable(boolean isForward)
  {
    Document document = getIFrameDocument();
    DomIterable documentDomIterable = new DomIterable(document, isForward);
    return documentDomIterable;
  }

  public Range getFirstRange()
  {
    Range range = null;
    JsArray<JavaScriptObject> jsRanges = getSelectionRanges();

    int rangeCount = jsRanges.length();
    if (rangeCount > 0)
    {
      JavaScriptObject jsRange = jsRanges.get(0);
      range = createRange(jsRange);
    }

    return range;
  }

  @Override
  public String getHTML()
  {
    String html = super.getHTML();
    if (html != null)
    {
      if (GXT.isIE)
      {
        HtmlNormalizer htmlNormalizer = new HtmlNormalizer();
        html = htmlNormalizer.normalize(html);
      }
      if (html.equals(FF_EMPTY_TEXT) || html.length() == 0)
      {
        html = null;
      }
    }
    return html;
  }

  public Document getIFrameDocument()
  {
    Document document = getIFrameDocument(getElement());
    return document;
  }

  public List<Range> getRanges()
  {
    List<Range> ranges = new LinkedList<Range>();
    JsArray<JavaScriptObject> jsRanges = getSelectionRanges();

    int rangeCount = jsRanges.length();
    for (int rangeOffset = 0; rangeOffset < rangeCount; rangeOffset++)
    {
      JavaScriptObject jsRange = jsRanges.get(rangeOffset);
      Range range = createRange(jsRange);
      ranges.add(range);
    }

    return ranges;
  }

  private JavaScriptObject getSelection()
  {
    JavaScriptObject selection = getSelection(getElement());
    return selection;
  }

  private JsArray<JavaScriptObject> getSelectionRanges()
  {
    if (GXT.isIE && !isFocused)
    {
      return ranges; // return selection that we saved when we lost focus
    }
    else
    {
      JavaScriptObject selection = getSelection();
      return getAllRanges(selection);
    }
  }

  public List<String> getSemanticTags()
  {
    Set<String> classNames = new HashSet<String>();
    DomIterable domIterable = getDocumentDomIterable(true);

    for (Node node : domIterable)
    {
      if (node instanceof Element)
      {
        Element element = (Element)node;
        String className = element.getClassName();
        if (className != null && className.length() > 0)
        {
          classNames.add(className);
        }
      }
    }

    String[] sortedClassNames = classNames.toArray(new String[classNames.size()]);
    Arrays.sort(sortedClassNames);

    List<String> semanticTags = new LinkedList<String>();
    for (String semanticTag : sortedClassNames)
    {
      semanticTags.add(semanticTag);
    }

    return semanticTags;
  }

  @Override
  protected void onDetach()
  {
    super.onDetach();
    this.textEditor.onSemanticTextEditorNotReady();
  }

  public void select(Node node, boolean isAddToSelection)
  {
    JavaScriptObject selection = getSelection();
    if (!isAddToSelection)
    {
      deselectAll(selection);
    }
    JavaScriptObject newRange = createRange();
    selectNodeContents(newRange, node);
    addRange(selection, newRange);
  }

  @Override
  public void setHTML(String html)
  {
    if (html == null)
    {
      html = ""; // required in production mode for both Firefox and IE
    }
    super.setHTML(html);
  }

}