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

import java.util.Date;

import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.table.ConstrainedField;

public final class DateConstrainedField extends DateField implements ConstrainedField
{
  private final long MS_PER_DAY = 24 * 60 * 60 * 1000;

  @Override
  public Object getObjectValue()
  {
    return super.getValue();
  }

  @Override
  public Object normalizeValue(Object value)
  {
    if (value != null && !(value instanceof Date))
    {
      try
      {
        value = Utility.parseDate(value.toString());
      }
      catch (IllegalArgumentException e)
      {
        value = null;
      }
    }
    return value;
  }

  @Override
  public void setMinValue(Date minValue)
  {
    long time = minValue.getTime();
    time = time - MS_PER_DAY;
    minValue.setTime(time);
    super.setMinValue(minValue); // workaround Ext-GWT problem that converts this date from midnight to noon
  }

  @Override
  public void setObjectValue(Object value)
  {
    value = normalizeValue(value);
    super.setValue((Date)value);
  }

  @Override
  public void setToolTip(String text)
  {
    super.setToolTip(text);
    getToolTip().enable();
    getToolTip().show();
    Point point = getToolTip().el().getAlignToXY(getElement(), "tl-bl?", 10, 10);
    getToolTip().showAt(point);
  }

}
