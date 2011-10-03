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

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.semanticexpression.connector.shared.SafeHtml;

public class SafeTextField<D> extends TextField<D>
{
  @SuppressWarnings("unchecked")
  @Override
  public D getValue()
  {
    D value = super.getValue();
    if (value instanceof String)
    {
      value = (D)SafeHtml.escape((String)value);
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValue(D value)
  {
    if (value instanceof String)
    {
      value = (D)SafeHtml.unescape((String)value);
    }
    super.setValue(value);
  }
}
