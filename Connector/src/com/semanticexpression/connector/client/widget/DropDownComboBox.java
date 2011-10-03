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

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.semanticexpression.connector.shared.Keys;

public class DropDownComboBox<T extends ModelData> extends ComboBox<T>
{
  public DropDownComboBox()
  {
    setEditable(false);
    setForceSelection(true);
    setTriggerAction(TriggerAction.ALL);
    setStore(new ListStore<T>());
    setDisplayField(Keys.NAME);
  }
  
  public void add(List<T> models)
  {
    getStore().add(models);
  }
  
  public <V> V getSimpleValue()
  {
    V value = null;
    T modelData = getValue();
    if (modelData != null)
    {
      value = modelData.get(Keys.VALUE);
    }
    return value;
  }

  public void setSimpleValue(Object value)
  {
    T model = getStore().findModel(Keys.VALUE, value);
    if (model != null)
    {
      setValue(model);
    }
  }
}
