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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Text;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.ContentManager;
import com.semanticexpression.connector.shared.HtmlConstants;

public final class DomNormalizer
{
  public static final String GS = new String(new char[] {
    0x1d
  });

  public static final Rule PR_DESCEND = new Rule(Action.DESCEND, null, null);
  public static final Rule PR_DISCARD = new Rule(Action.DISCARD, null, null);
  public static final Rule PR_FLATTEN = new Rule(Action.FLATTEN, null, null);

  public static final Rule PR_SKIP = new Rule(Action.SKIP, null, null);
  public static final Map<String, Rule> RULES = getDefaultRules();

  // See http://stackoverflow.com/questions/5126429/in-gwt-how-can-i-get-all-attributes-of-an-element-in-the-html-dom
  public static native JsArray<Node> getAttributes(Element elem) /*-{
		return elem.attributes;
  }-*/;

  private static Map<String, Rule> getDefaultRules()
  {
    Map<String, Rule> defaultStyles = new HashMap<String, Rule>();

    defaultStyles.put(HtmlConstants.SE_STRUCTURE_BODY, PR_DESCEND);
    defaultStyles.put(HtmlConstants.SE_STRUCTURE_HTML, PR_DESCEND);

    defaultStyles.put(HtmlConstants.SE_STRUCTURE_HEAD, PR_SKIP);
    defaultStyles.put(HtmlConstants.SE_STRUCTURE_STYLE, PR_DISCARD); // we expect our style to be under head which we skip any other is discarded
    defaultStyles.put(HtmlConstants.SE_STRUCTURE_TITLE, PR_DISCARD); // we expect our title to be under head which we skip any other is discarded

    defaultStyles.put(HtmlConstants.SE_STYLE_DIVISION, new Rule(Action.FLATTEN_WILD, HtmlConstants.SE_STYLE_DIVISION, ContentManager.DS_PARAGRAPH));
    defaultStyles.put(HtmlConstants.SE_STYLE_SPAN, new Rule(Action.FLATTEN_WILD, HtmlConstants.SE_STYLE_SPAN, ContentManager.DS_DETAIL));

    defaultStyles.put(HtmlConstants.SE_STYLE_ANCHOR, new Rule(Action.PROCESS, HtmlConstants.SE_STYLE_ANCHOR, ContentManager.DS_HYPERTEXT_LINK));
    defaultStyles.put(HtmlConstants.SE_STYLE_LIST_ITEM, new Rule(Action.PROCESS, HtmlConstants.SE_STYLE_LIST_ITEM, ContentManager.DS_LIST_ITEM));
    defaultStyles.put(HtmlConstants.SE_STYLE_ORDERED_LIST, new Rule(Action.PROCESS, HtmlConstants.SE_STYLE_ORDERED_LIST, ContentManager.DS_ORDERED_LIST));
    defaultStyles.put(HtmlConstants.SE_STYLE_UNORDERED_LIST, new Rule(Action.PROCESS, HtmlConstants.SE_STYLE_UNORDERED_LIST, ContentManager.DS_UNORDERED_LIST));

    defaultStyles.put("br", PR_SKIP);
    defaultStyles.put("b", new Rule(Action.PROCESS, HtmlConstants.SE_STYLE_SPAN, ContentManager.DS_IMPORTANT));
    defaultStyles.put("i", new Rule(Action.PROCESS, HtmlConstants.SE_STYLE_SPAN, ContentManager.DS_EMPHASIS));
    defaultStyles.put("p", new Rule(Action.CONVERT, HtmlConstants.SE_STYLE_DIVISION, ContentManager.DS_PARAGRAPH));

    return defaultStyles;
  }

  private TextEditor textEditor;

  public DomNormalizer(TextEditor textEditor)
  {
    this.textEditor = textEditor;
  }

  private void convert(Node node, Rule rule)
  {
    Element newElement = null;
    if (node instanceof Element)
    {
      Element element = (Element)node;
      String innerText = element.getInnerText();
      if (innerText != null)
      {
        innerText = innerText.trim();
        String elementName = rule.getElementName();
        String className = rule.getClassName();
        String elementId = textEditor.allocateNextElementId();
        newElement = node.getOwnerDocument().createElement(elementName);
        newElement.setClassName(className);
        newElement.setId(elementId);
        Text textNode = newElement.getOwnerDocument().createTextNode(innerText);
        newElement.appendChild(textNode);
      }
    }
    Element parent = node.getParentElement();
    if (newElement == null)
    {
      parent.removeChild(node);
    }
    else
    {
      parent.replaceChild(newElement, node);
    }
  }

