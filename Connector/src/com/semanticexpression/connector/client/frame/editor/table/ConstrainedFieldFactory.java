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

import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.semanticexpression.connector.client.frame.editor.table.field.DateConstrainedField;
import com.semanticexpression.connector.client.frame.editor.table.field.DecimalConstrainedField;
import com.semanticexpression.connector.client.frame.editor.table.field.IntegerConstrainedField;
import com.semanticexpression.connector.client.frame.editor.table.field.TextAreaConstrainedField;
import com.semanticexpression.connector.client.frame.editor.table.field.TextFieldConstrainedField;
import com.semanticexpression.connector.client.frame.editor.table.field.YesNoConstrainedField;
import com.semanticexpression.connector.client.widget.IntegerSpinnerField;
import com.semanticexpression.connector.client.widget.YesNoField;

public final class ConstrainedFieldFactory
{
  public static Field<?> createConstrainedField(ConstrainedType constrainedType)
  {
    Field<?> field = null;
    ColumnType columnType = constrainedType.getColumnType();
    switch (columnType)
    {
      case DATE:
        field = createDateField(constrainedType);
        break;
      case DECIMAL:
        field = createDecimalField(constrainedType);
        break;
      case INTEGER:
        field = createIntegerField(constrainedType);
        break;
      case TEXT:
        field = createTextField(constrainedType);
        break;
      case YESNO:
        field = createYesNoField(constrainedType);
        break;
    }
    field.setAutoValidate(true);
    field.setValidateOnBlur(false);
    ToolTipConfig toolTipConfig = new ToolTipConfig();
    toolTipConfig.setDismissDelay(0);
    field.setToolTip(toolTipConfig);
    return field;
  }

  private static DateField createDateField(ConstrainedType constrainedType)
  {
    DateField dateField = new DateConstrainedField();

    Date minimumDate = constrainedType.getDate(0);
    if (minimumDate != null)
    {
      dateField.setMinValue(minimumDate);
    }

    Date maximumDate = constrainedType.getDate(1);
    if (maximumDate != null)
    {
      dateField.setMaxValue(maximumDate);
    }

    Boolean isMustBePresent = constrainedType.getBoolean(2, Boolean.FALSE);
    dateField.setAllowBlank(!isMustBePresent);

    return dateField;
  }

  private static DecimalConstrainedField createDecimalField(ConstrainedType constrainedType)
  {
    DecimalConstrainedField spinnerField = new DecimalConstrainedField();

    BigDecimal minimumValue = constrainedType.getDecimal(0);
    if (minimumValue != null)
    {
      spinnerField.setMinValue(minimumValue);
    }

    BigDecimal maximumValue = constrainedType.getDecimal(1);
    if (maximumValue != null)
    {
      spinnerField.setMaxValue(maximumValue);
    }

    Boolean isMustBePresent = constrainedType.getBoolean(2, Boolean.FALSE);
    spinnerField.setAllowBlank(!isMustBePresent);

    return spinnerField;
  }

  private static IntegerSpinnerField createIntegerField(ConstrainedType constrainedType)
  {
    IntegerSpinnerField integerSpinnerField = new IntegerConstrainedField();

    Integer minimumValue = constrainedType.getInteger(0);
    if (minimumValue != null)
    {
      integerSpinnerField.setMinValue(minimumValue);
    }

    Integer maximumValue = constrainedType.getInteger(1);
    if (maximumValue != null)
    {
      integerSpinnerField.setMaxValue(maximumValue);
    }

    Boolean isMustBePresent = constrainedType.getBoolean(2, Boolean.FALSE);
    integerSpinnerField.setAllowBlank(!isMustBePresent);

    return integerSpinnerField;
  }

  private static TextField<?> createTextField(ConstrainedType constrainedType) // SafeTextField check - returned value is either a SafeTextField or SafeTextArea
  {
    TextField<?> textField;

    Boolean displayMultipleLines = constrainedType.getBoolean(2, Boolean.FALSE);
    if (displayMultipleLines)
    {
      textField = new TextAreaConstrainedField();
    }
    else
    {
      textField = new TextFieldConstrainedField();
    }

    Integer minimumLength = constrainedType.getInteger(0);
    if (minimumLength != null)
    {
      textField.setMinLength(minimumLength);
      if (minimumLength > 0)
      {
        textField.setAllowBlank(false);
      }
    }

    Integer maximumLength = constrainedType.getInteger(1);
    if (maximumLength != null)
    {
      textField.setMaxLength(maximumLength);
    }

    return textField;
  }

  private static YesNoField createYesNoField(ConstrainedType constrainedType)
  {
    YesNoField yesNoField = new YesNoConstrainedField();

    Boolean isMustBePresent = constrainedType.getBoolean(2, Boolean.FALSE);
    yesNoField.setAllowBlank(!isMustBePresent);

    return yesNoField;
  }

}
