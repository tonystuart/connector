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

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.widget.EnumIconRenderer;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Credential.AuthenticationType;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.SearchResult;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;
import com.semanticexpression.connector.shared.TagFilter.TagFilterType;

public class SearchResultPanel extends ContentPanel
{
  private static final int PAGE_SIZE = 20;

  private static final String TEMPLATE = "<div class='connector-SearchResult'>\n" + //
      "  <div class='connector-SearchResultDivider'>&nbsp;</div>\n" + //
      "  <span class='connector-SearchResultLabel'>Summary:</span> {" + Keys.SUMMARY + "}\n" + //
      "  <tpl if=\"typeof " + Keys.SEMANTIC_TAGS + " != 'undefined'\">\n" + //
      "      <tpl for=\"" + Keys.SEMANTIC_TAGS + "\">\n" + //
      "          <tpl if=\"xindex == 1\">\n" + //
      "            <div class='connector-SearchResultDivider'>&nbsp;</div>\n" + //
      "            <span class='connector-SearchResultLabel'>Semantic Tags:</span>\n" + //
      "          </tpl>\n" + //
      "          <tpl if=\"xindex &gt; 1\">&bull;</tpl>\n" + //
      "          <span id='{[\"id\"+xindex]}' class='connector-SearchResultSemanticTag'>{" + Keys.NAME + "}</span>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.MY_PRIVATE.name() + "'\"> (private)</tpl>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.MY_PUBLIC.name() + "'\"> (public)</tpl>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.OTHER_PUBLIC.name() + "'\"> (other)</tpl>\n" + //
      "      </tpl>\n" + //
      "  </tpl>\n" + //
      "  <tpl if=\"typeof " + Keys.TAG_LIST_CONTENT + " != 'undefined'\">\n" + //
      "      <tpl for=\"" + Keys.TAG_LIST_CONTENT + "\">\n" + //
      "          <tpl if=\"xindex == 1\">\n" + //
      "            <div class='connector-SearchResultDivider'>&nbsp;</div>\n" + //
      "            <span class='connector-SearchResultLabel'>Content Tags:</span>\n" + //
      "          </tpl>\n" + //
      "          <tpl if=\"xindex &gt; 1\">&bull;</tpl>\n" + //
      "          <span id='{[\"id\"+xindex]}' class='connector-SearchResultContentTag'>{" + Keys.NAME + "}</span>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.MY_PRIVATE.name() + "'\"> (private)</tpl>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.MY_PUBLIC.name() + "'\"> (public)</tpl>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.OTHER_PUBLIC.name() + "'\"> (other)</tpl>\n" + //
      "      </tpl>\n" + //
      "  </tpl>\n" + //
      "  <tpl if=\"typeof " + Keys.TAG_LIST_AUTHOR + " != 'undefined'\">\n" + //
      "      <tpl for=\"" + Keys.TAG_LIST_AUTHOR + "\">\n" + //
      "          <tpl if=\"xindex == 1\">\n" + //
      "            <div <div class='connector-SearchResultDivider'>&nbsp;</div>\n" + //
      "            <span class='connector-SearchResultLabel'>Author Tags:</span>\n" + //
      "          </tpl>\n" + //
      "          <tpl if=\"xindex &gt; 1\">&bull;</tpl>\n" + //
      "          <span id='{[\"id\"+xindex]}' class='connector-SearchResultAuthorTag'>{" + Keys.NAME + "}</span>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.MY_PRIVATE.name() + "'\"> (private)</tpl>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.MY_PUBLIC.name() + "'\"> (public)</tpl>\n" + //
      "          <tpl if=\"" + Keys.TAG_VISIBILITY + " == '" + TagVisibility.OTHER_PUBLIC.name() + "'\"> (other)</tpl>\n" + //
      "      </tpl>\n" + //
      "  </tpl>\n" + //
      "  </div>";

  private static final XTemplate XTEMPLATE = XTemplate.create(TEMPLATE);

  private BasePagingLoader<BasePagingLoadResult<SearchResult>> basePagingLoader;
  private ColumnModel columnModel;
  private ColumnConfig contentTypeColumnConfig;
  private Grid<SearchResult> grid;
  private Menu gridSearchMenu;
  private boolean isAutoOpen;
  private ListStore<SearchResult> listStore;
  private BasePagingLoadConfig pagingLoadConfig;
  private RpcProxy<BasePagingLoadResult<SearchResult>> pagingRpcProxy;
  private PagingToolBar pagingToolBar;
  private RowExpander rowExpander;
  private SearchFrame searchFrame;

  public SearchResultPanel(SearchFrame searchFrame)
  {
    this.searchFrame = searchFrame;

    setLayout(new FitLayout());
    setHeaderVisible(false);
    add(getGrid());
    setBottomComponent(getPagingToolBar());
  }

