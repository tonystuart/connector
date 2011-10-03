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


package com.semanticexpression.connector.client;

import java.util.List;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;

public class SaveAllButton extends Button
{
  private int modifyCount;

  public SaveAllButton(String text)
  {
    super(text);
    setEnabled(false);
  }

  public int getModifiedWindowCount()
  {
    int modifiedWindowCount = 0;
    List<Window> windows = WindowManager.get().getWindows();
    for (Window window : windows)
    {
      if (window instanceof EditorFrame)
      {
        EditorFrame editorFrame = (EditorFrame)window;
        if (editorFrame.isModified())
        {
          modifiedWindowCount++;
        }
      }
    }
    return modifiedWindowCount;
  }

  public void onCommit()
  {
    modifyCount = getModifiedWindowCount();
    if (modifyCount == 0)
    {
      setEnabled(false);
    }
  }

  public void onModify()
  {
    if (++modifyCount == 1)
    {
      setEnabled(true);
    }
  }

  public void onRollback()
  {
    modifyCount = getModifiedWindowCount();
    if (modifyCount == 0)
    {
      setEnabled(false);
    }
  }
}