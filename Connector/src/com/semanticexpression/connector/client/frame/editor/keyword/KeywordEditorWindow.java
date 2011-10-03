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

package com.semanticexpression.connector.client.frame.editor.keyword;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.InputSlider;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.semanticexpression.connector.client.frame.editor.EditorStore;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public final class KeywordEditorWindow extends MatchingNameEditorWindow
{
  private InputSlider valueField;

  public KeywordEditorWindow(EditorStore editorStore)
  {
    super(MatchingNameType.KEYWORD, Resources.KEYWORD, "Keyword Editor", "Enter Keyword Name:", "Type characters to find matching keywords", "or select existing keyword that matches:", "Relevance of Keyword to Content:", editorStore);
  }

  @Override
  protected void displayValue(Association association)
  {
    Integer value = association.get(Keys.VALUE);
    if (value != null)
    {
      getValueField().setValue(value);
    }
  }

  @Override
  protected Object getValue()
  {
    return getValueField().getValue();
  }

  @Override
  public InputSlider getValueField()
  {
    if (valueField == null)
    {
      valueField = new InputSlider();
      valueField.setInputWidth(30);
      valueField.setMinValue(1);
      valueField.setMaxValue(100);
      valueField.setIncrement(1);
      valueField.setValue(50);
    }
    return valueField;
  }

  @Override
  protected void initializeForm()
  {
    add(getNameText(), new RowData(Style.DEFAULT, Style.DEFAULT));
    add(getNameTextField(), new RowData(1.0, Style.DEFAULT));
    add(getMatchingNameListText(), new RowData(1.0, Style.DEFAULT, LABEL_MARGINS));
    add(getMatchingNameListView(), new RowData(1.0, 1.0));
    add(getValueText(), new RowData(Style.DEFAULT, Style.DEFAULT, LABEL_MARGINS));
    add(getValueField(), new RowData(1.0, Style.DEFAULT));
  }

}
