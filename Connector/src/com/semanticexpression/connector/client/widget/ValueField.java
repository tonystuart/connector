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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class ValueField extends LayoutContainer
{
  public ValueField()
  {
    setLayout(new RowLayout());
    setLayoutOnChange(true);
  }

  public Object getValue()
  {
    Object value = null;
    Component currentField = getItem(0);
    if (currentField instanceof SafeTextArea)
    {
      value = ((SafeTextArea)currentField).getValue();
    }
    else if (currentField instanceof BooleanComboBox)
    {
      value = ((BooleanComboBox)currentField).getSimpleValue();
    }
    return value;
  }

  public void setValue(Object value)
  {
    Component currentField = getItem(0);
    if (value == null || value instanceof String)
    {
      SafeTextArea safeTextArea;
      if (currentField instanceof SafeTextArea)
      {
        safeTextArea = (SafeTextArea)currentField;
      }
      else
      {
        removeAll();
        safeTextArea = new SafeTextArea();
        add(safeTextArea, new RowData(1, 1));
      }
      safeTextArea.setValue((String)value);
    }
    else if (value instanceof Boolean)
    {
      BooleanComboBox booleanComboBox;
      if (currentField instanceof BooleanComboBox)
      {
        booleanComboBox = (BooleanComboBox)currentField;
      }
      else
      {
        removeAll();
        booleanComboBox = new BooleanComboBox();
        add(booleanComboBox, new RowData(1, -1));
      }
      booleanComboBox.setSimpleValue((Boolean)value);
    }
  }
}
