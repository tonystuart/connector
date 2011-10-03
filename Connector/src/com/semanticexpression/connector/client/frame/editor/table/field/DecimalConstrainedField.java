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

import java.math.BigDecimal;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.semanticexpression.connector.client.frame.editor.table.ConstrainedField;

public final class DecimalConstrainedField extends AdapterField implements ConstrainedField
{
  public DecimalConstrainedField()
  {
    super(new FocusableSpinnerField());
    setResizeWidget(true);
  }

  @Override
  public void focus()
  {
    ((SpinnerField)widget).focus();
  }

  @Override
  public Object getObjectValue()
  {
    return getValue();
  }

  @Override
  public Object getValue()
  {
    Number value = ((SpinnerField)widget).getValue();
    return value == null ? null : value.toString();
  }

  @Override
  public Number normalizeValue(Object value)
  {
    Number number;
    if (value instanceof Number)
    {
      number = (Number)value;
    }
    else if (value != null)
    {
      try
      {
        number = Double.valueOf(value.toString());
      }
      catch (NumberFormatException e)
      {
        number = null;
      }
    }
    else
    {
      number = null;
    }
    return number;
  }

  @Override
  protected void onFocus(ComponentEvent ce)
  {
    ((FocusableSpinnerField)widget).onFocus(ce);
  }

  public void setAllowBlank(boolean isAllowBlank)
  {
    ((SpinnerField)widget).setAllowBlank(isAllowBlank);
  }

  @Override
  public void setEmptyText(String emptyText)
  {
    ((SpinnerField)widget).setEmptyText(emptyText);
  }

  public void setMaxValue(BigDecimal maximumValue)
  {
    ((SpinnerField)widget).setMaxValue(maximumValue);
  }

  public void setMinValue(BigDecimal minimumValue)
  {
    ((SpinnerField)widget).setMinValue(minimumValue);
  }

  @Override
  public void setObjectValue(Object value)
  {
    setValue(value);
  }

  @Override
  public void setToolTip(String text)
  {
    super.setToolTip(text);
    getToolTip().enable();
    getToolTip().show();
  }

  @Override
  public void setValue(Object value)
  {
    ((SpinnerField)widget).setValue(normalizeValue(value));
  }

  public final static class FocusableSpinnerField extends SpinnerField
  {

    @Override
    public void onFocus(ComponentEvent ce)
    {
      super.onFocus(ce);
    }

  }

}