  private void discard(Node node)
  {
    node.getParentElement().removeChild(node);
  }

  private void flatten(Node node)
  {
    Element parent = node.getParentElement();
    while (node.hasChildNodes())
    {
      parent.insertBefore(node.getChild(0), node);
    }
    parent.replaceChild(node.getOwnerDocument().createTextNode(GS), node);
  }

  private Rule getElementRule(Element element)
  {
    String tagName = element.getTagName().toLowerCase();
    Rule rule = RULES.get(tagName);
    if (rule == null)
    {
      rule = PR_FLATTEN;
    }
    else if (rule.getAction() == Action.FLATTEN_WILD)
    {
      if (!isValidId(element))
      {
        rule = PR_FLATTEN;
      }
    }
    return rule;
  }

  private Rule getRule(Node node)
  {
    Rule rule;
    switch (node.getNodeType())
    {
      case Node.DOCUMENT_NODE:
        rule = PR_DESCEND;
        break;
      case Node.ELEMENT_NODE:
        rule = getElementRule((Element)node);
        break;
      case Node.TEXT_NODE:
        rule = PR_SKIP;
        break;
      default:
        rule = PR_SKIP;
        break;
    }
    return rule;
  }

  public boolean isValidId(Element element)
  {
    String idName = element.getId();
    return idName != null && idName.startsWith(HtmlConstants.SE_ID_STYLE);
  }

  public void normalize(Node node)
  {
    int childOffset = 0;
    while (childOffset < node.getChildCount())
    {
      Node child = node.getChild(childOffset);
      Rule rule = getRule(child);
      switch (rule.getAction())
      {
        case CONVERT:
          convert(child, rule);
          break;
        case DESCEND:
          normalize(child);
          break;
        case DISCARD:
          discard(child);
          childOffset--;
          break;
        case FLATTEN:
          flatten(child);
          childOffset--;
          break;
        case PROCESS:
          process((Element)child, rule);
          break;
        case SKIP:
          break;
      }
      childOffset++;
    }
  }

  private void process(Element element, Rule rule)
  {
    String tagName = element.getTagName();
    String elementName = rule.getElementName();
    if (!tagName.equalsIgnoreCase(elementName))
    {
      element = replaceElement(element, elementName);
    }

    boolean validId = isValidId(element);
    if (!validId)
    {
      String elementId = textEditor.allocateNextElementId();
      element.setId(elementId);
    }

    String className = element.getClassName();
    if (!validClassName(className) || !validId)
    {
      element.setClassName(rule.getClassName());
    }

    JsArray<Node> attributes = getAttributes(element);
    if (attributes != null)
    {
      int attributeCount = attributes.length();
      for (int attributeOffset = 0; attributeOffset < attributeCount; attributeOffset++)
      {
        Node attribute = attributes.get(attributeOffset);
        if (attribute != null) // attribute may be null in production mode
        {
          String attributeName = attribute.getNodeName();
          if (attributeName != null && !attributeName.equals("class") && !attributeName.equals("id") && !attributeName.equals("href"))
          {
            element.removeAttribute(attributeName);
          }
        }
      }
    }
    normalize(element);
  }

  private Element replaceElement(Element oldElement, String elementName)
  {
    Element newElement = oldElement.getOwnerDocument().createElement(elementName);
    while (oldElement.getChildCount() > 0)
    {
      Node child = oldElement.getChild(0);
      newElement.appendChild(child);
    }
    oldElement.getParentElement().replaceChild(newElement, oldElement);
    return newElement;
  }

  private boolean validClassName(String className)
  {
    return className != null && className.length() > 0;
  }

  private enum Action
  {
    CONVERT, DESCEND, DISCARD, FLATTEN, FLATTEN_WILD, PROCESS, SKIP
  }

  private final static class Rule
  {
    private Action action;
    private String className;
    private String elementName;

    public Rule(Action action, String elementName, String styleName)
    {
      this.action = action;
      this.elementName = elementName;
      this.className = styleName == null ? null : Utility.createStyleClassName(styleName);
    }

    public Action getAction()
    {
      return action;
    }

    public String getClassName()
    {
      return className;
    }

    public String getElementName()
    {
      return elementName;
    }

  }

}
