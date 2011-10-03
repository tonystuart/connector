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

package com.semanticexpression.connector.client.frame.editor.outline;

import java.util.List;

import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.wiring.VisitorTreeStore;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.enums.ContentType;

public class OutlineTreeStore extends VisitorTreeStore<ContentReference>
{
  public OutlineTreeNode getSubtree(ContentReference contentReference)
  {
    OutlineTreeNode outlineTreeNode = new OutlineTreeNode(contentReference);
    for (ContentReference child : getChildren(contentReference))
    {
      outlineTreeNode.add(getSubtree(child));
    }
    return outlineTreeNode;
  }

  public boolean hasDocumentRoot()
  {
    boolean hasDocumentRoot = false;

    int childCount = getChildCount();
    if (childCount == 1)
    {
      ContentReference root = getChild(0);
      ContentType rootContentType = root.get(Keys.CONTENT_TYPE);
      if (rootContentType == ContentType.DOCUMENT)
      {
        hasDocumentRoot = true;
      }
    }

    return hasDocumentRoot;
  }

  /**
   * Works around the lack of drop constraints in TreePanelDropTarget: if the
   * store contains a document root, and a request is made to insert additional
   * items at root, modifies the request to insert the items as children of root
   * instead of additional root items.
   * <p/>
   * Note: this overrides the insert method responsible for inserting items at
   * the root level.
   */
  @Override
  public void insert(List<ContentReference> children, int index, boolean addChildren)
  {
    if (hasDocumentRoot())
    {
      ContentReference root = getChild(0);
      insert(root, children, index == 0 ? 0 : getChildCount(root), addChildren);
    }
    else
    {
      super.insert(children, index, addChildren);
    }
  }
}
