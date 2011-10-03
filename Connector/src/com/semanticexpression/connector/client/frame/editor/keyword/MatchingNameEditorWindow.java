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

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
import com.semanticexpression.connector.client.widget.BaseEditorWindow;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public abstract class MatchingNameEditorWindow extends BaseEditorWindow
{
  private DelayedTask matchingNameFinderDelayedTask;
  private ListStore<Association> matchingNameListStore;
  private Text matchingNameListText;
  private ListView<Association> matchingNameListView;
  protected MatchingNameType matchingNameType;

  public MatchingNameEditorWindow(MatchingNameType matchingNameType, AbstractImagePrototype windowIcon, String windowTitle, String nameLabel, String nameEmptyString, String matchingNameListLabel, String valueLabel, ListStore<Association> listStore)
  {
    super(windowIcon, windowTitle, nameLabel, valueLabel, listStore);
    this.matchingNameType = matchingNameType;
    getNameTextField().setEmptyText(nameEmptyString); // nameTextField is created by super()
    getMatchingNameListText().setText(matchingNameListLabel); // matchingNameListText is created by super() via polymorphic call to derived class initializeForm()
  }

  @Override
  public void edit(Association association, boolean isNew)
  {
    super.edit(association, isNew);

    getMatchingNameListStore().removeAll();
  }

  protected DelayedTask getMatchingNameFinderDelayedTask()
  {
    if (matchingNameFinderDelayedTask == null)
    {
      matchingNameFinderDelayedTask = new DelayedTask(new MatchingNameFinderDelayedTask());
    }
    return matchingNameFinderDelayedTask;
  }

  protected ListStore<Association> getMatchingNameListStore()
  {
    if (matchingNameListStore == null)
    {
      matchingNameListStore = new ListStore<Association>();
    }
    return matchingNameListStore;
  }

  protected Text getMatchingNameListText()
  {
    if (matchingNameListText == null)
    {
      matchingNameListText = new Text();
    }
    return matchingNameListText;
  }

  public ListView<Association> getMatchingNameListView()
  {
    if (matchingNameListView == null)
    {
      matchingNameListView = new ListView<Association>(getMatchingNameListStore());
      matchingNameListView.setTemplate("<tpl for=\".\"><div class='x-view-item'>{" + Keys.NAME + "} ({" + Keys.COUNT + "})</div></tpl>");
      matchingNameListView.addListener(Events.Select, new MatchingNameSelectListener());
    }
    return matchingNameListView;
  }

  protected void getMatchingNames()
  {
    getMatchingNameFinderDelayedTask().delay(0);
  }

  protected MatchingNameType getMatchingNameType()
  {
    return matchingNameType;
  }

  @Override
  public Object getName()
  {
    String value = getNameTextField().getValue();
    String normalizedValue = value == null ? null : value.toLowerCase();
    return normalizedValue;
  }

  @Override
  protected void onNameChange()
  {
    super.onNameChange();

    getMatchingNameFinderDelayedTask().delay(500);
  }

  private final class MatchingNameCallback extends FailureReportingAsyncCallback<BasePagingLoadResult<Association>>
  {
    @Override
    public void onSuccess(BasePagingLoadResult<Association> result)
    {
      getMatchingNameListStore().removeAll();
      getMatchingNameListStore().add(result.getData());
    }
  }

  final class MatchingNameFinderDelayedTask implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      String wildcard = getNameTextField().getValue();
      if (wildcard != null && wildcard.length() > 0)
      {
        Directory.getConnectorService().getMatchingNames(Utility.getAuthenticationToken(), wildcard, getMatchingNameType(), new BasePagingLoadConfig(0, 50), new MatchingNameCallback());
      }
    }
  }

  final class MatchingNameSelectListener implements Listener<ListViewEvent<Association>>
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
