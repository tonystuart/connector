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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Numeric;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public final class FormulaEditorWindow extends Window implements OkayCancelHandler
{
  private static final RowData FIELD_ROW_DATA = new RowData(1.0, Style.DEFAULT);
  private static final Margins LABEL_MARGINS = new Margins(5, 0, 0, 0);
  private static final RowData LABEL_ROW_DATA = new RowData(Style.DEFAULT, Style.DEFAULT, LABEL_MARGINS);

  private Association association;
  private Numeric firstColumnNumeric;
  private Text firstColumnText;
  private SafeTextArea formulaTextArea;
  private Numeric lastColumnNumeric;
  private Text lastColumnText;
  private Numeric nameColumnNumeric;
  private Text nameColumnText;
  private ToolBar okayCancelToolBar;
  private Text resultFormatText;
  private SafeTextField<String> resultFormatTextField;

  public FormulaEditorWindow()
  {
    setSize(300, 350);
    setHeading("Formula Editor");
    setIcon(Resources.TABLE_EDIT_FORMULA_ROW);
    setLayout(new FitLayout());
    LayoutContainer c = new LayoutContainer(); // work around for GXT.isIE margin issue
    c.setLayout(new RowLayout(Orientation.VERTICAL));
    c.add(getFormulaTextArea(), new RowData(1.0, 1.0));
    c.add(getResultFormatText(), LABEL_ROW_DATA);
    c.add(getResultFormatTextField(), FIELD_ROW_DATA);
    c.add(getNameColumnText(), LABEL_ROW_DATA);
    c.add(getNameColumnNumeric(), FIELD_ROW_DATA);
    c.add(getFirstColumnText(), LABEL_ROW_DATA);
    c.add(getFirstColumnNumeric(), FIELD_ROW_DATA);
    c.add(getLastColumnText(), LABEL_ROW_DATA);
    c.add(getLastColumnNumeric(), FIELD_ROW_DATA);
    add(c, new FitData(5));
    setBottomComponent(getOkayCancelToolBar());
    setFocusWidget(getFormulaTextArea());
  }

  @Override
  public void cancel()
  {
    hide();
  }

  public void display(Component component, Association association)
  {
    this.association = association;

    getFormulaTextArea().setValue(association.<String> get(Keys.FORMULA));
    getResultFormatTextField().setValue(association.<String> get(Keys.FORMULA_RESULT_FORMAT));
    getNameColumnNumeric().setValue(association.<Number> get(Keys.FORMULA_NAME_COLUMN));
    getFirstColumnNumeric().setValue(association.<Number> get(Keys.FORMULA_FIRST_COLUMN));
    getLastColumnNumeric().setValue(association.<Number> get(Keys.FORMULA_LAST_COLUMN));

    show();
    toFront();
    alignTo(component.getElement(), "c-c?", null);
  }

  public Numeric getFirstColumnNumeric()
  {
    if (firstColumnNumeric == null)
    {
      firstColumnNumeric = new Numeric("Defaults to second column in table");
    }
    return firstColumnNumeric;
  }

  public Text getFirstColumnText()
  {
    if (firstColumnText == null)
    {
      firstColumnText = new Text("First Column to Participate in Formula:");
    }
    return firstColumnText;
  }

  public SafeTextArea getFormulaTextArea()
  {
    if (formulaTextArea == null)
    {
      formulaTextArea = new SafeTextArea();
      formulaTextArea.setFieldLabel("Formula");
      formulaTextArea.setEmptyText("Enter formula here");
    }
    return formulaTextArea;
  }

  public Numeric getLastColumnNumeric()
  {
    if (lastColumnNumeric == null)
    {
      lastColumnNumeric = new Numeric("Defaults to last column in table");
    }
    return lastColumnNumeric;
  }

  public Text getLastColumnText()
  {
    if (lastColumnText == null)
    {
      lastColumnText = new Text("Last Column to Participate in Formula:");
    }
    return lastColumnText;
  }

  public Numeric getNameColumnNumeric()
  {
    if (nameColumnNumeric == null)
    {
      nameColumnNumeric = new Numeric("Defaults to first column in table");
    }
    return nameColumnNumeric;
  }

  public Text getNameColumnText()
  {
    if (nameColumnText == null)
    {
      nameColumnText = new Text("Column Containing Row Name:");
    }
    return nameColumnText;
  }

  public ToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(this);
    }
    return okayCancelToolBar;
  }

  public Text getResultFormatText()
  {
    if (resultFormatText == null)
    {
      resultFormatText = new Text("Result Format:");
    }
    return resultFormatText;
  }

  public SafeTextField<String> getResultFormatTextField()
  {
    if (resultFormatTextField == null)
    {
      resultFormatTextField = new SafeTextField<String>();
      resultFormatTextField.setEmptyText("Defaults to #.##");
    }
    return resultFormatTextField;
  }

  @Override
  public void okay()
  {
    association.set(Keys.FORMULA, getFormulaTextArea().getValue());
    association.set(Keys.FORMULA_RESULT_FORMAT, getResultFormatTextField().getValue());
    association.set(Keys.FORMULA_NAME_COLUMN, getNameColumnNumeric().getValue());
    association.set(Keys.FORMULA_FIRST_COLUMN, getFirstColumnNumeric().getValue());
    association.set(Keys.FORMULA_LAST_COLUMN, getLastColumnNumeric().getValue());

    hide();
  }
}
