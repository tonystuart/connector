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

package com.semanticexpression.connector.client.frame.search;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.keyword.MatchingNameEditorWindow;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;

public abstract class TagWindow extends MatchingNameEditorWindow
{
  private static final String RADIO_GROUP_NAME = "NewTagWindowRadio";
  private Radio noRadio;
  private RadioButtonLayoutContainer valueField;
  private Radio yesRadio;

  public TagWindow(AbstractImagePrototype windowIcon, String windowTitle, String nameLabel, String nameEmptyString, String matchingNameListLabel, String valueLabel, ListStore<Association> listStore)
  {
    super(null, windowIcon, windowTitle, nameLabel, nameEmptyString, matchingNameListLabel, valueLabel, listStore);
  }

  @Override
  protected void displayValue(Association association)
  {
    // See tag()
  }

  @Override
  protected MatchingNameType getMatchingNameType()
  {
    return Utility.isSet(getYesRadio().getValue()) ? MatchingNameType.PRIVATE_TAG : MatchingNameType.PUBLIC_TAG;
  }

  public Radio getNoRadio()
  {
    if (noRadio == null)
    {
      noRadio = new MatchingNamesRadio();
      noRadio.setBoxLabel("No");
      noRadio.setName(RADIO_GROUP_NAME);
    }
    return noRadio;
  }

  protected TagVisibility getTagVisibility()
  {
    return Utility.isSet(getYesRadio().getValue()) ? TagVisibility.MY_PRIVATE : TagVisibility.MY_PUBLIC;
  }

  @Override
  protected Object getValue()
  {
    return getYesRadio().getValue();
  }

  @Override
  public RadioButtonLayoutContainer getValueField()
  {
    if (valueField == null)
    {
      valueField = new RadioButtonLayoutContainer();
    }
    return valueField;
  }

  public Radio getYesRadio()
  {
    if (yesRadio == null)
    {
      yesRadio = new MatchingNamesRadio();
      yesRadio.setBoxLabel("Yes");
      yesRadio.setName(RADIO_GROUP_NAME);
    }
    return yesRadio;
  }

  @Override
  protected void initializeForm()
  {
    add(getNameText(), new RowData(Style.DEFAULT, Style.DEFAULT));
    add(getNameTextField(), new RowData(1.0, Style.DEFAULT));
    add(getMatchingNameListText(), new RowData(1.0, Style.DEFAULT, LABEL_MARGINS));
    add(getMatchingNameListView(), new RowData(1.0, 1.0));
    add(getValueText(), new RowData(Style.DEFAULT, Style.DEFAULT, LABEL_MARGINS));
    add(getValueField(), new RowData(1.0, 30));
  }

  protected void reset()
  {
    getNameTextField().clear();
    getMatchingNameListStore().removeAll();
    getYesRadio().setValue(Boolean.TRUE);
    setFocusWidget(getNameTextField());
    updateFormState();
  }

  private final class MatchingNamesRadio extends Radio
  {
    @Override
    protected void onClick(ComponentEvent be)
    {
      super.onClick(be);
      getMatchingNames();
    }
  }

  final class RadioButtonLayoutContainer extends LayoutContainer
  {
    private RadioButtonLayoutContainer()
    {
      setLayout(new RowLayout(Orientation.HORIZONTAL));
      add(new Html(), new RowData(0.20, -1));
      add(getYesRadio(), new RowData(0.20, -1));
      add(new Html(), new RowData(0.20, -1));
      add(getNoRadio(), new RowData(0.20, -1));
      add(new Html(), new RowData(0.20, -1));
    }
  }

}