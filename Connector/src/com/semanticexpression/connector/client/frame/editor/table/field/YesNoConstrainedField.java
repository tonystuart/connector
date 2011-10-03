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

package com.semanticexpression.connector.client.frame.editor.table.field;

import com.semanticexpression.connector.client.frame.editor.table.ConstrainedField;
import com.semanticexpression.connector.client.widget.YesNoField;

public final class YesNoConstrainedField extends YesNoField implements ConstrainedField
{

  @Override
  public Object getObjectValue()
  {
    return super.getValue();
  }

  @Override
  public Object normalizeValue(Object value)
  {
    if (value != null && !(value instanceof Boolean))
    {
      String stringValue = value.toString();
      if ("Yes".equals(stringValue) || "true".equals(stringValue) || "1".equals(stringValue))
      {
        value = Boolean.TRUE;
      }
      else if ("No".equals(stringValue) || "false".equals(stringValue) || "0".equals(stringValue))
      {
        value = Boolean.FALSE;
      }
      else
      {
        value = null;
      }
    }
    return value;
  }

  @Override
  public void setObjectValue(Object value)
  {
    value = normalizeValue(value);
    super.setValue((Boolean)value);
  }

  @Override
  public void setToolTip(String text)
  {
    super.setToolTip(text);
    getToolTip().enable();
    getToolTip().show();
  }

}
