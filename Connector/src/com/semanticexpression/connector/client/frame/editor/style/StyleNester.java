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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Text;
import com.semanticexpression.connector.client.frame.editor.text.Range;
import com.semanticexpression.connector.shared.HtmlConstants;

public class StyleNester
{
  private String href;
  private String styleClassName;
  private String styleElementName;

  public StyleNester(String styleClassName, String styleElementName)
  {
    this.styleClassName = styleClassName;
    this.styleElementName = styleElementName;
  }

  public Element apply(Node left, int leftOffset, Node right, int rightOffset)
  {
    if (isEmptySelection(left, leftOffset, right, rightOffset))
    {
      return null;
    }

    if (left.getNodeType() != Node.TEXT_NODE)
    {
      // Zoom in until we find a text node (offset is a node offset, not a character offset)
      while (left.getNodeType() != Node.TEXT_NODE)
      {
        left = left.getChild(leftOffset);
        leftOffset = 0;
      }
    }
    else if (leftOffset == left.getNodeValue().length())
    {
      // Narrow selection to leftmost offset in next text node
      Node firstCommonAncestor = findFirstCommonAncestor(left, right);
      left = narrowLeft(firstCommonAncestor, (Text)left, new boolean[1]);
      leftOffset = 0;
    }

    if (right.getNodeType() != Node.TEXT_NODE)
    {
      // Zoom in until we find a text node (offset is a offset of next node, not a offset of next character)
      while (right.getNodeType() != Node.TEXT_NODE)
      {
        rightOffset--;
        right = right.getChild(rightOffset);
        rightOffset = right.getNodeType() == Node.TEXT_NODE ? right.getNodeValue().length() : right.getChildCount();
      }
    }
    else if (rightOffset == 0)
    {
      Node firstCommonAncestor = findFirstCommonAncestor(left, right);
      right = narrowRight(firstCommonAncestor, (Text)right, new Text[1]);
      rightOffset = right.getNodeValue().length();
    }

    Node firstCommonAncestor = findFirstCommonAncestor(left, right);

    if (isEmptySelection(left, leftOffset, right, rightOffset))
    {
      return null;
    }

    Element styleElement = firstCommonAncestor.getOwnerDocument().createElement(styleElementName);
    styleElement.setClassName(styleClassName);
    if (styleElementName.equals(HtmlConstants.SE_STYLE_ANCHOR))
    {
      if (isAnchorInLineage(left) || isAnchorInLineage(right))
      {
        throw new IllegalArgumentException("The operation you requested would create a nested link. Nested links are illegal in HTML.");
      }
      styleElement.setAttribute("href", href);
    }

    if (left == right)
    {
      String value = left.getNodeValue();
      int length = value.length();
      if (leftOffset == 0 && rightOffset < length)
      {
        // insert new on left (before)
        String leftValue = value.substring(leftOffset, rightOffset);
        String rightValue = value.substring(rightOffset);

        Text leftTextNode = (Text)left;
        Text rightTextNode = (Text)clonePath(firstCommonAncestor, leftTextNode);

        leftTextNode.setData(leftValue);
        rightTextNode.setData(rightValue);

        Node rightRootChild = getRootChild(firstCommonAncestor, rightTextNode);
        firstCommonAncestor.insertBefore(styleElement, rightRootChild);
        styleElement.appendChild(leftTextNode);
      }
      else if (leftOffset > 0 && rightOffset == length)
      {
        // insert new on right (after)
        String leftValue = value.substring(0, leftOffset);
        String rightValue = value.substring(leftOffset);

        Text leftTextNode = (Text)left;
        Text rightTextNode = (Text)clonePath(firstCommonAncestor, leftTextNode);

        leftTextNode.setData(leftValue);
        rightTextNode.setData(rightValue);

        Node leftRootChild = getRootChild(firstCommonAncestor, leftTextNode);
        firstCommonAncestor.insertAfter(styleElement, leftRootChild);
        styleElement.appendChild(rightTextNode);
      }
      else if (leftOffset > 0 && rightOffset < length)
      {
        // insert new in middle
        String leftValue = value.substring(0, leftOffset);
        String middleValue = value.substring(leftOffset, rightOffset);
        String rightValue = value.substring(rightOffset);

        Text leftTextNode = (Text)left;
        Text middleTextNode = (Text)clonePath(firstCommonAncestor, leftTextNode);
        Text rightTextNode = (Text)clonePath(firstCommonAncestor, middleTextNode);

        leftTextNode.setData(leftValue);
        middleTextNode.setData(middleValue);
        rightTextNode.setData(rightValue);

        Node leftRootChild = getRootChild(firstCommonAncestor, leftTextNode);
        firstCommonAncestor.insertAfter(styleElement, leftRootChild);
        styleElement.appendChild(middleTextNode);
      }
      else
      {
        // insert new above
        Node nodeRootChild = getRootChild(firstCommonAncestor, left);
        firstCommonAncestor.insertAfter(styleElement, nodeRootChild);
        styleElement.appendChild(nodeRootChild);
      }
    }
    else
    {
      String leftNodeValue = left.getNodeValue();
      String rightNodeValue = right.getNodeValue();
      int rightNodeValueLength = rightNodeValue.length();

      Node leftRootChild;
      Node rightRootChild;

      if (leftOffset == 0)
      {
        leftRootChild = getRootChild(firstCommonAncestor, left);
      }
      else
      {
        // split text and attach to parent, parent is split in next step
        String leftValue = leftNodeValue.substring(0, leftOffset);
        String rightValue = leftNodeValue.substring(leftOffset);

        Text rightTextNode = (Text)left;
        Text leftTextNode = (Text)rightTextNode.cloneNode(false);
        rightTextNode.getParentNode().insertBefore(leftTextNode, rightTextNode);

        leftTextNode.setData(leftValue);
        rightTextNode.setData(rightValue);

        leftRootChild = getRootChild(firstCommonAncestor, rightTextNode);
      }

      Node leftSibling = left.getPreviousSibling();
      if (leftSibling != null)
      {
        // split left parent node
        Node oldLeftParent = left.getParentNode();
        if (oldLeftParent != firstCommonAncestor)
        {
          Node newLeftParent = clonePath(firstCommonAncestor, oldLeftParent, Location.BEFORE);
          while (leftSibling != null)
          {
            Node previousSibling = leftSibling.getPreviousSibling();
            newLeftParent.insertFirst(leftSibling);
            leftSibling = previousSibling;
          }
        }

      }

      if (rightOffset == rightNodeValueLength)
      {
        rightRootChild = getRootChild(firstCommonAncestor, right);
      }
      else
      {
        // split text and attach to parent, parent is split in next step
        String leftValue = rightNodeValue.substring(0, rightOffset);
        String rightValue = rightNodeValue.substring(rightOffset);

        Text leftTextNode = (Text)right;
        Text rightTextNode = (Text)leftTextNode.cloneNode(false);
        leftTextNode.getParentNode().insertAfter(rightTextNode, leftTextNode);

        leftTextNode.setData(leftValue);
        rightTextNode.setData(rightValue);

        rightRootChild = getRootChild(firstCommonAncestor, leftTextNode);
      }

      Node rightSibling = right.getNextSibling();
      if (rightSibling != null)
      {
        // split right parent node
        Node oldRightParent = right.getParentNode();
        if (oldRightParent != firstCommonAncestor)
        {
          Node newRightParent = clonePath(firstCommonAncestor, oldRightParent);
          while (rightSibling != null)
          {
            Node nextSibling = rightSibling.getNextSibling();
            newRightParent.appendChild(rightSibling);
            rightSibling = nextSibling;
          }

        }
      }

      firstCommonAncestor.insertBefore(styleElement, leftRootChild);

      Node thisRootChild;
      do
      {
        thisRootChild = leftRootChild;
        leftRootChild = thisRootChild.getNextSibling(); // before re-parenting
        styleElement.appendChild(thisRootChild);
      }
      while (thisRootChild != rightRootChild);

    }

    return styleElement;
  }

