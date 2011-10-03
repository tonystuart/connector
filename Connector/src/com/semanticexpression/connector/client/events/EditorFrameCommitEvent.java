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

package com.semanticexpression.connector.client.events;

import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.wiring.EventNotification;

public class EditorFrameCommitEvent implements EventNotification
{
  private EditorFrame editorFrame;

  public EditorFrameCommitEvent(EditorFrame editorFrame)
  {
    this.editorFrame = editorFrame;
  }

  public EditorFrame getEditorFrame()
  {
    return editorFrame;
  }

}
