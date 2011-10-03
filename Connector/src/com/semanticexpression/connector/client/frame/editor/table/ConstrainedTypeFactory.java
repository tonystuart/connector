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

package com.semanticexpression.connector.client.frame.editor.table;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public class ConstrainedTypeFactory
{
  private static final String A_DELIMITER = ":";
  private static final String B_DEFAULT = ColumnType.TEXT.name() + A_DELIMITER + A_DELIMITER + A_DELIMITER;

  public static ConstrainedType createConstrainedType(Association column)
  {
    String constrainedTypeSpecification = column.get(Keys.CONSTRAINED_TYPE, B_DEFAULT);
    ConstrainedType constrainedType = new ConstrainedType(constrainedTypeSpecification);
    return constrainedType;
  }

  public static String createSpecification(ColumnType columnType, Field<?>... fields)
  {
    StringBuilder s = new StringBuilder();
    s.append(columnType.name());
    for (Field<?> field : fields)
    {
      s.append(A_DELIMITER);
      s.append(getValue(field));
    }
    return s.toString();
  }


  private static Object getValue(Field<?> field)
  {
    Object value = field.getValue();

    if (value == null)
    {
      value = "";
    }
    else if (value instanceof Date)
    {
      value = ((Date)value).getTime();
    }
    else if (value instanceof Boolean)
    {
      value = ((Boolean)value) ? 1 : 0;
    }
    return value;
  }

}