  public Element apply(Range range)
  {
    return apply(range.getLeft(), range.getLeftOffset(), range.getRight(), range.getRightOffset());
  }

  private Node clonePath(Node firstCommonAncestor, Node oldNode)
  {
    return clonePath(firstCommonAncestor, oldNode, Location.AFTER);
  }

  private Node clonePath(Node firstCommonAncestor, Node oldNode, Location location)
  {
    Node firstNode = null;
    Node previousOldNode = null;
    Node previousNewNode = null;

    while (oldNode != firstCommonAncestor)
    {
      Node newNode = oldNode.cloneNode(false);
      if (firstNode == null)
      {
        firstNode = newNode;
      }
      if (previousNewNode != null)
      {
        newNode.appendChild(previousNewNode);
      }
      previousOldNode = oldNode;
      previousNewNode = newNode;
      oldNode = oldNode.getParentNode();
    }

    if (location == Location.AFTER)
    {
      firstCommonAncestor.insertAfter(previousNewNode, previousOldNode);
    }
    else
    {
      firstCommonAncestor.insertBefore(previousNewNode, previousOldNode);
    }

    return firstNode;
  }

  @SuppressWarnings("unused")
  private void dump(Node firstCommonAncestor, Node left, int leftOffset, Node right, int rightOffset)
  {
    String[] range = new String[] {
      "before"
    };

    dump(firstCommonAncestor, left, leftOffset, right, rightOffset, 0, 0, range);
  }

