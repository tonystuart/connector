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

package com.semanticexpression.connector.client.frame.editor.history;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.widget.Framelet;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.HistoryItem;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.exception.HistoryCacheMissException;

public class HistoryFramelet extends Framelet
{
  public static final String CURRENT = "Current";
  public static final Date CURRENT_DATE = new Date(8640000000000000L); // Maximum value of a JavaScript date, see: http://code.google.com/p/google-web-toolkit/issues/detail?id=4857

  private BasePagingLoader<BasePagingLoadResult<HistoryItem>> basePagingLoader;
  private ColumnModel columnModel;
  private ContentReference contentReference;
  private HistoryItem currentHistoryItem;
  private EditorFrame editorFrame;
  private Grid<HistoryItem> grid;
  private HashMap<Date, Content> historyCache = new HashMap<Date, Content>();
  private ListStore<HistoryItem> listStore;
  private Menu listViewContextMenu;
  private PagingLoadConfig pagingLoadConfig;
  private PagingRpcProxy pagingRpcProxy;
  private Menu toolBarButtonMenu;

  public HistoryFramelet(EditorFrame editorFrame)
  {
    super("History", Resources.HISTORY);
    this.editorFrame = editorFrame;
    setFrameletMenu(getToolBarButtonMenu());
    add(getGrid());
  }

  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    historyCache = new HashMap<Date, Content>();
    if (contentReference == null)
    {
      getListStore().removeAll();
    }
    else
    {
      getBasePagingLoader().load(getPagingLoadConfig()); // includes history requests for temporary content to invoke common load listener on callback
    }
  }

  private BasePagingLoader<BasePagingLoadResult<HistoryItem>> getBasePagingLoader()
  {
    if (basePagingLoader == null)
    {
      basePagingLoader = new BasePagingLoader<BasePagingLoadResult<HistoryItem>>(getPagingRpcProxy());
      basePagingLoader.addListener(Loader.Load, new LoadListener());
    }
    return basePagingLoader;
  }

  private ColumnModel getColumnMode()
  {
    if (columnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      columnConfigs.add(Utility.createColumnConfig(Keys.HISTORY_DATE, "Date", 100, new HistoryDateTimeFormat()));
      columnConfigs.add(new ColumnConfig(Keys.HISTORY_USER_NAMES, "User", 100));
      columnConfigs.add(new ColumnConfig(Keys.HISTORY_PROPERTIES, "Change", 100));
      columnModel = new ColumnModel(columnConfigs);
    }
    return columnModel;
  }

  public Content getContent(Date date)
  {
    Content content = date.equals(CURRENT_DATE) ? contentReference.getBaseContent() : historyCache.get(date);
    if (content == null)
    {
      throw new HistoryCacheMissException();
    }
    return content;
  }

  protected HistoryItem getCurrentHistoryItem()
  {
    if (currentHistoryItem == null)
    {
      currentHistoryItem = new HistoryItem();
      currentHistoryItem.set(Keys.HISTORY_DATE, CURRENT_DATE);
      currentHistoryItem.set(Keys.HISTORY_USER_NAMES, CURRENT);
      currentHistoryItem.set(Keys.HISTORY_PROPERTIES, CURRENT);
    }
    return currentHistoryItem;
  }

  public Grid<HistoryItem> getGrid()
  {
    if (grid == null)
    {
      grid = new Grid<HistoryItem>(getListStore(), getColumnMode());
      grid.getView().setForceFit(true);
      grid.getView().setAutoFill(true);
      grid.getView().setEmptyText("A history entry is created when you save your changes. Click on a history entry to view the content as it was at that point in time.<br/><br/>To compare two different versions of Text content, use control+click to select two history entries, then select Compare from the context menu.");
      grid.setContextMenu(getListViewContextMenu());
      grid.setBorders(false);
      grid.addListener(Events.RowClick, new HistorySelectListener());
    }
    return grid;
  }

  public ListStore<HistoryItem> getListStore()
  {
    if (listStore == null)
    {
      listStore = new ListStore<HistoryItem>(getBasePagingLoader());
    }
    return listStore;
  }

  private Menu getListViewContextMenu()
  {
    if (listViewContextMenu == null)
    {
      listViewContextMenu = new HistoryMenu();
    }
    return listViewContextMenu;
  }

  private PagingLoadConfig getPagingLoadConfig()
  {
    if (pagingLoadConfig == null)
    {
      pagingLoadConfig = new BasePagingLoadConfig();
      pagingLoadConfig.setOffset(0);
      pagingLoadConfig.setLimit(50);
    }
    return pagingLoadConfig;
  }

  private RpcProxy<BasePagingLoadResult<HistoryItem>> getPagingRpcProxy()
  {
    if (pagingRpcProxy == null)
    {
      pagingRpcProxy = new PagingRpcProxy();
    }
    return pagingRpcProxy;
  }

  private Menu getToolBarButtonMenu()
  {
    if (toolBarButtonMenu == null)
    {
      toolBarButtonMenu = new HistoryMenu();
    }
    return toolBarButtonMenu;
  }

  public void onCompareHistory()
  {
    List<HistoryItem> selectedItems = getGrid().getSelectionModel().getSelectedItems();

    if (selectedItems.size() != 2)
    {
      MessageBox.alert("Text Compare", "Please use CTRL+Click to select two History items and try again.", null);
      return;
    }

    HistoryItem item1 = selectedItems.get(0);
    HistoryItem item2 = selectedItems.get(1);

    Date item1Date = item1.get(Keys.HISTORY_DATE);
    Date item2Date = item2.get(Keys.HISTORY_DATE);

    if (item1Date.getTime() > item2Date.getTime())
    {
      Date tempDate = item1Date;
      item1Date = item2Date;
      item2Date = tempDate;
    }

    editorFrame.compareHistory(item1Date, item2Date);

  }

  public void onHistoryItemSelect(HistoryItem historyItem)
  {
    if (historyItem != null)
    {
      if (contentReference != null)
      {
        Date historyDate = historyItem.get(Keys.HISTORY_DATE);
        if (historyDate == HistoryFramelet.CURRENT_DATE)
        {
          editorFrame.displayCurrent(contentReference);
        }
        else if (historyCache.containsKey(historyDate))
        {
          Content content = historyCache.get(historyDate);
          editorFrame.displayHistory(contentReference, historyDate, content);
        }
        else
        {
          Id contentId = contentReference.getId();
          Directory.getConnectorService().retrieveContent(Utility.getAuthenticationToken(), contentId, historyDate, false, new RetrieveHistoryCallback(historyDate));
        }
      }
    }
  }

  public void onOutlineSelectionChanged(ContentReference contentReference)
  {
    if (this.contentReference != null)
    {
      if (this.contentReference.getHistoryDate() != null)
      {
        // Reset history to current in node that is losing selection, prevents issue adding new block if read only document node history was selected 
        this.contentReference.clearHistory();
      }
    }
    display(contentReference);
  }

  public void refresh()
  {
    getBasePagingLoader().load(getPagingLoadConfig());
  }

  public void replace(final Content historyContent)
  {
    Directory.getContentManager().replace(contentReference, historyContent); // after this, any ContentReference cached by an editor is stale
    editorFrame.displayCurrentWithoutSavingChanges(contentReference);
    display(contentReference);
  }

  public void replace(HistoryItem historyItem)
  {
    Date historyDate = historyItem.get(Keys.HISTORY_DATE);
    if (CURRENT_DATE.equals(historyDate))
    {
      MessageBox.alert("Replace", "Please select a non-current history item and try again.", null);
      return;
    }

    Content historyContent = historyCache.get(historyDate);
    if (historyContent == null)
    {
      MessageBox.alert("Replace", "Selected item is not in cache, please clear selection and try again.", null);
      return;
    }

    if (editorFrame.isModified())
    {
      MessageBox.confirm("Replace", "The current content contains unsaved changes. These unsaved changes will not be available in your history for subsequent restore. Would you like to replace the current content with the selected history item?", new ReplaceUnsavedCurrentHandler(historyContent));
    }
    else
    {
      replace(historyContent);
    }
  }

  private final class ClearListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      HistoryItem selectedItem = getGrid().getSelectionModel().getSelectedItem();
      if (selectedItem != null)
      {
      }
    }
  }

  private final class CompareListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      onCompareHistory();
    }
  }

  private final class HistoryMenu extends Menu
  {
    private MenuItem clearMenuItem;
    private MenuItem compareMenuItem;
    private MenuItem replaceMenuItem;

    public HistoryMenu()
    {
      add(getCompareMenuItem());
      add(getReplaceMenuItem());
      add(getClearMenuItem());
    }

    public MenuItem getClearMenuItem()
    {
      if (clearMenuItem == null)
      {
        clearMenuItem = new MenuItem("Clear");
        clearMenuItem.setIcon(Resources.HISTORY_CLEAR);
        clearMenuItem.addSelectionListener(new ClearListener());
      }
      return clearMenuItem;
    }

    public MenuItem getCompareMenuItem()
    {
      if (compareMenuItem == null)
      {
        compareMenuItem = new MenuItem("Compare");
        compareMenuItem.setIcon(Resources.HISTORY_COMPARE);
        compareMenuItem.addSelectionListener(new CompareListener());
      }
      return compareMenuItem;
    }

    public MenuItem getReplaceMenuItem()
    {
      if (replaceMenuItem == null)
      {
        replaceMenuItem = new MenuItem("Replace");
        replaceMenuItem.setIcon(Resources.HISTORY_REPLACE);
        replaceMenuItem.addSelectionListener(new ReplaceListener());
      }
      return replaceMenuItem;
    }

  }

  private final class HistorySelectListener implements Listener<GridEvent<HistoryItem>>
  {
    @Override
    public void handleEvent(GridEvent<HistoryItem> be)
    {
      HistoryItem historyItem = be.getModel();
      onHistoryItemSelect(historyItem);
    }
  }

  private final class LoadListener implements Listener<LoadEvent>
  {
    @Override
    public void handleEvent(LoadEvent loadEvent)
    {
      Object data = loadEvent.getData();
      if (data instanceof PagingLoadResult<?>)
      {
        PagingLoadResult<?> pagingLoadResult = (PagingLoadResult<?>)data;
        @SuppressWarnings("unchecked")
        List<HistoryItem> list = (List<HistoryItem>)pagingLoadResult.getData();
        list.add(0, getCurrentHistoryItem());
        Scheduler.get().scheduleDeferred(new ScheduledCommand()
        {
          @Override
          public void execute()
          {
            HistoryItem historyItem = getListStore().getAt(0);
            getGrid().getSelectionModel().select(historyItem, false);
          }
        });
      }
    }
  }

  private final class PagingRpcProxy extends RpcProxy<BasePagingLoadResult<HistoryItem>>
  {
    @Override
    public void load(Object loadConfig, AsyncCallback<BasePagingLoadResult<HistoryItem>> callback)
    {
      if (contentReference != null) // may be null when content is removed from outline tree
      {
        Id contentId = contentReference.getId();
        // For new content, the server throws an AuthorizationException, which the PagingLoader ignores, resulting in the GridView displaying the EmptyText.
        Directory.getConnectorService().getHistory(Utility.getAuthenticationToken(), contentId, (PagingLoadConfig)loadConfig, callback);
      }
    }
  }

  private final class ReplaceListener extends SelectionListener<MenuEvent>
  {
    public void componentSelected(MenuEvent ce)
    {
      HistoryItem historyItem = getGrid().getSelectionModel().getSelectedItem();
      if (historyItem != null)
      {
        replace(historyItem);
      }
    }
  }

  private final class ReplaceUnsavedCurrentHandler implements Listener<MessageBoxEvent>
  {
    private final Content historyContent;

    private ReplaceUnsavedCurrentHandler(Content historyContent)
    {
      this.historyContent = historyContent;
    }

    @Override
    public void handleEvent(MessageBoxEvent messageBoxEvent)
    {
      if (Dialog.YES.equals(messageBoxEvent.getButtonClicked().getItemId()))
      {
        replace(historyContent);
      }
    }
  }

  private final class RetrieveHistoryCallback extends FailureReportingAsyncCallback<List<Content>>
  {
    private final Date historyDate;

    private RetrieveHistoryCallback(Date historyDate)
    {
      this.historyDate = historyDate;
    }

    @Override
    public void onSuccess(List<Content> result)
    {
      Content content = result.get(0);
      historyCache.put(historyDate, content);
      editorFrame.displayHistory(contentReference, historyDate, content);
    }
  }

}
