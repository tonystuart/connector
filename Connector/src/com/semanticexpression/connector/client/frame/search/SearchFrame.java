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

import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.services.OpenExistingContentServiceRequest;
import com.semanticexpression.connector.client.widget.Frame;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Credential.AuthenticationType;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.SearchRequest;
import com.semanticexpression.connector.shared.SearchResult;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.TagConstants.TagType;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;
import com.semanticexpression.connector.shared.TagFilter;
import com.semanticexpression.connector.shared.TagFilter.TagFilterType;

public class SearchFrame extends Frame
{
  private AddTagFilterWindow addTagFilterWindow;
  private AddTagWindow addTagWindow;
  private Menu frameSearchMenu;
  private DateField fromDateField;
  private Button getMyContentButton;
  private Button resetFormButton;
  private Button runSearchButton;
  private SearchPanel searchPanel;
  private SearchResultPanel searchResultPanel;
  private SafeTextField<String> searchTermsTextField;
  private DateField toDateField;
  private ToolBar toolBar;

  public SearchFrame()
  {
    setHeading("Search");
    setSize(900, 350);
    setIcon(Resources.SEARCH);
    setLayout(new BorderLayout());
    setTopComponent(getToolBar());
    setFrameMenu(getFrameSearchMenu());

    add(getSearchPanel(), getSearchOptionsLayoutData());
    add(getSearchResultPanel(), getSearchResultsLayoutData());

    setFocusWidget(getSearchTermsTextField());
  }

  public void addTag(TagType tagType)
  {
    List<SearchResult> selectedItems = getSearchResultPanel().getSelectedItems();
    if (selectedItems.size() == 0)
    {
      Utility.displaySelectionMessageBox();
      return;
    }

    getNewTagWindow().show();
    getNewTagWindow().alignTo(getElement(), "c-c?", null);
    getNewTagWindow().toFront();
    getNewTagWindow().tag(this, selectedItems, tagType);
  }

  public void addTagFilter()
  {
    getAddTagFilterWindow().show();
    getAddTagFilterWindow().alignTo(getElement(), "c-c?", null);
    getAddTagFilterWindow().toFront();
    getAddTagFilterWindow().bind(this);
  }

  public void addTagFilter(TagFilter tagFilter)
  {
    getSearchPanel().addTagFilter(tagFilter);
  }

  public SearchRequest createSearchRequest()
  {
    String searchTerms = (String)getSearchTermsTextField().getValue();
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setTerms(searchTerms);
    searchRequest.setFromDate(getFromDateField().getValue());
    searchRequest.setToDate(getToDateField().getValue());
    getSearchPanel().prepareSearchRequest(searchRequest);
    return searchRequest;
  }

  private AddTagFilterWindow getAddTagFilterWindow()
  {
    if (addTagFilterWindow == null)
    {
      addTagFilterWindow = new AddTagFilterWindow(null);
    }
    return addTagFilterWindow;
  }

  private Menu getFrameSearchMenu()
  {
    if (frameSearchMenu == null)
    {
      frameSearchMenu = new SearchMenu(this);
    }
    return frameSearchMenu;
  }

  public DateField getFromDateField()
  {
    if (fromDateField == null)
    {
      fromDateField = new DateField();
      fromDateField.setSize("100", "20");
      fromDateField.setEmptyText("From Date");
      fromDateField.setFieldLabel("From Date");
    }
    return fromDateField;
  }