  private void dump(Node node, Node left, int leftOffset, Node right, int rightOffset, int level, int nodeOffset, String[] range)
  {
    short nodeType = node.getNodeType();
    String value = nodeType == Node.TEXT_NODE ? ("'" + node.getNodeValue() + "'") : node.getNodeName();

    String offsetMessage = "";

    if (node == left)
    {
      range[0] = "start";
      offsetMessage = ", offset=" + leftOffset;
    }

    if (node == right)
    {
      range[0] = "end";
      offsetMessage += ", offset=" + rightOffset;
    }

    dumpInfo(level, "level=" + level + ", node=" + nodeOffset + ", type=" + nodeType + ", range=" + range[0] + offsetMessage + ", value=" + value);

    if (node == left)
    {
      range[0] = "in";
    }

    if (node == right)
    {
      range[0] = "after";
    }

    int childCount = node.getChildCount();
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      Node child = node.getChild(childOffset);
      dump(child, left, leftOffset, right, rightOffset, level + 1, childOffset, range);
    }
  }

  private void dumpInfo(int level, String text)
  {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < level; i++)
    {
      s.append("  ");
    }
    if (text.length() > 64)
    {
      text = text.substring(0, 64) + "...";
    }
    text = text.replaceAll("[\t\n\r]+", "\\s");
    System.out.println(s + text);
  }

  private Node findFirstCommonAncestor(Node firstLeft, Node firstRight)
  {
    Node thisLeft = firstLeft;
    while ((thisLeft = thisLeft.getParentNode()) != null)
    {
      Node thisRight = firstRight;
      while ((thisRight = thisRight.getParentNode()) != null)
      {
        if (thisLeft == thisRight)
        {
          return thisLeft;
        }
      }
    }
    return null;
  }

  private Node getRootChild(Node firstCommonAncestor, Node node)
  {
    Node previousNode = null;
    while (node != firstCommonAncestor)
    {
      previousNode = node;
      node = node.getParentNode();
    }
    return previousNode;
  }

  private boolean isAnchorInLineage(Node node)
  {
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element element = (Element)node;
        String tagName = element.getTagName();
        if (tagName != null) // i.e. not document or text
        {
          if (tagName.equalsIgnoreCase(HtmlConstants.SE_STYLE_ANCHOR))
          {
            return true;
          }
        }
      }
      node = node.getParentNode();
    }
    return false;
  }

  private boolean isEmptySelection(Node left, int leftOffset, Node right, int rightOffset)
  {
    return left == right && leftOffset == rightOffset;
  }

  private Text narrowLeft(Node node, Text left, boolean[] next)
  {
    if (node == left)
    {
      next[0] = true;
      return null;
    }

    if (next[0] && node.getNodeType() == Node.TEXT_NODE)
    {
      return (Text)node;
    }

    int childCount = node.getChildCount();
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      Node child = node.getChild(childOffset);
      Text newLeft = narrowLeft(child, left, next);
      if (newLeft != null)
      {
        return newLeft;
      }
    }

    return null;
  }

  private Text narrowRight(Node node, Text right, Text[] previous)
  {
    if (node == right)
    {
      return previous[0];
    }

    if (node.getNodeType() == Node.TEXT_NODE)
    {
      previous[0] = (Text)node;
    }

    int childCount = node.getChildCount();
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      Node child = node.getChild(childOffset);
      Text newRight = narrowRight(child, right, previous);
      if (newRight != null)
      {
        return newRight;
      }
    }

    return null;
  }

  public enum Location
  {
    AFTER, BEFORE
  }

}