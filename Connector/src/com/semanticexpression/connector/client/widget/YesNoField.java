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

import com.extjs.gxt.ui.client.widget.form.AdapterField;

public class YesNoField extends AdapterField
{
  public YesNoField()
  {
    super(new YesNoComboBox());
    setResizeWidget(true);
  }

  @Override
  public Boolean getValue()
  {
    return ((YesNoComboBox)widget).getBooleanValue();
  }

  public void setAllowBlank(boolean isAllowBlank)
  {
    ((YesNoComboBox)widget).setForceSelection(!isAllowBlank);
  }

  @Override
  public void setEmptyText(String emptyText)
  {
    ((YesNoComboBox)widget).setEmptyText(emptyText);
  }

  public void setValue(Boolean value)
  {
    ((YesNoComboBox)widget).setBooleanValue(value);
  }

}