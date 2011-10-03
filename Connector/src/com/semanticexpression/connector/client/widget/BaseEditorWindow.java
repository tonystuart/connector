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

import java.util.Date;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public abstract class BaseEditorWindow extends ListStoreEditorWindow implements OkayCancelHandler
{
  public static final Margins LABEL_MARGINS = new Margins(5, 0, 0, 0);
  private LayoutContainer container;
  protected String nameLabel;
  protected Text nameText;
  protected SafeTextField<String> nameTextField;
  protected String valueLabel;

  protected Text valueText;

  public BaseEditorWindow(AbstractImagePrototype windowIcon, String windowTitle, String nameLabel, String valueLabel, ListStore<Association> listStore)
  {
    super(listStore);

    this.nameLabel = nameLabel;
    this.valueLabel = valueLabel;

    setWindowSize();
    setIcon(windowIcon);
    setHeading(windowTitle);
    setClosable(false); // tricky to reliably intercept, hide() is invoked during initial render
    setLayout(new FitLayout());
    container = new LayoutContainer(); // work around for GXT.isIE margin issue
    container.setLayout(new RowLayout(Orientation.VERTICAL));
    super.add(container, new FitData(5));
    initializeForm();
    setBottomComponent(getOkayCancelToolBar());
    updateFormState();
  }

  @Override
  public boolean add(Widget widget, LayoutData layoutData)
  {
    return container.add(widget, layoutData);
  }

  protected void createContainer()
  {
  }

  protected void displayName(Association association, boolean isNew)
  {
    getNameTextField().setValue(association.<String> get(Keys.NAME));
  }

  protected abstract void displayValue(Association association);

  public void edit(Association association, boolean isNew)
  {
    super.edit(association, isNew);

    displayName(association, isNew);
    displayValue(association);

    setFocusWidget(isNew ? getNameTextField() : getValueField());
    updateFormState();
  }

  public Object getName()
  {
    return getNameTextField().getValue();
  }

  public Text getNameText()
  {
    if (nameText == null)
    {
      nameText = new Text(nameLabel);
    }
    return nameText;
  }

  public SafeTextField<String> getNameTextField()
  {
    if (nameTextField == null)
    {
      nameTextField = new SafeTextField<String>();
      nameTextField.addListener(Events.Change, new NameChangeListener());
      nameTextField.addKeyListener(new NameKeyListener());
    }
    return nameTextField;
  }

  protected abstract Object getValue();

  public abstract Component getValueField();

  public Text getValueText()
  {
    if (valueText == null)
    {
      valueText = new Text(valueLabel);
    }
    return valueText;
  }

  protected void initializeForm()
  {
    add(getNameText(), new RowData(Style.DEFAULT, Style.DEFAULT));
    add(getNameTextField(), new RowData(1.0, Style.DEFAULT));
    add(getValueText(), new RowData(Style.DEFAULT, Style.DEFAULT, LABEL_MARGINS));
    add(getValueField(), new RowData(1.0, 1.0));
  }

  public void okay()
  {
    Date timestamp = new Date();
    String userName = Utility.getUserName();
    if (isNew)
    {
      association.setTransient(Keys.CREATED_BY, userName);
      association.setTransient(Keys.CREATED_AT, timestamp);
      isNew = false;
    }
    association.set(Keys.NAME, getName());
    association.set(Keys.VALUE, getValue());
    association.setTransient(Keys.MODIFIED_BY, userName);
    association.setTransient(Keys.MODIFIED_AT, timestamp);
    hide();
  }

  protected void onNameChange()
  {
    updateFormState();
  }

  protected void setWindowSize()
  {
    setSize("300", "300");
  }

  protected void updateFormState()
  {
    Object value = getName();
    getOkayCancelToolBar().getOkayButton().setEnabled(value != null);
  }

  public final class NameChangeListener implements Listener<FieldEvent>
  {
    @Override
    public void handleEvent(FieldEvent be)
    {
      onNameChange();
    }
  }

  public final class NameKeyListener extends KeyListener
  {
    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      onNameChange();
    }
  }

}