  private Button getGetMyContentButton()
  {
    if (getMyContentButton == null)
    {
      // myContentButton = new Button("Get My Content");
      getMyContentButton = new Button();
      getMyContentButton.setToolTip("Get my content");
      getMyContentButton.setIcon(Resources.SEARCH_GET_MY_CONTENT);
      getMyContentButton.addSelectionListener(new SelectionListener<ButtonEvent>()
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          getMyContent();
        }
      });
    }
    return getMyContentButton;
  }

  public void getMyContent()
  {
    Credential credential = Utility.getCredential();
    if (credential.getAuthenticationType() == AuthenticationType.UNAUTHENTICATED)
    {
      MessageBox.alert("Guest Access", "You currently have read only guest access. To search your own content, please login and try again.", null);
      return;
    }
    // resetForm();
    String userName = credential.getUserName();
    getSearchTermsTextField().setValue(Keys.CREATED_BY + ":\"" + userName + "\"");
    runSearch();
  }

  private AddTagWindow getNewTagWindow()
  {
    if (addTagWindow == null)
    {
      addTagWindow = new AddTagWindow(null);
    }
    return addTagWindow;
  }

  private Button getResetFormButton()
  {
    if (resetFormButton == null)
    {
      // resetFormButton = new Button("Reset Form");
      resetFormButton = new Button();
      resetFormButton.setToolTip("Reset form");
      resetFormButton.setIcon(Resources.SEARCH_RESET_FORM);
      resetFormButton.addSelectionListener(new SelectionListener<ButtonEvent>()
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          resetForm();
        }
      });
    }
    return resetFormButton;
  }

  public Button getRunSearchButton()
  {
    if (runSearchButton == null)
    {
      // searchButton = new Button("Run Search");
      runSearchButton = new Button();
      runSearchButton.setToolTip("Run search");
      runSearchButton.setIcon(Resources.SEARCH_RUN_SEARCH);
      runSearchButton.addListener(Events.Select, new Listener<ButtonEvent>()
      {
        public void handleEvent(ButtonEvent e)
        {
          runSearch();
        }

      });
    }
    return runSearchButton;
  }

  private BorderLayoutData getSearchOptionsLayoutData()
  {
    BorderLayoutData searchOptionsLayoutData = new BorderLayoutData(LayoutRegion.WEST, 0.25f, 0, 10000);
    searchOptionsLayoutData.setMargins(new Margins(5, 5, 5, 5));
    searchOptionsLayoutData.setSplit(true);
    searchOptionsLayoutData.setFloatable(false);
    return searchOptionsLayoutData;
  }

  public SearchPanel getSearchPanel()
  {
    if (searchPanel == null)
    {
      searchPanel = new SearchPanel();
    }
    return searchPanel;
  }

  public SearchResultPanel getSearchResultPanel()
  {
    if (searchResultPanel == null)
    {
      searchResultPanel = new SearchResultPanel(this);
    }
    return searchResultPanel;
  }

  private BorderLayoutData getSearchResultsLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
    layoutData.setMargins(new Margins(5, 5, 5, 0));
    return layoutData;
  }

  public SafeTextField<String> getSearchTermsTextField()
  {
    if (searchTermsTextField == null)
    {
      searchTermsTextField = new SafeTextField<String>();
      searchTermsTextField.addKeyListener(new SearchTermsKeyListener());
      searchTermsTextField.setWidth("150");
      searchTermsTextField.setEmptyText("Enter Search Terms");
      searchTermsTextField.setFieldLabel("Search Terms");
    }
    return searchTermsTextField;
  }

  public DateField getToDateField()
  {
    if (toDateField == null)
    {
      toDateField = new DateField();
      toDateField.setEmptyText("To Date");
      toDateField.setSize("100", "20");
      toDateField.setFieldLabel("To");
    }
    return toDateField;
  }

  public ToolBar getToolBar()
  {
    if (toolBar == null)
    {
      toolBar = new ToolBar();
      toolBar.setSpacing(3);
      toolBar.add(getSearchTermsTextField());
      toolBar.add(getFromDateField());
      toolBar.add(getToDateField());
      toolBar.add(getRunSearchButton());
      toolBar.add(getResetFormButton());
      toolBar.add(getGetMyContentButton());
    }
    return toolBar;
  }

  public void onTagClick(Association tag, TagFilterType tagFilterType)
  {
    TagFilter tagFilter = new TagFilter(tag, tagFilterType);
    getSearchPanel().addTagFilter(tagFilter);
  }

  public void open(List<SearchResult> items)
  {
    if (items.size() == 0)
    {
      Utility.displaySelectionMessageBox();
    }
    else
    {
      for (SearchResult searchResult : items)
      {
        Id contentId = searchResult.getId();
        String title = searchResult.get(Keys.TITLE);
        Directory.getServiceBus().invoke(new OpenExistingContentServiceRequest(contentId, title));
      }
    }
  }

  public void openSelectedItems()
  {
    List<SearchResult> selectedItems = getSearchResultPanel().getSelectedItems();
    open(selectedItems);
  }

  public void resetForm()
  {
    getSearchTermsTextField().clear();
    getFromDateField().clear();
    getToDateField().clear();
    getSearchPanel().resetForm();
  }

  public void runSearch()
  {
    SearchRequest searchRequest = createSearchRequest();
    if (searchRequest.getTerms() == null)
    {
      MessageBox.alert("Search Terms", "Please enter search terms and try again.", null);
      return;
    }
    if (!searchRequest.isTitle() && !searchRequest.isContent() && !searchRequest.isCaption())
    {
      MessageBox.alert("Fields to Search", "Please select one or more Fields to Search and try again.", null);
      return;
    }
    if (!searchRequest.isPublished() && !searchRequest.isUnpublished())
    {
      MessageBox.alert("Content to Retrieve", "Please select Published and/or Unpublished and try again.", null);
      return;
    }

    PagingLoadConfig pagingLoadConfig = new BasePagingLoadConfig(0, 20);
    Directory.getConnectorService().search(Utility.getAuthenticationToken(), searchRequest, pagingLoadConfig, new QueryCallback());
  }

  public void search(String searchCommand)
  {
    String term = null;

    String[] searchParameters = searchCommand.split(":");
    for (String searchParameter : searchParameters)
    {
      String[] pairs = searchParameter.split("=");
      if (pairs[0].equals("term") && pairs.length == 2)
      {
        term = pairs[1]; // TODO: decode url encoding of search terms
      }
      else if (pairs[0].equals("open"))
      {
        getSearchResultPanel().setAutoOpen(true);
      }
      else
      {
        MessageBox.alert("Search Parameters", "Invalid search parameters.", null);
      }
    }

    if (term != null)
    {
      getSearchTermsTextField().setValue(term);
      runSearch();
    }
  }

  @Override
  public String toString()
  {
    return "[SearchFrame searchTerms=" + getSearchTermsTextField().getValue() + "]";
  }

  public void updateSearchResults(List<SearchResult> selectedItems, TagType tagType, String tagName, TagVisibility tagVisibility)
  {
    for (SearchResult selectedItem : selectedItems)
    {
      Sequence<Association> tagList = null;
      switch (tagType)
      {
        case AUTHOR:
          tagList = selectedItem.get(Keys.TAG_LIST_AUTHOR);
          break;
        case CONTENT:
          tagList = selectedItem.get(Keys.TAG_LIST_CONTENT);
          break;
      }
      if (tagList != null)
      {
        Association tag = new Association();
        tag.set(Keys.NAME, tagName);
        tag.set(Keys.TAG_VISIBILITY, tagVisibility);
        tagList.add(tag);
        getSearchResultPanel().refreshRow(selectedItem);
      }
    }
  }

  private final class QueryCallback extends FailureReportingAsyncCallback<BasePagingLoadResult<SearchResult>>
  {
    @Override
    public void onSuccess(BasePagingLoadResult<SearchResult> result)
    {
      getSearchResultPanel().displayResult(result);
    }
  }

  private class SearchTermsKeyListener extends KeyListener
  {
    public void componentKeyDown(ComponentEvent event)
    {
      int keyCode = event.getKeyCode();
      if (keyCode == KeyCodes.KEY_ENTER)
      {
        runSearch();
      }
    }
  }

}
