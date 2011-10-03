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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.DomIterable;
import com.semanticexpression.connector.client.frame.editor.comment.CommentEditor;
import com.semanticexpression.connector.client.frame.editor.style.StyleNester;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Keys;

public class CommandProcessor
{
  private final TextEditor textEditor;

  public CommandProcessor(TextEditor textEditor)
  {
    this.textEditor = textEditor;
  }

  /**
   * Apply a nested style.
   * <p/>
   * Currently supports only single selection. Multiple selection would require
   * adjusting the selections as the DOM tree changes, which is non-trivial.
   */
  public Element apply()
  {
    Element newStyleElement = null;
    Association style = getStyleGrid().getSelectionModel().getSelectedItem();
    if (style == null)
    {
      Utility.displaySelectionMessageBox();
    }
    else
    {
      Range range = getSemanticTextArea().getFirstRange();
      if (range == null)
      {
        MessageBox.alert("Nothing Selected", "Please select some text and try again.", null);
      }
      else
      {
        newStyleElement = apply(style, range);
      }
    }
    return newStyleElement;
  }

  private Element apply(Association style, Range range)
  {
    String styleName = style.get(Keys.NAME);
    String styleClassName = Utility.createStyleClassName(styleName);
    String styleElementName = style.get(Keys.STYLE_ELEMENT_NAME);
    StyleNester styleNester = new StyleNester(styleClassName, styleElementName);
    Element newStyleElement = styleNester.apply(range);
    if (newStyleElement != null)
    {
      fixIncorrectlyNestedListItem(newStyleElement, styleElementName);

      getSemanticTextArea().select(newStyleElement, false);

      String styleElementId = textEditor.allocateNextElementId();
      newStyleElement.setId(styleElementId);

      boolean isComment = style.get(Keys.STYLE_IS_COMMENT_ENABLED, false);
      if (isComment)
      {
        getCommentEditor().addItem(styleElementId);
      }

      textEditor.onModify();
    }
    return newStyleElement;
  }

  public void applyStyles(List<ContentReference> styles)
  {
    String styleRules = getStyleRules(styles);
    getSemanticTextArea().applyStyles(styleRules);
    checkStyleIssues();
  }

  private void checkStyleIssues()
  {
    Document document = getSemanticTextArea().getIFrameDocument();
    checkStyleIssues(document);
  }