  public void displayResult(BasePagingLoadResult<SearchResult> basePagingLoadResult)
  {
    getListStore().removeAll();

    List<SearchResult> searchResults = basePagingLoadResult.getData();
    if (searchResults.size() == 0)
    {
      String message = "Your search for \"" + searchFrame.getSearchTermsTextField().getValue() + "\" did not return any results. Note that common words such as \"the\" are not indexed and cannot be used as search terms.";
      if (Utility.getCredential().getAuthenticationType() == AuthenticationType.UNAUTHENTICATED)
      {
        message += "<br/><br/>You currently have read only guest access. To search your own content, please login and try again.";
      }
      MessageBox.alert("No Matching Content", message, null);
    }
    else
    {
      // Load and sort data, update paging toolbar, schedule row expander
      basePagingLoader.fireEvent(Loader.Load, new LoadEvent(basePagingLoader, getPagingLoadConfig(), basePagingLoadResult));
    }
  }

  private void expandAll()
  {
    int rowCount = getListStore().getCount();
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      getRowExpander().expandRow(rowOffset);
    }
  }

  private BasePagingLoader<BasePagingLoadResult<SearchResult>> getBasePagingLoader()
  {
    if (basePagingLoader == null)
    {
      basePagingLoader = new BasePagingLoader<BasePagingLoadResult<SearchResult>>(getPagingRpcProxy());
      basePagingLoader.useLoadConfig(getPagingLoadConfig());
      basePagingLoader.addListener(Loader.Load, new LoadListener());
    }
    return basePagingLoader;
  }

  public ColumnModel getColumnModel()
  {
    if (columnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      columnConfigs.add(getRowExpander());
      columnConfigs.add(getContentTypeColumnConfig());
      columnConfigs.add(new ColumnConfig(Keys.TITLE, "Title", 100));
      columnConfigs.add(new ColumnConfig(Keys.CREATED_BY, "Created By", 100));
      columnConfigs.add(Utility.createDateTimeColumnConfig(Keys.MODIFIED_AT, "Modified At", 100));
      columnConfigs.add(new ColumnConfig(Keys.CONTENT_ID, "Id", 100));
      columnConfigs.add(Utility.createColumnConfig(Keys.SCORE, "Score", 100, NumberFormat.getFormat("#.###")));
      for (ColumnConfig columnConfig : columnConfigs)
      {
        columnConfig.setSortable(false);
      }
      columnModel = new ColumnModel(columnConfigs);
    }
    return columnModel;
  }

  public ColumnConfig getContentTypeColumnConfig()
  {
    if (contentTypeColumnConfig == null)
    {
      contentTypeColumnConfig = new ColumnConfig(Keys.CONTENT_TYPE, "Type", 40);
      contentTypeColumnConfig.setAlignment(HorizontalAlignment.CENTER);
      contentTypeColumnConfig.setRenderer(new EnumIconRenderer());
    }
    return contentTypeColumnConfig;
  }

  public Grid<SearchResult> getGrid()
  {
    if (grid == null)
    {
      grid = new Grid<SearchResult>(getListStore(), getColumnModel());
      grid.setView(getGridViewWithScrollAdjust());
      grid.getView().setEmptyText("Enter Search Terms and optional Search Options and press Enter.");
      grid.getView().setAutoFill(true);
      grid.setContextMenu(getGridSearchMenu());
      grid.addPlugin(getRowExpander());
      grid.addListener(Events.RowDoubleClick, new DoubleClickListener());
      new SearchResultDragSource(grid);
    }
    return grid;
  }

  private Menu getGridSearchMenu()
  {
    if (gridSearchMenu == null)
    {
      gridSearchMenu = new SearchMenu(searchFrame);
    }
    return gridSearchMenu;
  }

  private GridView getGridViewWithScrollAdjust()
  {
    GridViewWithScrollAdjust gridViewWithScrollAdjust = new GridViewWithScrollAdjust();
    return gridViewWithScrollAdjust;
  }

  private ListStore<SearchResult> getListStore()
  {
    if (listStore == null)
    {
      listStore = new ListStore<SearchResult>(getBasePagingLoader());
      listStore.sort(Keys.SCORE, SortDir.DESC); // workaround possible Ext-GWT bug: listStore onLoad requires: 1) a config containing an initialized sort field and direction, and 2) a storeSorter. The config can be initialized via PagingLoadConfig and the storeSorter here.
    }
    return listStore;
  }

  private BasePagingLoadConfig getPagingLoadConfig()
  {
    if (pagingLoadConfig == null)
    {
      pagingLoadConfig = new BasePagingLoadConfig(0, PAGE_SIZE);
      pagingLoadConfig.setSortField(Keys.SCORE);
      pagingLoadConfig.setSortDir(SortDir.DESC);
    }
    return pagingLoadConfig;
  }

  private RpcProxy<BasePagingLoadResult<SearchResult>> getPagingRpcProxy()
  {
    if (pagingRpcProxy == null)
    {
      pagingRpcProxy = new PagingRpcProxy();
    }
    return pagingRpcProxy;
  }

  public PagingToolBar getPagingToolBar()
  {
    if (pagingToolBar == null)
    {
      pagingToolBar = new PagingToolBar(PAGE_SIZE);
      pagingToolBar.bind(basePagingLoader);
    }
    return pagingToolBar;
  }

  public RowExpander getRowExpander()
  {
    if (rowExpander == null)
    {
      rowExpander = new ResultRowExpander();
      rowExpander.setTemplate(XTEMPLATE);
    }
    return rowExpander;
  }

  public List<SearchResult> getSelectedItems()
  {
    return getGrid().getSelectionModel().getSelectedItems();
  }

  private void handleAutoOpen()
  {
    if (isAutoOpen)
    {
      if (getListStore().getCount() > 0)
      {
        getGrid().getSelectionModel().select(0, false);
        searchFrame.openSelectedItems();
      }
      isAutoOpen = false;
    }
  }

  private void onSearchResultsDisplayed()
  {
    expandAll();
    handleAutoOpen();
  }

  public void refreshRow(SearchResult item)
  {
    GridViewWithScrollAdjust gridViewWithScrollAdjust = (GridViewWithScrollAdjust)getGrid().getView();
    int rowOffset = getListStore().indexOf(item);
    gridViewWithScrollAdjust.refreshRow(rowOffset);
    getRowExpander().expandRow(rowOffset);
  }

  private void scheduleOnSearchResultsDisplayed()
  {
    Scheduler.get().scheduleDeferred(new ScheduledCommand()
    {
      @Override
      public void execute()
      {
        onSearchResultsDisplayed();
      }
    });
  }

  public void setAutoOpen(boolean isAutoOpen)
  {
    this.isAutoOpen = isAutoOpen;
  }

  private final class DoubleClickListener implements Listener<GridEvent<SearchResult>>
  {
    @Override
    public void handleEvent(GridEvent<SearchResult> be)
    {
      searchFrame.openSelectedItems();
    }
  }

  private final class GridViewWithScrollAdjust extends GridView
  {
    @Override
    protected int getScrollAdjust()
    {
      return scrollOffset; // with autoFill always include room for vertical scroll bar to prevent spurious horizontal scroll bar
    }

    @Override
    public void refreshRow(int row)
    {
      super.refreshRow(row); // just provide public access
    }
  }

  private final class LoadListener implements Listener<LoadEvent>
  {
    @Override
    public void handleEvent(LoadEvent loadEvent)
    {
      scheduleOnSearchResultsDisplayed();
    }
  }

  private final class PagingRpcProxy extends RpcProxy<BasePagingLoadResult<SearchResult>>
  {
    @Override
    public void load(Object loadConfig, final AsyncCallback<BasePagingLoadResult<SearchResult>> callback)
    {
      // NB: loadConfig is the BasePagingLoadConfig created by constructor (or alternatively for BaseLoader.load(loadConfig)) and reused by PagingToolBar when isReuseConfig() is true
      Directory.getConnectorService().search(Utility.getAuthenticationToken(), searchFrame.createSearchRequest(), (PagingLoadConfig)loadConfig, callback);
    }
  }

  public class ResultRowExpander extends RowExpander
  {
    private Association getSelectedSearchResult(GridEvent<?> gridEvent, String tagListKey)
    {
      int rowIndex = gridEvent.getRowIndex();
      SearchResult searchResult = getListStore().getAt(rowIndex);
      String id = gridEvent.getTarget().getId();
      int itemOffset = Integer.parseInt(id.substring(2)) - 1;
      List<Association> tagList = searchResult.get(tagListKey);
      Association tag = tagList.get(itemOffset);
      return tag;
    }

    @Override
    protected void onMouseDown(GridEvent<?> gridEvent)
    {
      String className = gridEvent.getTarget().getClassName();
      if (className.equals("connector-SearchResultAuthorTag"))
      {
        Association tag = getSelectedSearchResult(gridEvent, Keys.TAG_LIST_AUTHOR);
        searchFrame.onTagClick(tag, TagFilterType.AUTHOR);
      }
      else if (className.equals("connector-SearchResultContentTag"))
      {
        Association tag = getSelectedSearchResult(gridEvent, Keys.TAG_LIST_CONTENT);
        searchFrame.onTagClick(tag, TagFilterType.CONTENT);
      }
      else if (className.equals("connector-SearchResultSemanticTag"))
      {
        Association tag = getSelectedSearchResult(gridEvent, Keys.SEMANTIC_TAGS);
        searchFrame.onTagClick(tag, TagFilterType.SEMANTIC);
      }
      super.onMouseDown(gridEvent);
    }

  }

}
