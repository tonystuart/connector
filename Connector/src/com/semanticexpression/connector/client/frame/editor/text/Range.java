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

import com.google.gwt.dom.client.Node;

public class Range
{
  private Node left;
  private int leftOffset;
  private Node right;
  private int rightOffset;

  public Range(Node left, Node right, int leftOffset, int rightOffset)
  {
    this.left = left;
    this.right = right;
    this.leftOffset = leftOffset;
    this.rightOffset = rightOffset;
  }

  public Node getLeft()
  {
    return left;
  }

  public int getLeftOffset()
  {
    return leftOffset;
  }

  public Node getRight()
  {
    return right;
  }

  public int getRightOffset()
  {
    return rightOffset;
  }

}
