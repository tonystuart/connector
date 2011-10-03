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

import java.util.Iterator;

import com.google.gwt.dom.client.Node;

public final class DomIterable implements Iterable<Node>
{
  private boolean isForward;
  private Node node;

  public DomIterable(Node node, boolean isForward)
  {
    this.node = node;
    this.isForward = isForward;
  }

  @Override
  public Iterator<Node> iterator()
  {
    return new DomIterator(node);
  }

  public class DomIterator implements Iterator<Node>
  {
    private Node node;
    private Node current;

    public DomIterator(Node node)
    {
      this.node = node;
    }

    @Override
    public boolean hasNext()
    {
      return node != null;
    }

    @Override
    public Node next()
    {
      current = node;
      node = getNext(node, true);
      return current;
    }

    @Override
    public void remove()
    {
      node = getNext(current, false);
      current.getParentNode().removeChild(current);
    }

    private Node getNext(Node node, boolean isIncludeChildren)
    {
      if (isIncludeChildren)
      {
        Node child = isForward ? node.getFirstChild() : node.getLastChild();
        if (child != null)
        {
          return child;
        }
      }
      
      Node sibling = isForward ? node.getNextSibling() : node.getPreviousSibling();
      if (sibling != null)
      {
        return sibling;
      }

      Node parent;
      Node target = node;

      while ((parent = target.getParentNode()) != null)
      {
        sibling = isForward ? parent.getNextSibling() : parent.getPreviousSibling();
        if (sibling != null)
        {
          return sibling;
        }
        target = parent;
      }

      return null;
    }

  }

}