  private void checkStyleIssues(Node node)
  {
    int childCount = node.getChildCount();
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      Node child = node.getChild(childOffset);
      if (child instanceof Element)
      {
        Element element = (Element)child;
        String className = element.getClassName();
        if (className != null && className.length() > 0)
        {
          String tagName = element.getTagName();
          Association style = findStyleByName(className);
          if (style == null)
          {
            style = new Association();
            style.set(Keys.NAME, className);
            style.set(Keys.STYLE_ELEMENT_NAME, tagName);
            style.set(Keys.STYLE_IS_UNDEFINED, true);
            getStyleListStore().add(style);
          }
          else
          {
            String styleElementName = style.get(Keys.STYLE_ELEMENT_NAME);
            if (!tagName.equalsIgnoreCase(styleElementName))
            {
              Document ownerDocument = element.getOwnerDocument();
              Element newElement = ownerDocument.createElement(styleElementName);
              newElement.setClassName(className);
              String elementId = element.getId();
              if (elementId != null && elementId.length() > 0)
              {
                newElement.setId(elementId);
              }
              while (element.hasChildNodes())
              {
                newElement.appendChild(element.getFirstChild());
              }
              element.getParentNode().replaceChild(newElement, element);
            }
          }
        }
        checkStyleIssues(child);
      }
    }
  }

  public void clear()
  {
    Set<Element> matchingElements = new HashSet<Element>(); // Mozilla appears to duplicate items in nested selections
    List<Range> ranges = getSemanticTextArea().getRanges();
    for (Range range : ranges)
    {
      Node node = range.getLeft();
      while (node != null)
      {
        if (node instanceof Element)
        {
          Element element = (Element)node;
          String className = element.getClassName();
          if (className != null && className.length() > 0)
          {
            Association style = findStyleByName(className);
            if (style != null)
            {
              boolean isSelected = getStyleGrid().getSelectionModel().isSelected(style);
              if (isSelected)
              {
                matchingElements.add(element);
              }
            }
          }
        }
        // must process lineage, mozilla selection may be text node instead of parent style element
        node = node.getParentNode();
      }
    }

    for (Element element : matchingElements)
    {
      removeElementAndReparentChildren(element);
    }

    getCommentEditor().synchronizeCommentsAndText();

    textEditor.onModify();
  }

  public void clearAll()
  {
    MessageBox.confirm("Clear All", "Are you sure you want to remove all styles and any associated comments from the text?", new Listener<MessageBoxEvent>()
    {
      @Override
      public void handleEvent(MessageBoxEvent be)
      {
        if (Dialog.YES.equals(be.getButtonClicked().getItemId()))
        {
          selectAll();
          clear();
        }
      }
    });
  }

  public void find(boolean isForward)
  {
    Range range = getSemanticTextArea().getFirstRange();
    if (range != null)
    {
      Node node = range.getLeft();
      DomIterable domIterable = new DomIterable(node, isForward);
      for (Node nextNode : domIterable)
      {
        if (nextNode instanceof Element)
        {
          Element nextElement = (Element)nextNode;
          String className = nextElement.getClassName();
          if (className != null)
          {
            List<Association> selectedStyles = getStyleGrid().getSelectionModel().getSelectedItems();
            for (Association style : selectedStyles)
            {
              String styleName = style.get(Keys.NAME);
              String styleClassName = Utility.createStyleClassName(styleName);
              if (className.equals(styleClassName))
              {
                getSemanticTextArea().select(nextElement, false);
                return;
              }
            }
          }
        }
      }
    }
  }

  public Element find(String styleElementId)
  {
    DomIterable domIterable = getDocumentDomIterable(true);
    for (Node node : domIterable)
    {
      if (node instanceof Element)
      {
        Element element = (Element)node;
        String id = element.getId();
        if (styleElementId.equals(id))
        {
          return element;
        }
      }
    }
    return null;
  }

  public Association findStyleByName(String className)
  {
    int styleCount = getStyleListStore().getCount();
    for (int styleOffset = 0; styleOffset < styleCount; styleOffset++)
    {
      Association style = getStyleListStore().getAt(styleOffset);
      String styleName = style.get(Keys.NAME);
      String styleClassName = Utility.createStyleClassName(styleName);
      if (styleClassName.equals(className)) // classNames are case sensitive
      {
        return style;
      }
    }
    return null;
  }

  /**
   * Fixes a poorly nested list. If the user applies a list item style to a
   * single text item and then applies a list style to the same text item, the
   * first common ancestor will be the list item and the list will end up
   * incorrectly nested inside the list item. This method checks for this
   * situation and if found gives the user an opportunity to let us swap the
   * list and list item.
   */
  void fixIncorrectlyNestedListItem(final Element newStyleElement, String styleElementName)
  {
    if (HtmlConstants.SE_STYLE_ORDERED_LIST.equals(styleElementName) || HtmlConstants.SE_STYLE_UNORDERED_LIST.equals(styleElementName))
    {
      final Element parentElement = newStyleElement.getParentElement();
      if (parentElement.getChildCount() == 1)
      {
        if (HtmlConstants.SE_STYLE_LIST_ITEM.equalsIgnoreCase(parentElement.getTagName()))
        {
          MessageBox.confirm("List Structure", "Would you like to make the list the parent of the list item?", new Listener<MessageBoxEvent>()
          {
            @Override
            public void handleEvent(MessageBoxEvent messageBoxEvent)
            {
              if (Dialog.YES.equals(messageBoxEvent.getButtonClicked().getItemId()))
              {
                Element childElement = (Element)newStyleElement.getChild(0);
                Element listElement = newStyleElement;
                Element listItemElement = parentElement;
                Element grandParentElement = parentElement.getParentElement();
                grandParentElement.replaceChild(listElement, listItemElement);
                listElement.appendChild(listItemElement);
                listItemElement.appendChild(childElement);
              }
            }
          });
        }
      }
    }
  }

  private CommentEditor getCommentEditor()
  {
    return textEditor.getCommentEditor();
  }

  public DomIterable getDocumentDomIterable(boolean isForward)
  {
    return getSemanticTextArea().getDocumentDomIterable(isForward);
  }

  private SemanticTextArea getSemanticTextArea()
  {
    return textEditor.getSemanticTextArea();
  }

  private Grid<Association> getStyleGrid()
  {
    return textEditor.getStyleGrid();
  }

  private ListStore<Association> getStyleListStore()
  {
    return textEditor.getStyleListStore();
  }

  public String getStyleRules(List<ContentReference> styleCards)
  {
    StringBuilder s = new StringBuilder();
    LinkedHashMap<String, Association> styles = new LinkedHashMap<String, Association>();
    for (ContentReference styleCard : styleCards)
    {
      List<Association> styleProperties = styleCard.get(Keys.STYLES);
      if (styleProperties != null)
      {
        for (Association styleProperty : styleProperties)
        {
          // Many style names can map to one style class name and one style class name can map to many style selectors. Ultimately the selector determines what style will be used.
          String styleName = styleProperty.get(Keys.NAME);
          String styleClassName = Utility.createStyleClassName(styleName);

          // If there are multiple definitions for this style, let StyleFramelet grid display last one. This does not combine styles. For that we would have to build up a complete CSS rule.
          styles.put(styleClassName, styleProperty);

          String styleDeclarations = styleProperty.get(Keys.VALUE);
          if (styleDeclarations != null)
          {
            String styleSelector = styleProperty.get(Keys.STYLE_SELECTOR);
            String styleRule = styleSelector + " { " + styleDeclarations + " }";
            if (s.length() > 0)
            {
              s.append("\n");
            }
            s.append(styleRule);
          }
        }
      }
    }

    getStyleListStore().removeAll();
    for (Association style : styles.values())
    {
      getStyleListStore().add(style);
    }

    return s.toString();
  }

  private void promptForHyperlinkUrl(Element element)
  {
    MessageBox prompt = MessageBox.prompt("Update Hyperlink", "Please enter the URL for this hyperlink. To follow the hyperlink, press the control key while clicking on it.", false, new HyperlinkPromptCallback(element));
    String hyperlinkUrl = element.getAttribute(HtmlConstants.SE_STYLE_ANCHOR_HREF);
    prompt.getTextBox().setValue(hyperlinkUrl);
  }

  public void removeElementAndReparentChildren(Element element)
  {
    Node parent = element.getParentNode();
    while (element.hasChildNodes())
    {
      parent.insertBefore(element.getFirstChild(), element);
    }
    parent.removeChild(element);
  }

  public void select(String styleElementId)
  {
    Element element = find(styleElementId);
    if (element == null)
    {
      MessageBox.alert("No Comment Style", "The marker for the comment has been removed.", null);
    }
    else
    {
      getSemanticTextArea().select(element, false);
      selectGridStylesFromText();
    }
  }

  public void selectAll()
  {
    getStyleGrid().getSelectionModel().selectAll();
    selectTextStylesFromGrid();
  }

  public void selectGridStylesFromText()
  {
    getStyleGrid().getSelectionModel().deselectAll();
    getCommentEditor().deselectAll();

    List<Range> ranges = getSemanticTextArea().getRanges();
    for (Range range : ranges)
    {
      Node node = range.getLeft();
      while (node != null)
      {
        if (node instanceof Element)
        {
          Element element = (Element)node;
          String className = element.getClassName();
          if (className != null && className.length() > 0)
          {
            Association style = findStyleByName(className);
            if (style != null)
            {
              getStyleGrid().getSelectionModel().select(style, true);
            }
          }
          String styleElementId = element.getId();
          if (styleElementId != null && styleElementId.length() > 0)
          {
            getCommentEditor().selectCommentByStyleElementId(styleElementId);
          }
        }
        node = node.getParentNode();
      }
    }
  }

  public void selectTextStylesFromGrid()
  {
    getCommentEditor().deselectAll();
    getSemanticTextArea().deselectAll();

    List<Association> selectedStyles = getStyleGrid().getSelectionModel().getSelectedItems();
    for (Association style : selectedStyles)
    {
      String styleName = style.get(Keys.NAME);
      String styleClassName = Utility.createStyleClassName(styleName);
      Document document = getSemanticTextArea().getIFrameDocument();
      int selectCount = selectTextStylesFromGrid(document, styleClassName);
      if (selectCount == 0)
      {
        getStyleGrid().getSelectionModel().deselect(style);
      }
    }
  }

  private int selectTextStylesFromGrid(Node node, String styleClassName)
  {
    int selectCount = 0;
    int childCount = node.getChildCount();
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      Node child = node.getChild(childOffset);
      if (child instanceof Element)
      {
        Element element = (Element)child;
        String className = element.getClassName();
        if (className != null && className.equals(styleClassName))
        {
          getSemanticTextArea().select(element, true);
          selectCount++;
          String styleElementId = element.getId();
          if (styleElementId != null && styleElementId.length() > 0)
          {
            getCommentEditor().selectCommentByStyleElementId(styleElementId);
          }
        }
        // Styles can nest, and so should selections
        selectCount += selectTextStylesFromGrid(child, styleClassName);
      }
    }
    return selectCount;
  }

  public void updateHyperlink()
  {
    int updateCount = 0;
    Range range = getSemanticTextArea().getFirstRange();
    if (range != null)
    {
      Node node = range.getLeft();
      while (node != null && updateCount == 0) // Find first hyperlink in lineage from caret, nested hyperlinks are not allowed.
      {
        if (node instanceof Element)
        {
          Element element = (Element)node;
          String tagName = element.getTagName();
          if (tagName != null) // i.e. not document or text
          {
            if (tagName.equalsIgnoreCase(HtmlConstants.SE_STYLE_ANCHOR))
            {
              promptForHyperlinkUrl(element);
              updateCount++;
            }
          }
        }
        node = node.getParentNode();
      }
    }

    if (updateCount == 0)
    {
      MessageBox.alert("Hyperlink Update", "Please select text with a hyperlink style and try again.", null);
    }
  }

  private final class HyperlinkPromptCallback implements Listener<MessageBoxEvent>
  {
    private Element element;

    private HyperlinkPromptCallback(Element element)
    {
      this.element = element;
    }

    @Override
    public void handleEvent(MessageBoxEvent be)
    {
      if (Dialog.OK.equals(be.getButtonClicked().getItemId()))
      {
        element.setAttribute(HtmlConstants.SE_STYLE_ANCHOR_HREF, be.getValue());
      }
    }
  }

}