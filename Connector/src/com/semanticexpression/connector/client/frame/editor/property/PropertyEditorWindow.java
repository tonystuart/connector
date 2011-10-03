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

package com.semanticexpression.connector.client.frame.editor.property;

import com.extjs.gxt.ui.client.store.ListStore;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.BaseEditorWindow;
import com.semanticexpression.connector.client.widget.ValueField;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public class PropertyEditorWindow extends BaseEditorWindow
{
  private ValueField valueField;

  public PropertyEditorWindow(ListStore<Association> listStore)
  {
    super(Resources.PROPERTY, "Property Editor", "Property Name:", "Property Value:", listStore);
  }

  @Override
  protected void displayValue(Association association)
  {
    getValueField().setValue(association.<String> get(Keys.VALUE));
  }

  @Override
  protected Object getValue()
  {
    return getValueField().getValue();
  }

  @Override
  public ValueField getValueField()
  {
    if (valueField == null)
    {
      valueField = new ValueField();
    }
    return valueField;
  }
}
