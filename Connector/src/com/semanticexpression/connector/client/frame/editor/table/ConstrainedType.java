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

import java.math.BigDecimal;
import java.util.Date;

public class ConstrainedType
{
  private String[] values;

  public ConstrainedType(String specification)
  {
    values = specification.split(":", -1);
  }

  public Boolean getBoolean(int parameterOffset)
  {
    return parseBoolean(values[parameterOffset + 1]);
  }

  public Boolean getBoolean(int parameterOffset, Boolean defaultValue)
  {
    Boolean value = parseBoolean(values[parameterOffset + 1]);
    if (value == null)
    {
      value = defaultValue;
    }
    return value;
  }

  public ColumnType getColumnType()
  {
    return ColumnType.valueOf(values[0]);
  }

  public Date getDate(int parameterOffset)
  {
    return parseDate(values[parameterOffset + 1]);
  }

  public BigDecimal getDecimal(int parameterOffset)
  {
    return parseDecimal(values[parameterOffset + 1]);
  }

  public Integer getInteger(int parameterOffset)
  {
    return parseInteger(values[parameterOffset + 1]);
  }

  private Boolean parseBoolean(String value)
  {
    Boolean booleanConstraint = null;
    Integer integerConstraint = parseInteger(value);
    if (integerConstraint != null)
    {
      booleanConstraint = integerConstraint == 0 ? Boolean.FALSE : integerConstraint == 1 ? Boolean.TRUE : null;
    }
    return booleanConstraint;
  }

  private Date parseDate(String value)
  {
    Date dateConstraint = null;
    if (value.length() > 0)
    {
      try
      {
        dateConstraint = new Date(Long.parseLong(value));
      }
      catch (Exception e)
      {
        // default to null;
      }
    }
    return dateConstraint;
  }

  private BigDecimal parseDecimal(String value)
  {
    BigDecimal decimalConstraint = null;
    if (value.length() > 0)
    {
      try
      {
        decimalConstraint = BigDecimal.valueOf(Double.valueOf(value));
      }
      catch (Exception e)
      {
        // default to null;
      }
    }
    return decimalConstraint;
  }

  private Integer parseInteger(String value)
  {
    Integer integerConstraint = null;
    if (value.length() > 0)
    {
      try
      {
        integerConstraint = Integer.valueOf(value);
      }
      catch (Exception e)
      {
        // default to null;
      }
    }
    return integerConstraint;
  }

}
