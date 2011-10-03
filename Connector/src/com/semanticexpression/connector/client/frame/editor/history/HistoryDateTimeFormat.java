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

package com.semanticexpression.connector.client.frame.editor.history;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class HistoryDateTimeFormat extends DateTimeFormat
{
  protected HistoryDateTimeFormat()
  {
    super("yyyy-MM-dd HH:mm");
  }

  @Override
  public String format(Date date)
  {
    String value;
    if (date == HistoryFramelet.CURRENT_DATE)
    {
      value = HistoryFramelet.CURRENT;
    }
    else
    {
      value = super.format(date);
    }
    return value;
  }
}
