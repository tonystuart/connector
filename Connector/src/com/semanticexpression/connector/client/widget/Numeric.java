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

import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.google.gwt.i18n.client.NumberFormat;

public final class Numeric extends SpinnerField
{

  public Numeric(String emptyText)
  {
    setEmptyText(emptyText);
    setPropertyEditorType(Integer.class);
    setMinValue(1);
    setAllowDecimals(false);
    setIncrement(Integer.valueOf(1));
    setFormat(NumberFormat.getFormat("0"));
  }

  public void setValue(Integer value, Integer maxValue)
  {
    setValue(value);
    if (maxValue != null)
    {
      setMaxValue(maxValue);
    }
    else
    {
      setMaxValue(10000);
    }
  }

}