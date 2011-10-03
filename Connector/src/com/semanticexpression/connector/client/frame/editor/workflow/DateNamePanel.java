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

package com.semanticexpression.connector.client.frame.editor.workflow;

import java.util.Date;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.WorkflowConstants;

public final class DateNamePanel extends LayoutContainer
{
  private DateField dateField;
  private String dateLabelString;
  private Text dateText;
  private Radio groupRadio;
  private DelayedTask nameFinderDelayedTask;
  private String nameLabelString;
  private ListStore<Association> nameListStore;
  private ListView<Association> nameListView;
  private Text nameText;
  private SafeTextField<String> nameTextField;
  private LayoutContainer nameTypeLayoutContainer;
  private String originalName;
  private String radioName;
  private Radio userRadio;

  public DateNamePanel(String dateLabelString, String nameLabelString, String radioName)
  {
    this.dateLabelString = dateLabelString;
    this.nameLabelString = nameLabelString;
    this.radioName = radioName;

    setLayout(new RowLayout(Orientation.VERTICAL));
    add(getDateText());
    add(getDateField(), new RowData(1.0, Style.DEFAULT, new Margins()));
    add(getNameTypeLayoutContainer(), new RowData(1.0, 30.0, new Margins(5, 0, 0, 0)));
    add(getNameTextField(), new RowData(1.0, Style.DEFAULT, new Margins()));
    add(getNameListView(), new RowData(1.0, 1.0, new Margins(5, 0, 0, 0)));
  }

  public void clear()
  {
    getNameListStore().removeAll();
  }

  public void display(Date date, String name, Integer completedByType)
  {
    originalName = name;
    getDateField().setValue(date);
    getNameTextField().setValue(name);
    boolean isUser = completedByType == null || completedByType == WorkflowConstants.TYPE_1_USER;
    getUserRadio().setValue(isUser);
    getGroupRadio().setValue(!isUser);
    getMatchingNames(name == null ? "" : name, isUser ? WorkflowConstants.TYPE_1_USER : WorkflowConstants.TYPE_2_GROUP);
  }

  public DateField getDateField()
  {
    if (dateField == null)
    {
      dateField = new DateField();
    }
    return dateField;
  }

  public Text getDateText()
  {
    if (dateText == null)
    {
      dateText = new Text(dateLabelString);
    }
    return dateText;
  }

  public Radio getGroupRadio()
  {
    if (groupRadio == null)
    {
      groupRadio = new DateNameRadio();
      groupRadio.setName(radioName);
      groupRadio.setBoxLabel("Group");
      groupRadio.setHideLabel(true);
    }
    return groupRadio;
  }

  public Widget getInvalidWidget()
  {
    String name = getNameTextField().getValue();
    if (name != null && (!name.equals(originalName) && getNameListStore().findModel(Keys.NAME, name) == null))
    {
      MessageBox.alert("Invalid Name", "Please select a name from the list by double-clicking on it.", null);
      return getNameTextField();
    }
    return null;
  }

  private void getMatchingNames(String wildcard, int workflowType)
  {
    MatchingNameType matchingNameType = workflowType == WorkflowConstants.TYPE_1_USER ? MatchingNameType.USER : MatchingNameType.GROUP;
    Directory.getConnectorService().getMatchingNames(Utility.getAuthenticationToken(), wildcard, matchingNameType, new BasePagingLoadConfig(0, 50), new MatchingNameCallback());
  }

  public String getName()
  {
    return getNameTextField().getValue();
  }

  private DelayedTask getNameFinderDelayedTask()
  {
    if (nameFinderDelayedTask == null)
    {
      nameFinderDelayedTask = new DelayedTask(new NameFinderDelayedTask());
    }
    return nameFinderDelayedTask;
  }

  private ListStore<Association> getNameListStore()
  {
    if (nameListStore == null)
    {
      nameListStore = new ListStore<Association>();
    }
    return nameListStore;
  }

  public ListView<Association> getNameListView()
  {
    if (nameListView == null)
    {
      nameListView = new ListView<Association>(getNameListStore());
      nameListView.setTemplate("<tpl for=\".\"><div class='x-view-item'>{" + Keys.NAME + "}</div></tpl>");
      nameListView.addListener(Events.DoubleClick, new NameListViewDoubleClickListener());
    }
    return nameListView;
  }

  public Text getNameText()
  {
    if (nameText == null)
    {
      nameText = new Text(nameLabelString);
    }
    return nameText;
  }

  public SafeTextField<String> getNameTextField()
  {
    if (nameTextField == null)
    {
      nameTextField = new SafeTextField<String>();
      nameTextField.setEmptyText("Type to filter names. Double click to select.");
      nameTextField.addListener(Events.Change, new NameChangeListener());
      nameTextField.addKeyListener(new NameKeyListener());
    }
    return nameTextField;
  }

  public LayoutContainer getNameTypeLayoutContainer()
  {
    if (nameTypeLayoutContainer == null)
    {
      nameTypeLayoutContainer = new LayoutContainer();
      nameTypeLayoutContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
      nameTypeLayoutContainer.add(getNameText(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(5, 0, 0, 0)));
      nameTypeLayoutContainer.add(getUserRadio(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
      nameTypeLayoutContainer.add(getGroupRadio());
    }
    return nameTypeLayoutContainer;
  }

  public int getType()
  {
    return getUserRadio().getValue() ? WorkflowConstants.TYPE_1_USER : WorkflowConstants.TYPE_2_GROUP;
  }

  public Radio getUserRadio()
  {
    if (userRadio == null)
    {
      userRadio = new DateNameRadio();
      userRadio.setName(radioName);
      userRadio.setBoxLabel("User");
      userRadio.setHideLabel(true);
      userRadio.setValue(true);
    }
    return userRadio;
  }

  private void onNameChange()
  {
    getNameFinderDelayedTask().delay(500);
  }

  public class DateNameRadio extends Radio
  {
    @Override
    protected void onClick(ComponentEvent be)
    {
      onNameChange();
      super.onClick(be);
    }
  }

  private final class MatchingNameCallback extends FailureReportingAsyncCallback<BasePagingLoadResult<Association>>
  {
    @Override
    public void onSuccess(BasePagingLoadResult<Association> result)
    {
      getNameListStore().removeAll();
      getNameListStore().add(result.getData());
    }
  }

  public final class NameChangeListener implements Listener<FieldEvent>
  {
    @Override
    public void handleEvent(FieldEvent be)
    {
      onNameChange();
    }
  }

  private final class NameFinderDelayedTask implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      String wildcard = getNameTextField().getValue();
      if (wildcard == null)
      {
        wildcard = "";
      }
      int type = getType();
      getMatchingNames(wildcard, type);
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

  private final class NameListViewDoubleClickListener implements Listener<ListViewEvent<Association>>
  {
    @Override
    public void handleEvent(ListViewEvent<Association> listViewEvent)
    {
      Association selectedName = listViewEvent.getModel();
      if (selectedName != null)
      {
        String name = selectedName.get(Keys.NAME);
        getNameTextField().setValue(name);
      }
    }
  }

}
