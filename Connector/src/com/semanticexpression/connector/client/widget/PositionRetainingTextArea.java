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

package com.semanticexpression.connector.client.widget;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.util.Scroll;
import com.extjs.gxt.ui.client.widget.form.TextArea;

public class PositionRetainingTextArea extends TextArea
{
  public void insertAtCursor(String text, boolean isAdvanceCursor, int rightCursorOffset)
  {
    int cursorPos;
    String value = getValue();
    if (value == null)
    {
      value = "";
      cursorPos = 0;
    }
    else
    {
      cursorPos = getCursorPos();
    }
    String newValue = value.substring(0, cursorPos) + text + value.substring(cursorPos);
    El inputEl = getInputEl();
    Scroll scroll = inputEl.getScroll();
    int scrollTop = scroll.getScrollTop();
    inputEl.setValue(newValue);
    inputEl.setScrollTop(scrollTop);
    if (isAdvanceCursor)
    {
      cursorPos += text.length() - rightCursorOffset;
    }
    setCursorPos(cursorPos);
    inputEl.setFocus(true);
  }
}
